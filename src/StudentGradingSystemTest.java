import java.io.File;

public class StudentGradingSystemTest {
    public static void main(String[] args) {
        testAddStudent();
        testRejectsDuplicateId();
        testDeleteStudent();
        testDeleteNonExistentReturnsFalse();
        testUpdateStudent();
        testUpdateNonExistentReturnsFalse();
        testFindById();
        testClassAverageEmpty();
        testClassAverage();
        testPersistenceRoundTrip();
        TestHarness.summary();
    }

    static void testAddStudent() {
        StudentGradingSystem sgs = new StudentGradingSystem(null);
        sgs.addStudent(new Student(1, "Amahle Dlamini", 75.0));
        TestHarness.assertEquals(1, sgs.getStudents().size(), "addStudent grows roster to 1");
    }

    static void testRejectsDuplicateId() {
        StudentGradingSystem sgs = new StudentGradingSystem(null);
        sgs.addStudent(new Student(1, "Amahle Dlamini", 75.0));
        TestHarness.assertThrows(
            () -> sgs.addStudent(new Student(1, "Sipho Zulu", 60.0)),
            "rejects duplicate id"
        );
    }

    static void testDeleteStudent() {
        StudentGradingSystem sgs = new StudentGradingSystem(null);
        sgs.addStudent(new Student(1, "Amahle Dlamini", 75.0));
        boolean result = sgs.deleteStudent(1);
        TestHarness.assertTrue(result, "deleteStudent returns true when found");
        TestHarness.assertEquals(0, sgs.getStudents().size(), "deleteStudent removes from roster");
    }

    static void testDeleteNonExistentReturnsFalse() {
        StudentGradingSystem sgs = new StudentGradingSystem(null);
        TestHarness.assertTrue(!sgs.deleteStudent(99), "deleteStudent returns false when not found");
    }

    static void testUpdateStudent() {
        StudentGradingSystem sgs = new StudentGradingSystem(null);
        sgs.addStudent(new Student(1, "Amahle Dlamini", 75.0));
        boolean result = sgs.updateStudent(1, "Amahle D.", 90.0);
        TestHarness.assertTrue(result, "updateStudent returns true when found");
        TestHarness.assertEquals("Amahle D.", sgs.findById(1).getName(), "updateStudent changes name");
        TestHarness.assertEquals(90.0, sgs.findById(1).getMark(), "updateStudent changes mark");
    }

    static void testUpdateNonExistentReturnsFalse() {
        StudentGradingSystem sgs = new StudentGradingSystem(null);
        TestHarness.assertTrue(!sgs.updateStudent(99, "Nobody", 50.0), "updateStudent returns false when not found");
    }

    static void testFindById() {
        StudentGradingSystem sgs = new StudentGradingSystem(null);
        sgs.addStudent(new Student(1, "Amahle Dlamini", 75.0));
        TestHarness.assertEquals("Amahle Dlamini", sgs.findById(1).getName(), "findById locates correct student");
        TestHarness.assertTrue(sgs.findById(99) == null, "findById returns null when not found");
    }

    static void testClassAverageEmpty() {
        StudentGradingSystem sgs = new StudentGradingSystem(null);
        TestHarness.assertEquals(0.0, sgs.classAverage(), "classAverage is 0 for empty roster");
    }

    static void testClassAverage() {
        StudentGradingSystem sgs = new StudentGradingSystem(null);
        sgs.addStudent(new Student(1, "A", 80.0));
        sgs.addStudent(new Student(2, "B", 60.0));
        TestHarness.assertEquals(70.0, sgs.classAverage(), "classAverage computes mean of marks");
    }

    static void testPersistenceRoundTrip() {
        String path = "test_students.csv";
        new File(path).delete();
        StudentGradingSystem sgs = new StudentGradingSystem(path);
        sgs.addStudent(new Student(1, "Amahle Dlamini", 75.0));
        sgs.save();

        StudentGradingSystem reloaded = new StudentGradingSystem(path);
        TestHarness.assertEquals(1, reloaded.getStudents().size(), "reloaded roster has persisted student");
        TestHarness.assertEquals("Amahle Dlamini", reloaded.findById(1).getName(), "persisted name survives round trip");
        new File(path).delete();
    }
}

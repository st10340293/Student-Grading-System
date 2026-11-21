import java.io.File;

public class StudentGradingSystemTest {
    public static void main(String[] args) {
        testAddStudent();
        testRejectsDuplicateId();
        testDeleteStudent();
        testDeleteNonExistentReturnsFalse();
        testUpdateStudentDetails();
        testUpdateNonExistentReturnsFalse();
        testFindById();
        testAddSubjectToStudent();
        testAddSubjectToNonExistentReturnsFalse();
        testClassAverageEmpty();
        testClassAverageAcrossStudents();
        testPersistenceRoundTripWithSubjects();
        TestHarness.summary();
    }

    static void testAddStudent() {
        StudentGradingSystem sgs = new StudentGradingSystem(null);
        sgs.addStudent(new Student(1, "Amahle Dlamini", 10));
        TestHarness.assertEquals(1, sgs.getStudents().size(), "addStudent grows roster to 1");
    }

    static void testRejectsDuplicateId() {
        StudentGradingSystem sgs = new StudentGradingSystem(null);
        sgs.addStudent(new Student(1, "Amahle Dlamini", 10));
        TestHarness.assertThrows(
            () -> sgs.addStudent(new Student(1, "Sipho Zulu", 9)),
            "rejects duplicate id"
        );
    }

    static void testDeleteStudent() {
        StudentGradingSystem sgs = new StudentGradingSystem(null);
        sgs.addStudent(new Student(1, "Amahle Dlamini", 10));
        TestHarness.assertTrue(sgs.deleteStudent(1), "deleteStudent returns true when found");
        TestHarness.assertEquals(0, sgs.getStudents().size(), "deleteStudent removes from roster");
    }

    static void testDeleteNonExistentReturnsFalse() {
        StudentGradingSystem sgs = new StudentGradingSystem(null);
        TestHarness.assertTrue(!sgs.deleteStudent(99), "deleteStudent returns false when not found");
    }

    static void testUpdateStudentDetails() {
        StudentGradingSystem sgs = new StudentGradingSystem(null);
        sgs.addStudent(new Student(1, "Amahle Dlamini", 10));
        boolean result = sgs.updateStudentDetails(1, "Amahle D.", 11);
        TestHarness.assertTrue(result, "updateStudentDetails returns true when found");
        TestHarness.assertEquals("Amahle D.", sgs.findById(1).getName(), "updateStudentDetails changes name");
        TestHarness.assertEquals(11, sgs.findById(1).getGradeLevel(), "updateStudentDetails changes grade level");
    }

    static void testUpdateNonExistentReturnsFalse() {
        StudentGradingSystem sgs = new StudentGradingSystem(null);
        TestHarness.assertTrue(!sgs.updateStudentDetails(99, "Nobody", 8), "updateStudentDetails returns false when not found");
    }

    static void testFindById() {
        StudentGradingSystem sgs = new StudentGradingSystem(null);
        sgs.addStudent(new Student(1, "Amahle Dlamini", 10));
        TestHarness.assertEquals("Amahle Dlamini", sgs.findById(1).getName(), "findById locates correct student");
        TestHarness.assertTrue(sgs.findById(99) == null, "findById returns null when not found");
    }

    static void testAddSubjectToStudent() {
        StudentGradingSystem sgs = new StudentGradingSystem(null);
        sgs.addStudent(new Student(1, "Amahle Dlamini", 10));
        boolean result = sgs.addSubjectToStudent(1, "Mathematics", 85.0);
        TestHarness.assertTrue(result, "addSubjectToStudent returns true when student found");
        TestHarness.assertEquals(85.0, sgs.findById(1).getSubjectMark("Mathematics"), "subject mark stored on student");
    }

    static void testAddSubjectToNonExistentReturnsFalse() {
        StudentGradingSystem sgs = new StudentGradingSystem(null);
        TestHarness.assertTrue(!sgs.addSubjectToStudent(99, "Mathematics", 85.0), "addSubjectToStudent returns false when not found");
    }

    static void testClassAverageEmpty() {
        StudentGradingSystem sgs = new StudentGradingSystem(null);
        TestHarness.assertEquals(0.0, sgs.classAverage(), "classAverage is 0 for empty roster");
    }

    static void testClassAverageAcrossStudents() {
        StudentGradingSystem sgs = new StudentGradingSystem(null);
        Student a = new Student(1, "A", 10);
        a.addSubject("Mathematics", 80.0);
        Student b = new Student(2, "B", 9);
        b.addSubject("Mathematics", 60.0);
        sgs.addStudent(a);
        sgs.addStudent(b);
        TestHarness.assertEquals(70.0, sgs.classAverage(), "classAverage means each student's average");
    }

    static void testPersistenceRoundTripWithSubjects() {
        String path = "test_students.csv";
        new File(path).delete();
        StudentGradingSystem sgs = new StudentGradingSystem(path);
        sgs.addStudent(new Student(1, "Amahle Dlamini", 10));
        sgs.addSubjectToStudent(1, "Mathematics", 85.0);
        sgs.addSubjectToStudent(1, "English", 70.0);
        sgs.save();

        StudentGradingSystem reloaded = new StudentGradingSystem(path);
        Student s = reloaded.findById(1);
        TestHarness.assertEquals(1, reloaded.getStudents().size(), "reloaded roster has persisted student");
        TestHarness.assertEquals("Amahle Dlamini", s.getName(), "persisted name survives round trip");
        TestHarness.assertEquals(10, s.getGradeLevel(), "persisted grade level survives round trip");
        TestHarness.assertEquals(85.0, s.getSubjectMark("Mathematics"), "persisted Mathematics mark survives round trip");
        TestHarness.assertEquals(70.0, s.getSubjectMark("English"), "persisted English mark survives round trip");
        new File(path).delete();
    }
}
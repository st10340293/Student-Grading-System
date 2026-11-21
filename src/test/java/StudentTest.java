public class StudentTest {
    public static void main(String[] args) {
        testConstructorAndGetters();
        testRejectsGradeLevelBelowEight();
        testRejectsGradeLevelAboveTwelve();
        testAddSubjectAndGetMark();
        testRejectsSubjectMarkAboveHundred();
        testRejectsSubjectMarkBelowZero();
        testAverageAcrossSubjects();
        testAverageWithNoSubjectsIsZero();
        testLetterGradeA();
        testLetterGradeF();
        testUpdateExistingSubjectMark();
        TestHarness.summary();
    }

    static void testConstructorAndGetters() {
        Student s = new Student(1, "Thabo Nkosi", 10);
        TestHarness.assertEquals(1, s.getId(), "constructor sets id");
        TestHarness.assertEquals("Thabo Nkosi", s.getName(), "constructor sets name");
        TestHarness.assertEquals(10, s.getGradeLevel(), "constructor sets grade level");
    }

    static void testRejectsGradeLevelBelowEight() {
        TestHarness.assertThrows(() -> new Student(2, "Test", 7), "rejects grade level below 8");
    }

    static void testRejectsGradeLevelAboveTwelve() {
        TestHarness.assertThrows(() -> new Student(3, "Test", 13), "rejects grade level above 12");
    }

    static void testAddSubjectAndGetMark() {
        Student s = new Student(4, "Test", 9);
        s.addSubject("Mathematics", 85.0);
        TestHarness.assertEquals(85.0, s.getSubjectMark("Mathematics"), "getSubjectMark returns added mark");
    }

    static void testRejectsSubjectMarkAboveHundred() {
        Student s = new Student(5, "Test", 9);
        TestHarness.assertThrows(() -> s.addSubject("Mathematics", 150.0), "rejects subject mark above 100");
    }

    static void testRejectsSubjectMarkBelowZero() {
        Student s = new Student(6, "Test", 9);
        TestHarness.assertThrows(() -> s.addSubject("Mathematics", -5.0), "rejects subject mark below 0");
    }

    static void testAverageAcrossSubjects() {
        Student s = new Student(7, "Test", 11);
        s.addSubject("Mathematics", 80.0);
        s.addSubject("English", 60.0);
        TestHarness.assertEquals(70.0, s.getAverage(), "average computed across subjects");
    }

    static void testAverageWithNoSubjectsIsZero() {
        Student s = new Student(8, "Test", 8);
        TestHarness.assertEquals(0.0, s.getAverage(), "average is 0 with no subjects");
    }

    static void testLetterGradeA() {
        Student s = new Student(9, "Test", 12);
        s.addSubject("Mathematics", 90.0);
        TestHarness.assertEquals("A", s.getLetterGrade(), "90 average yields A grade");
    }

    static void testLetterGradeF() {
        Student s = new Student(10, "Test", 8);
        s.addSubject("Mathematics", 40.0);
        TestHarness.assertEquals("F", s.getLetterGrade(), "40 average yields F grade");
    }

    static void testUpdateExistingSubjectMark() {
        Student s = new Student(11, "Test", 9);
        s.addSubject("Mathematics", 60.0);
        s.addSubject("Mathematics", 75.0);
        TestHarness.assertEquals(75.0, s.getSubjectMark("Mathematics"), "re-adding subject overwrites mark");
    }
}
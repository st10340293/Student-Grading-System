public class StudentTest {
    public static void main(String[] args) {
        testConstructorAndGetters();
        testLetterGradeA();
        testLetterGradeB();
        testLetterGradeF();
        testRejectsMarkAboveHundred();
        testRejectsNegativeMark();
        TestHarness.summary();
    }

    static void testConstructorAndGetters() {
        Student s = new Student(1, "Thabo Nkosi", 85.0);
        TestHarness.assertEquals(1, s.getId(), "constructor sets id");
        TestHarness.assertEquals("Thabo Nkosi", s.getName(), "constructor sets name");
        TestHarness.assertEquals(85.0, s.getMark(), "constructor sets mark");
    }

    static void testLetterGradeA() {
        Student s = new Student(2, "Test", 90.0);
        TestHarness.assertEquals("A", s.getLetterGrade(), "90 mark yields A grade");
    }

    static void testLetterGradeB() {
        Student s = new Student(3, "Test", 72.0);
        TestHarness.assertEquals("B", s.getLetterGrade(), "72 mark yields B grade");
    }

    static void testLetterGradeF() {
        Student s = new Student(4, "Test", 40.0);
        TestHarness.assertEquals("F", s.getLetterGrade(), "40 mark yields F grade");
    }

    static void testRejectsMarkAboveHundred() {
        TestHarness.assertThrows(() -> new Student(5, "Test", 150.0), "rejects mark above 100");
    }

    static void testRejectsNegativeMark() {
        TestHarness.assertThrows(() -> new Student(6, "Test", -5.0), "rejects negative mark");
    }
}

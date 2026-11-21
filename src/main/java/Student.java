import java.util.LinkedHashMap;
import java.util.Map;

public class Student {
    private final int id;
    private String name;
    private int gradeLevel;
    private final Map<String, Double> subjects = new LinkedHashMap<>();

    public Student(int id, String name, int gradeLevel) {
        validateGradeLevel(gradeLevel);
        this.id = id;
        this.name = name;
        this.gradeLevel = gradeLevel;
    }

    private static void validateGradeLevel(int gradeLevel) {
        if (gradeLevel < 8 || gradeLevel > 12) {
            throw new IllegalArgumentException("Grade level must be between 8 and 12, got: " + gradeLevel);
        }
    }

    private static void validateMark(double mark) {
        if (mark < 0 || mark > 100) {
            throw new IllegalArgumentException("Mark must be between 0 and 100, got: " + mark);
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(int gradeLevel) {
        validateGradeLevel(gradeLevel);
        this.gradeLevel = gradeLevel;
    }

    public void addSubject(String subject, double mark) {
        validateMark(mark);
        subjects.put(subject, mark);
    }

    public boolean removeSubject(String subject) {
        return subjects.remove(subject) != null;
    }

    public Double getSubjectMark(String subject) {
        return subjects.get(subject);
    }

    public Map<String, Double> getSubjects() {
        return subjects;
    }

    public double getAverage() {
        if (subjects.isEmpty()) return 0.0;
        double sum = 0;
        for (double mark : subjects.values()) sum += mark;
        return sum / subjects.size();
    }

    public String getLetterGrade() {
        double avg = getAverage();
        if (avg >= 80) return "Level 7";
        if (avg >= 70) return "Level 6";
        if (avg >= 60) return "Level 5";
        if (avg >= 50) return "Level 4";
        if (avg >= 40) return "Level 3";
        if (avg >= 30) return "Level 2";
        return "Level 1";
    }

    @Override
    public String toString() {
        return id + "," + name + "," + gradeLevel;
    }
}
import java.io.*;
import java.util.*;

public class StudentGradingSystem {
    private final List<Student> students = new ArrayList<>();
    private final String filePath;

    public StudentGradingSystem(String filePath) {
        this.filePath = filePath;
        if (filePath != null) {
            load();
        }
    }

    public void addStudent(Student student) {
        if (findById(student.getId()) != null) {
            throw new IllegalArgumentException("Student with id " + student.getId() + " already exists.");
        }
        students.add(student);
    }

    public boolean deleteStudent(int id) {
        return students.removeIf(s -> s.getId() == id);
    }

    public boolean updateStudentDetails(int id, String name, int gradeLevel) {
        Student s = findById(id);
        if (s == null) return false;
        s.setName(name);
        s.setGradeLevel(gradeLevel);
        return true;
    }

    public boolean addSubjectToStudent(int id, String subject, double mark) {
        Student s = findById(id);
        if (s == null) return false;
        s.addSubject(subject, mark);
        return true;
    }

    public Student findById(int id) {
        for (Student s : students) {
            if (s.getId() == id) return s;
        }
        return null;
    }

    public List<Student> getStudents() {
        return students;
    }

    // Mean of each student's own average, not a mark-by-mark mean --
    // keeps students with more subjects from skewing the class figure.
    public double classAverage() {
        if (students.isEmpty()) return 0.0;
        double sum = 0;
        for (Student s : students) sum += s.getAverage();
        return sum / students.size();
    }

    // CSV format: id,name,gradeLevel,subject:mark;subject:mark;...
    public void save() {
        if (filePath == null) return;
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            for (Student s : students) {
                StringBuilder subjectsPart = new StringBuilder();
                for (Map.Entry<String, Double> entry : s.getSubjects().entrySet()) {
                    if (subjectsPart.length() > 0) subjectsPart.append(";");
                    subjectsPart.append(entry.getKey()).append(":").append(entry.getValue());
                }
                pw.println(s.getId() + "," + s.getName() + "," + s.getGradeLevel() + "," + subjectsPart);
            }
        } catch (IOException e) {
            System.out.println("Error saving students: " + e.getMessage());
        }
    }

    private void load() {
        File file = new File(filePath);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] parts = line.split(",", 4);
                int id = Integer.parseInt(parts[0].trim());
                String name = parts[1].trim();
                int gradeLevel = Integer.parseInt(parts[2].trim());
                Student student = new Student(id, name, gradeLevel);
                if (parts.length > 3 && !parts[3].isBlank()) {
                    for (String pair : parts[3].split(";")) {
                        String[] kv = pair.split(":", 2);
                        student.addSubject(kv[0].trim(), Double.parseDouble(kv[1].trim()));
                    }
                }
                students.add(student);
            }
        } catch (IOException e) {
            System.out.println("Error loading students: " + e.getMessage());
        }
    }
}
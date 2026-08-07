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

    public boolean updateStudent(int id, String name, double mark) {
        Student s = findById(id);
        if (s == null) return false;
        s.setName(name);
        s.setMark(mark);
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

    public double classAverage() {
        if (students.isEmpty()) return 0.0;
        double sum = 0;
        for (Student s : students) sum += s.getMark();
        return sum / students.size();
    }

    // Plain CSV persistence: id,name,mark per line. One clear format,
    // no mismatch between save format and load format.
    public void save() {
        if (filePath == null) return;
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            for (Student s : students) {
                pw.println(s.getId() + "," + s.getName() + "," + s.getMark());
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
                String[] parts = line.split(",", 3);
                int id = Integer.parseInt(parts[0].trim());
                String name = parts[1].trim();
                double mark = Double.parseDouble(parts[2].trim());
                students.add(new Student(id, name, mark));
            }
        } catch (IOException e) {
            System.out.println("Error loading students: " + e.getMessage());
        }
    }
}

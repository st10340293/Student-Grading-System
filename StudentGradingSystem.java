import java.io.*;
import java.util.*;

public class StudentGradingSystem {
    private List<Student> students;
    private static final String FILE_PATH = "students.txt";

    public StudentGradingSystem() {
        students = new ArrayList<>();
        loadStudents();
    }

    // Load students from file
    private void loadStudents() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH))) {
            students = (List<Student>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading students: " + e.getMessage());
        }
    }

    // Update student details and write back to file
    public void updateStudent(int id, String name, String grade) {
        for (Student student : students) {
            if (student.getId() == id) {
                student.setName(name);
                student.setGrade(grade);
                saveStudents(); // Persist changes
                return;
            }
        }
        System.out.println("Student not found.");
    }

    // Persist student data to file
    private void saveStudents() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(students);
        } catch (IOException e) {
            System.out.println("Error saving students: " + e.getMessage());
        }
    }

    // Convert student data to a String for file writing
    public String toFileString() {
        StringBuilder sb = new StringBuilder();
        for (Student student : students) {
            sb.append(student.toString()).append('\n');
        }
        return sb.toString();
    }

    // Add other methods as needed
    public static void main(String[] args) {
        StudentGradingSystem sgs = new StudentGradingSystem();
        // Example usage
        sgs.updateStudent(1, "John Doe", "A");
    }
}

class Student implements Serializable {
    private int id;
    private String name;
    private String grade;

    public Student(int id, String name, String grade) {
        this.id = id;
        this.name = name;
        this.grade = grade;
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

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    @Override
    public String toString() {
        return id + "|" + name + "|" + grade;
    }
}
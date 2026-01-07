import java.io.*;
import java.util.*;

class Student {
    String id;
    String name;
    String[] subjects;
    int[] marks;
    double average;
    char grade;

    public Student(String id, String name, String[] subjects, int[] marks) {
        this.id = id;
        this.name = name;
        this.subjects = subjects;
        this.marks = marks;
        calculateAverage();
        assignGrade();
    }

    void calculateAverage() {
        int total = 0;
        for (int m : marks) total += m;
        average = total / (double) marks.length;
    }

    void assignGrade() {
        if (average >= 75) grade = 'A';
        else if (average >= 65) grade = 'B';
        else if (average >= 55) grade = 'C';
        else if (average >= 45) grade = 'D';
        else grade = 'F';
    }

    String toFileString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < subjects.length; i++) {
            sb.append(subjects[i]).append(":").append(marks[i]).append(",");
        }
        return id + "|" + name + "|" + sb + average + "|" + grade;
    }
}

public class StudentGradingSystem {

    static final String FILE_NAME = "students.txt";
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n===== STUDENT GRADING SYSTEM =====");
            System.out.println("1. Add Student");
            System.out.println("2. View Students");
            System.out.println("3. Update Student");
            System.out.println("4. Delete Student");
            System.out.println("5. Class Statistics");
            System.out.println("6. Exit");
            System.out.print("Choose option: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1 -> addStudent();
                case 2 -> viewStudents();
                case 3 -> updateStudent();
                case 4 -> deleteStudent();
                case 5 -> classStatistics();
                case 6 -> System.exit(0);
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    // ---------------- ADD STUDENT ----------------
    static void addStudent() {
    try (FileWriter fw = new FileWriter(FILE_NAME, true)) {

        System.out.print("Student ID: ");
        String id = sc.nextLine();

        System.out.print("Student Name: ");
        String name = sc.nextLine();

        System.out.print("Number of subjects: ");
        int n = sc.nextInt();
        sc.nextLine();

        String[] subjects = new String[n];
        int[] marks = new int[n];

        for (int i = 0; i < n; i++) {
            System.out.print("Subject name: ");
            subjects[i] = sc.nextLine();

            System.out.print("Mark for " + subjects[i] + ": ");
            marks[i] = sc.nextInt();
            sc.nextLine();
        }

        Student s = new Student(id, name, subjects, marks);
        fw.write(s.toFileString() + "\n");

        System.out.println("Student added successfully!");

    } catch (IOException e) {
        System.out.println("File error!");
    }
}

    // ---------------- VIEW STUDENTS ----------------
    static void viewStudents() {
    try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
        String line;
        System.out.println("\n--- STUDENT RECORDS ---");

        while ((line = br.readLine()) != null) {
            String[] data = line.split("\\|");

            System.out.println("ID: " + data[0]);
            System.out.println("Name: " + data[1]);
            System.out.println("Subjects & Marks:");

            String[] subjectData = data[2].split(",");
            for (String s : subjectData) {
                if (!s.isEmpty()) {
                    System.out.println("  - " + s.replace(":", ": "));
                }
            }

            System.out.println("Average: " + data[data.length - 2]);
            System.out.println("Grade: " + data[data.length - 1]);
            System.out.println("-------------------------");
        }

    } catch (IOException e) {
        System.out.println("No records found.");
    }
}


    // ---------------- UPDATE STUDENT ----------------
    static void updateStudent() {
        List<String> students = readAll();
        System.out.print("Enter ID to update: ");
        String id = sc.nextLine();
        boolean found = false;

        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).startsWith(id + "|")) {
                found = true;
                System.out.print("New name: ");
                String name = sc.nextLine();

                System.out.print("Number of subjects: ");
                int n = sc.nextInt();
                sc.nextLine();

                String[] subjects = new String[n];
                int[] marks = new int[n];

                for (int j = 0; j < n; j++) {
                    System.out.print("Subject name: ");
                    subjects[j] = sc.nextLine();

                    System.out.print("Mark for " + subjects[j] + ": ");
                    marks[j] = sc.nextInt();
                    sc.nextLine();
                }

                Student s = new Student(id, name, subjects, marks);
                students.set(i, s.toFileString());

                    }
        }
    }

    // ---------------- DELETE STUDENT ----------------
    static void deleteStudent() {
        List<String> students = readAll();
        System.out.print("Enter ID to delete: ");
        String id = sc.nextLine();

        boolean removed = students.removeIf(s -> s.startsWith(id + "|"));
        writeAll(students);

        if (removed) System.out.println("Student deleted.");
        else System.out.println("Student not found.");
    }

    // ---------------- CLASS STATISTICS ----------------
    static void classStatistics() {
        List<String> students = readAll();
        if (students.isEmpty()) {
            System.out.println("No data available.");
            return;
        }

        double totalAvg = 0;
        double highest = Double.MIN_VALUE;
        double lowest = Double.MAX_VALUE;

        for (String s : students) {
            String[] data = s.split("\\|");
            double avg = Double.parseDouble(data[data.length - 2]);
            totalAvg += avg;
            highest = Math.max(highest, avg);
            lowest = Math.min(lowest, avg);
        }

        System.out.println("\n--- CLASS STATISTICS ---");
        System.out.println("Class Average: " + (totalAvg / students.size()));
        System.out.println("Highest Average: " + highest);
        System.out.println("Lowest Average: " + lowest);
    }

    // ---------------- FILE HELPERS ----------------
    static List<String> readAll() {
        List<String> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = br.readLine()) != null) list.add(line);
        } catch (IOException ignored) {}
        return list;
    }

    static void writeAll(List<String> list) {
        try (FileWriter fw = new FileWriter(FILE_NAME)) {
            for (String s : list) fw.write(s + "\n");
        } catch (IOException e) {
            System.out.println("File write error.");
        }
    }
}

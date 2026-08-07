import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final String DATA_FILE = "students.csv";

    public static void main(String[] args) {
        StudentGradingSystem sgs = new StudentGradingSystem(DATA_FILE);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> addStudent(sgs, scanner);
                case "2" -> listStudents(sgs);
                case "3" -> updateStudentDetails(sgs, scanner);
                case "4" -> deleteStudent(sgs, scanner);
                case "5" -> addSubject(sgs, scanner);
                case "6" -> showAverage(sgs);
                case "7" -> {
                    sgs.save();
                    System.out.println("Saved. Goodbye.");
                    return;
                }
                default -> System.out.println("Invalid option, try again.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("""

                === Student Grading System ===
                1. Add student
                2. List students
                3. Update student details
                4. Delete student
                5. Add/update subject mark
                6. Show class average
                7. Save and exit
                Choose an option:""");
    }

    private static void addStudent(StudentGradingSystem sgs, Scanner scanner) {
        try {
            System.out.print("ID: ");
            int id = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Grade level (8-12): ");
            int gradeLevel = Integer.parseInt(scanner.nextLine().trim());

            sgs.addStudent(new Student(id, name, gradeLevel));
            System.out.println("Added.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid number entered.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void listStudents(StudentGradingSystem sgs) {
        if (sgs.getStudents().isEmpty()) {
            System.out.println("No students yet.");
            return;
        }
        for (Student s : sgs.getStudents()) {
            System.out.printf("%-5d %-25s Grade %-3d Avg %-6.1f %-6s%n",
                    s.getId(), s.getName(), s.getGradeLevel(), s.getAverage(), s.getLetterGrade());
            for (Map.Entry<String, Double> entry : s.getSubjects().entrySet()) {
                System.out.printf("      - %-20s %.1f%n", entry.getKey(), entry.getValue());
            }
        }
    }

    private static void updateStudentDetails(StudentGradingSystem sgs, Scanner scanner) {
        try {
            System.out.print("ID to update: ");
            int id = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("New name: ");
            String name = scanner.nextLine().trim();
            System.out.print("New grade level (8-12): ");
            int gradeLevel = Integer.parseInt(scanner.nextLine().trim());

            if (sgs.updateStudentDetails(id, name, gradeLevel)) {
                System.out.println("Updated.");
            } else {
                System.out.println("Student not found.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number entered.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void deleteStudent(StudentGradingSystem sgs, Scanner scanner) {
        System.out.print("ID to delete: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            System.out.println(sgs.deleteStudent(id) ? "Deleted." : "Student not found.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid number entered.");
        }
    }

    private static void addSubject(StudentGradingSystem sgs, Scanner scanner) {
        try {
            System.out.print("Student ID: ");
            int id = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Subject name: ");
            String subject = scanner.nextLine().trim();
            System.out.print("Mark (0-100): ");
            double mark = Double.parseDouble(scanner.nextLine().trim());

            if (sgs.addSubjectToStudent(id, subject, mark)) {
                System.out.println("Saved.");
            } else {
                System.out.println("Student not found.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number entered.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void showAverage(StudentGradingSystem sgs) {
        System.out.printf("Class average: %.2f%n", sgs.classAverage());
    }
}
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
                case "3" -> updateStudent(sgs, scanner);
                case "4" -> deleteStudent(sgs, scanner);
                case "5" -> showAverage(sgs);
                case "6" -> {
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
                3. Update student
                4. Delete student
                5. Show class average
                6. Save and exit
                Choose an option:""");
    }

    private static void addStudent(StudentGradingSystem sgs, Scanner scanner) {
        try {
            System.out.print("ID: ");
            int id = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Mark (0-100): ");
            double mark = Double.parseDouble(scanner.nextLine().trim());

            sgs.addStudent(new Student(id, name, mark));
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
        System.out.printf("%-5s %-25s %-6s %-6s%n", "ID", "Name", "Mark", "Grade");
        for (Student s : sgs.getStudents()) {
            System.out.printf("%-5d %-25s %-6.1f %-6s%n", s.getId(), s.getName(), s.getMark(), s.getLetterGrade());
        }
    }

    private static void updateStudent(StudentGradingSystem sgs, Scanner scanner) {
        try {
            System.out.print("ID to update: ");
            int id = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("New name: ");
            String name = scanner.nextLine().trim();
            System.out.print("New mark (0-100): ");
            double mark = Double.parseDouble(scanner.nextLine().trim());

            if (sgs.updateStudent(id, name, mark)) {
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

    private static void showAverage(StudentGradingSystem sgs) {
        System.out.printf("Class average: %.2f%n", sgs.classAverage());
    }
}

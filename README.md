# Student Grading System

Java app for managing student records and grades, with a Swing GUI and a console menu.

## Features

- Add, update, delete, list students
- Each student has a grade level (8–12) and any number of subjects, each with its own mark
- Auto-computed letter grade (A–F) from a student's average mark across subjects
- Class average (mean of each student's own average)
- Plain-text CSV persistence (`students.csv`) — human-readable, no binary serialization
- Input validation (marks 0–100, grade level 8–12, no duplicate student IDs)

## Structure

```
.
├── README.md
├── .gitignore
├── docs/                      (design notes, screenshots)
└── src/
    ├── main/java/
    │   ├── Student.java               model, grade calculation, mark validation
    │   ├── StudentGradingSystem.java  CRUD, persistence, class average
    │   ├── Main.java                  CLI menu
    │   └── GradingGui.java            Swing GUI
    └── test/java/
        ├── TestHarness.java           zero-dependency test runner
        ├── StudentTest.java
        └── StudentGradingSystemTest.java
```

## Run

GUI:

```bash
javac src/main/java/*.java -d out
cd out
java GradingGui
```

Console menu:

```bash
javac src/main/java/*.java -d out
cd out
java Main
```

Data saves to `students.csv` in the working directory: on exit in the console menu (option 7); in the GUI with Ctrl+S or automatically when the window closes.
GUI and console share the same file.

CSV format: `id,name,gradeLevel,subject:mark;subject:mark;...`

## Run tests

```bash
javac src/main/java/*.java src/test/java/*.java -d out
cd out
java StudentTest
java StudentGradingSystemTest
```

Built test-first: every method in `Student` and `StudentGradingSystem` has a
test that was verified to fail before the implementation existed.
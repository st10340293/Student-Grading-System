# Student Grading System

Java console app for managing student records and grades.

## Features

- Add, update, delete, list students
- Each student has a grade level (8–12) and any number of subjects, each with its own mark
- Auto-computed letter grade (A–F) from a student's average mark across subjects
- Class average (mean of each student's own average)
- Plain-text CSV persistence (`students.csv`) — human-readable, no binary serialization
- Input validation (marks 0–100, grade level 8–12, no duplicate student IDs)

## Structure

- `Student.java` — student model, grade calculation, mark validation
- `StudentGradingSystem.java` — CRUD operations, persistence, class average
- `Main.java` — CLI menu
- `TestHarness.java` — lightweight zero-dependency test runner
- `StudentTest.java`, `StudentGradingSystemTest.java` — unit tests

## Run

```bash
javac src/*.java -d out
cd out
java Main
```

Data saves to `students.csv` in the working directory on exit (option 7).

CSV format: `id,name,gradeLevel,subject:mark;subject:mark;...`

## Run tests

```bash
javac src/*.java -d out
cd out
java StudentTest
java StudentGradingSystemTest
```

Built test-first: every method in `Student` and `StudentGradingSystem` has a
test that was verified to fail before the implementation existed.
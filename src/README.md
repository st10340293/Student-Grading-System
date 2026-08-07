# Student Grading System

Java console app for managing student records and grades.

## Features

- Add, update, delete, list students
- Auto-computed letter grade (A–F) from numeric mark
- Class average calculation
- Plain-text CSV persistence (`students.csv`) — human-readable, no binary serialization
- Input validation (marks must be 0–100, no duplicate IDs)

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

Data saves to `students.csv` in the working directory on exit (option 6).

## Run tests

```bash
javac src/*.java -d out
cd out
java StudentTest
java StudentGradingSystemTest
```

Built test-first: every method in `Student` and `StudentGradingSystem` has a
test that was verified to fail before the implementation existed.

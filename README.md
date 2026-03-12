# ITI Student Exam System (SQL Server)

Database schema + stored procedures for an ITI-style examination system: branches/tracks/courses, question bank, exam generation, student submissions, auto-correction, and reporting.

## Contents

- [ERD](#erd)
- [Tech](#tech)
- [Setup](#setup)
- [Key Stored Procedures](#key-stored-procedures)
- [Reports](#reports)
- [Roles & Permissions](#roles--permissions)
- [Team](#team)
- [Project Structure](#project-structure)

## ERD

![ERD](ERD/ERD.png)

## Tech

- Microsoft SQL Server (T-SQL)
- SSMS / Azure Data Studio (or any SQL client that can run `.sql` scripts)

## Setup

1. Open SQL Server Management Studio (SSMS).
2. Run the scripts in this recommended order:

   1. [CreateDbTables.sql](CreateDbTables.sql) (creates `ITI_ExaminationDB` and all tables)
   2. CRUD stored procedures:
      - [BranchCRUD_SP.sql](BranchCRUD_SP.sql)
      - [TrackCRUD_sp.sql](TrackCRUD_sp.sql)
      - [CourseCRUD_sp.sql](CourseCRUD_sp.sql)
   3. Exam + submission + correction procedures:
      - [ExamCRUD_SP.sql](ExamCRUD_SP.sql)
   4. Reports procedures (if you prefer them separated):
      - [REPORTS_SP.sql](REPORTS_SP.sql)
   5. Users/roles (optional):
      - [UserRoles&Enviroment.sql](UserRoles&Enviroment.sql)
   6. Sample seed data:
      - [sample-data.sql](sample-data.sql)

Notes:
- If you run [ExamCRUD_SP.sql](ExamCRUD_SP.sql) you may not need [REPORTS_SP.sql](REPORTS_SP.sql) because some report procedures appear in both.
- [UserRoles&Enviroment.sql](UserRoles&Enviroment.sql) creates SQL logins/users and grants permissions. Review and change any placeholder passwords before using in a real environment.

## Key Stored Procedures

### CRUD

- `InsertBranch`, `UpdateBranch`, `DeleteBranch`, `SelectBranch`
- `InsertTrack`, `UpdateTrack`, `DeleteTrack`, `SelectTrackByBranch`
- `InsertCourse`, `UpdateCourse`, `DeleteCourse`, `SelectCourseByTrack`

### Exams

- `GenerateExam`  
  Creates an exam header in `Exam`, randomly selects questions from `Question`, and fills `Exam_Question`.

Example:

```sql
EXEC GenerateExam
    @CourseID = 1,
    @ExamName = N'SQL Fundamentals - Quiz 1',
    @NumMCQ = 10,
    @NumTF = 5;
```

To view generated questions:

```sql
SELECT
    e.ExamID,
    e.ExamName,
    eq.OrderNo,
    q.QuestionText,
    q.QuestionType,
    q.Points
FROM Exam e
JOIN Exam_Question eq ON e.ExamID = eq.ExamID
JOIN Question q ON eq.QuestionID = q.QuestionID
WHERE e.ExamID = 1
ORDER BY eq.OrderNo;
```

- `SubmitExamAnswers`  
  Inserts a row into `StudentExam`, then inserts answers into `StudentAnswer` from an XML payload.

Example:

```sql
DECLARE @Answers XML = N'
<Answers>
  <Answer><QuestionID>1</QuestionID><ChosenOptionID>1</ChosenOptionID></Answer>
  <Answer><QuestionID>2</QuestionID><ChosenOptionID>5</ChosenOptionID></Answer>
</Answers>';

EXEC SubmitExamAnswers
    @StudentID = 1,
    @ExamID = 1,
    @StartTime = '2026-01-01T10:00:00',
    @EndTime = '2026-01-01T10:30:00',
    @Answers = @Answers;
```

- `CorrectExam`  
  Computes the final grade by comparing `StudentAnswer.ChosenOptionID` with `ModelAnswer.OptionID`, then updates `StudentExam.TotalGrade`.

Example:

```sql
EXEC CorrectExam @StudentExamID = 1;

SELECT StudentExamID, StudentID, ExamID, TotalGrade
FROM StudentExam
WHERE StudentExamID = 1;
```

## Reports

- `Report_StudentsByDepartment @DepartmentNo`
- `Report_StudentGrades @StudentID`
- `Report_InstructorCourses @InstructorID`

Examples:

```sql
EXEC Report_StudentGrades @StudentID = 1;
EXEC Report_InstructorCourses @InstructorID = 1;
```

## Roles & Permissions

The [UserRoles&Enviroment.sql](UserRoles&Enviroment.sql) script demonstrates:

- An admin user mapped to `db_owner`
- An instructor user with read access plus write permissions on exam/question tables
- A student user with read access plus insert permissions for submitting answers, while denying reads on sensitive tables like `StudentAnswer` and `ModelAnswer`

## Team

- Eng Shorouk Elkassas
- Eng Mohamed Helmy
- Eng Ahmed El Arabawy
- Eng Mahmoud Farid

## Project Structure

- `ERD/` – ERD diagram
- `CreateDbTables.sql` – database and schema (tables)
- `sample-data.sql` – sample seed data (branches, tracks, courses, instructors, students, questions, options, model answers)
- `*CRUD*_SP.sql` – CRUD stored procedures
- `ExamCRUD_SP.sql` – exam generation/submission/correction procedures (and may include report procs)
- `REPORTS_SP.sql` – reporting stored procedures
- `ITI_Exam_System_SRS_v3.pdf` – requirements/specification document


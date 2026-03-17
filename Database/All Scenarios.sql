USE ITI_ExaminationDB;
GO

/* Scenario 1: GenerateExam — valid inputs (MCQ only for demo) */
DECLARE @CourseID INT = COALESCE((SELECT MIN(CourseID) FROM Course), 1);
EXEC GenerateExam 
    @CourseID = @CourseID, 
    @ExamName = N'Sample Exam - Auto',
    @NumMCQ = 10,
    @NumTF = 0;

/* Fetch latest ExamID for the course we just generated */
DECLARE @ExamID INT;
SELECT TOP 1 @ExamID = ExamID 
FROM Exam 
WHERE CourseID = @CourseID
ORDER BY ExamID DESC;

/* Scenario 2: GenerateExam — not enough questions (expect error) */
BEGIN TRY
    EXEC GenerateExam 
        @CourseID = @CourseID, 
        @ExamName = N'Stress Test Exam', 
        @NumMCQ = 100, 
        @NumTF = 100;
END TRY
BEGIN CATCH
    PRINT 'Expected failure (not enough questions): ' + ERROR_MESSAGE();
END CATCH

/* Scenario 3: SubmitExamAnswers — all correct for first few questions */
DECLARE @AnswersCorrect XML =
(
    SELECT 
        (SELECT TOP 5 
                eq.QuestionID      AS [QuestionID],
                ma.OptionID        AS [ChosenOptionID]
         FROM Exam_Question eq
         JOIN ModelAnswer   ma ON ma.QuestionID = eq.QuestionID
         WHERE eq.ExamID = @ExamID
         ORDER BY eq.OrderNo
         FOR XML PATH('Answer'), TYPE)
    FOR XML PATH('Answers')
);

DECLARE @StartTime1 DATETIME = DATEADD(MINUTE, -60, GETDATE());
DECLARE @EndTime1   DATETIME = GETDATE();
EXEC SubmitExamAnswers 
    @StudentID = 1, 
    @ExamID = @ExamID, 
    @StartTime = @StartTime1,
    @EndTime   = @EndTime1,
    @Answers   = @AnswersCorrect;

/* Get latest StudentExamID for student 1 / that exam and correct it */
DECLARE @SE_Correct INT;
SELECT @SE_Correct = MAX(StudentExamID)
FROM StudentExam
WHERE StudentID = 1 AND ExamID = @ExamID;

EXEC CorrectExam @StudentExamID = @SE_Correct;

/* Scenario 4: SubmitExamAnswers — all wrong (choose a wrong option) */
DECLARE @AnswersWrong XML =
(
    SELECT 
        (SELECT TOP 5 
                eq.QuestionID AS [QuestionID],
                w.OptionID    AS [ChosenOptionID]
         FROM Exam_Question eq
         JOIN ModelAnswer   ma ON ma.QuestionID = eq.QuestionID
         CROSS APPLY (
            SELECT TOP 1 o.OptionID
            FROM [Option] o 
            WHERE o.QuestionID = eq.QuestionID AND o.OptionID <> ma.OptionID
            ORDER BY o.OptionID
         ) w
         WHERE eq.ExamID = @ExamID
         ORDER BY eq.OrderNo
         FOR XML PATH('Answer'), TYPE)
    FOR XML PATH('Answers')
);

DECLARE @StartTime2 DATETIME = DATEADD(MINUTE, -40, GETDATE());
DECLARE @EndTime2   DATETIME = GETDATE();
EXEC SubmitExamAnswers 
    @StudentID = 2, 
    @ExamID = @ExamID, 
    @StartTime = @StartTime2,
    @EndTime   = @EndTime2,
    @Answers   = @AnswersWrong;

DECLARE @SE_Wrong INT;
SELECT @SE_Wrong = MAX(StudentExamID)
FROM StudentExam
WHERE StudentID = 2 AND ExamID = @ExamID;

EXEC CorrectExam @StudentExamID = @SE_Wrong;

/* Scenario 5: Reports */
EXEC Report_StudentGrades        @StudentID    = 1;
EXEC Report_InstructorCourses    @InstructorID = 1;  -- adjust if needed
EXEC Report_StudentsByDepartment @DepartmentNo = 1;  -- adjust if needed

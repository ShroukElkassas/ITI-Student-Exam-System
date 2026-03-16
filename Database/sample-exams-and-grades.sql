USE ITI_ExaminationDB;

SET NOCOUNT ON;

DECLARE @CourseId INT = 1;

DECLARE @ExamMidtermId INT;
DECLARE @ExamFinalId INT;

SELECT @ExamMidtermId = ExamID
FROM Exam
WHERE ExamName = N'SQL Fundamentals - Midterm' AND CourseID = @CourseId;

IF @ExamMidtermId IS NULL
BEGIN
    INSERT INTO Exam (ExamName, CourseID, TotalQuestions)
    VALUES (N'SQL Fundamentals - Midterm', @CourseId, 20);
    SET @ExamMidtermId = SCOPE_IDENTITY();
END

SELECT @ExamFinalId = ExamID
FROM Exam
WHERE ExamName = N'SQL Fundamentals - Final' AND CourseID = @CourseId;

IF @ExamFinalId IS NULL
BEGIN
    INSERT INTO Exam (ExamName, CourseID, TotalQuestions)
    VALUES (N'SQL Fundamentals - Final', @CourseId, 20);
    SET @ExamFinalId = SCOPE_IDENTITY();
END

INSERT INTO Exam_Question (ExamID, QuestionID, OrderNo)
SELECT @ExamMidtermId, v.QuestionID, v.OrderNo
FROM (VALUES
    (1, 1), (2, 2), (3, 3), (4, 4), (5, 5),
    (6, 6), (7, 7), (8, 8), (9, 9), (10, 10),
    (11, 11), (12, 12), (13, 13), (14, 14), (15, 15),
    (16, 16), (17, 17), (18, 18), (19, 19), (20, 20)
) v(OrderNo, QuestionID)
WHERE NOT EXISTS (
    SELECT 1 FROM Exam_Question eq
    WHERE eq.ExamID = @ExamMidtermId AND eq.QuestionID = v.QuestionID
);

INSERT INTO Exam_Question (ExamID, QuestionID, OrderNo)
SELECT @ExamFinalId, v.QuestionID, v.OrderNo
FROM (VALUES
    (1, 21), (2, 22), (3, 23), (4, 24), (5, 25),
    (6, 26), (7, 27), (8, 28), (9, 29), (10, 30),
    (11, 31), (12, 32), (13, 33), (14, 34), (15, 35),
    (16, 36), (17, 37), (18, 38), (19, 39), (20, 40)
) v(OrderNo, QuestionID)
WHERE NOT EXISTS (
    SELECT 1 FROM Exam_Question eq
    WHERE eq.ExamID = @ExamFinalId AND eq.QuestionID = v.QuestionID
);

DECLARE @StudentExamId INT;

DECLARE @Start DATETIME = DATEADD(DAY, -7, GETDATE());
DECLARE @End DATETIME = DATEADD(MINUTE, 60, @Start);

IF NOT EXISTS (SELECT 1 FROM StudentExam WHERE StudentID = 1 AND ExamID = @ExamMidtermId)
BEGIN
    INSERT INTO StudentExam (StudentID, ExamID, StartTime, EndTime, TotalGrade)
    VALUES (1, @ExamMidtermId, @Start, @End, 85);
END

IF NOT EXISTS (SELECT 1 FROM StudentExam WHERE StudentID = 2 AND ExamID = @ExamMidtermId)
BEGIN
    INSERT INTO StudentExam (StudentID, ExamID, StartTime, EndTime, TotalGrade)
    VALUES (2, @ExamMidtermId, DATEADD(MINUTE, 10, @Start), DATEADD(MINUTE, 70, @Start), 72);
END

IF NOT EXISTS (SELECT 1 FROM StudentExam WHERE StudentID = 3 AND ExamID = @ExamMidtermId)
BEGIN
    INSERT INTO StudentExam (StudentID, ExamID, StartTime, EndTime, TotalGrade)
    VALUES (3, @ExamMidtermId, DATEADD(MINUTE, 20, @Start), DATEADD(MINUTE, 80, @Start), 64);
END

IF NOT EXISTS (SELECT 1 FROM StudentExam WHERE StudentID = 4 AND ExamID = @ExamMidtermId)
BEGIN
    INSERT INTO StudentExam (StudentID, ExamID, StartTime, EndTime, TotalGrade)
    VALUES (4, @ExamMidtermId, DATEADD(MINUTE, 30, @Start), DATEADD(MINUTE, 90, @Start), 93);
END

IF NOT EXISTS (SELECT 1 FROM StudentExam WHERE StudentID = 5 AND ExamID = @ExamMidtermId)
BEGIN
    INSERT INTO StudentExam (StudentID, ExamID, StartTime, EndTime, TotalGrade)
    VALUES (5, @ExamMidtermId, DATEADD(MINUTE, 40, @Start), DATEADD(MINUTE, 100, @Start), 58);
END

IF NOT EXISTS (SELECT 1 FROM StudentExam WHERE StudentID = 1 AND ExamID = @ExamFinalId)
BEGIN
    INSERT INTO StudentExam (StudentID, ExamID, StartTime, EndTime, TotalGrade)
    VALUES (1, @ExamFinalId, DATEADD(DAY, -2, @Start), DATEADD(MINUTE, 70, DATEADD(DAY, -2, @Start)), 88);
END

IF NOT EXISTS (SELECT 1 FROM StudentExam WHERE StudentID = 2 AND ExamID = @ExamFinalId)
BEGIN
    INSERT INTO StudentExam (StudentID, ExamID, StartTime, EndTime, TotalGrade)
    VALUES (2, @ExamFinalId, DATEADD(DAY, -2, DATEADD(MINUTE, 10, @Start)), DATEADD(MINUTE, 80, DATEADD(DAY, -2, @Start)), 76);
END

IF NOT EXISTS (SELECT 1 FROM StudentExam WHERE StudentID = 3 AND ExamID = @ExamFinalId)
BEGIN
    INSERT INTO StudentExam (StudentID, ExamID, StartTime, EndTime, TotalGrade)
    VALUES (3, @ExamFinalId, DATEADD(DAY, -2, DATEADD(MINUTE, 20, @Start)), DATEADD(MINUTE, 90, DATEADD(DAY, -2, @Start)), 67);
END

SELECT TOP 1 @StudentExamId = StudentExamID
FROM StudentExam
WHERE StudentID = 1 AND ExamID = @ExamMidtermId
ORDER BY StudentExamID DESC;

IF @StudentExamId IS NOT NULL
BEGIN
    INSERT INTO StudentAnswer (StudentExamID, QuestionID, ChosenOptionID)
    SELECT @StudentExamId, v.QuestionID, v.ChosenOptionID
    FROM (VALUES
        (1, 1),
        (2, 5),
        (3, 10),
        (4, 16),
        (5, 18)
    ) v(QuestionID, ChosenOptionID)
    WHERE NOT EXISTS (
        SELECT 1 FROM StudentAnswer sa
        WHERE sa.StudentExamID = @StudentExamId AND sa.QuestionID = v.QuestionID
    );
END


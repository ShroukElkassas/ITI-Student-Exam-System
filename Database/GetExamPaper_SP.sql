USE ITI_ExaminationDB;
GO

CREATE OR ALTER PROCEDURE dbo.GetExamPaper
    @ExamID INT
AS
BEGIN
    SET NOCOUNT ON;

    IF @ExamID IS NULL
    BEGIN
        RAISERROR('ExamID is required.', 16, 1);
        RETURN;
    END

    SELECT
        eq.OrderNo,
        q.QuestionID,
        q.CourseID,
        q.QuestionText,
        q.QuestionType,
        q.Points,
        o.OptionID,
        o.OptionText,
        o.OptionOrder
    FROM Exam_Question eq
    JOIN Question q ON q.QuestionID = eq.QuestionID
    LEFT JOIN [Option] o ON o.QuestionID = q.QuestionID
    WHERE eq.ExamID = @ExamID
    ORDER BY eq.OrderNo, o.OptionOrder, o.OptionID;
END;
GO


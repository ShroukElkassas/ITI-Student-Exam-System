
CREATE PROCEDURE InsertQuestion
    @CourseID INT, 
    @Text NVARCHAR(MAX), 
    @Type NVARCHAR(10), 
    @Points INT
AS
BEGIN
    IF @Type IN ('MCQ', 'TF')
    BEGIN
        INSERT INTO Question (CourseID, QuestionText, QuestionType, Points)
        VALUES (@CourseID, @Text, @Type, @Points);
    END
    ELSE
    BEGIN
        RAISERROR('Type must be MCQ or TF', 16, 1);
    END
END;
GO




CREATE PROCEDURE UpdateQuestion
    @ID INT, 
    @Text NVARCHAR(MAX), 
    @Points INT
AS
BEGIN
    UPDATE Question 
    SET QuestionText = @Text, Points = @Points
    WHERE QuestionID = @ID;
END;
GO




CREATE PROCEDURE DeleteQuestion @ID INT
AS
BEGIN
    DELETE FROM Question WHERE QuestionID = @ID;
END;
GO
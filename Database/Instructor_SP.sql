

CREATE OR ALTER PROCEDURE InsertInstructor
    @Name NVARCHAR(100), 
    @Email NVARCHAR(255), 
    @DeptNo INT
AS
BEGIN
    BEGIN TRANSACTION;
    INSERT INTO Instructor (InstructorName, Email, DepartmentNo)
    VALUES (@Name, @Email, @DeptNo);
    COMMIT TRANSACTION;
END;
GO




CREATE OR ALTER PROCEDURE UpdateInstructor
    @ID INT, 
    @Name NVARCHAR(100), 
    @Email NVARCHAR(255), 
    @DeptNo INT
AS
BEGIN
    UPDATE Instructor 
    SET InstructorName = @Name, Email = @Email, DepartmentNo = @DeptNo
    WHERE InstructorID = @ID;
END;
GO




CREATE OR ALTER PROCEDURE AssignInstructorToCourse
    @InstID INT, 
    @CourseID INT
AS
BEGIN
    INSERT INTO Instructor_Course (InstructorID, CourseID)
    VALUES (@InstID, @CourseID);
END;
GO


CREATE OR ALTER PROCEDURE DeleteInstructorCourse
    @InstructorID INT,
    @CourseID INT
AS
BEGIN
    SET NOCOUNT ON;

    IF @InstructorID IS NULL OR @CourseID IS NULL
    BEGIN
        RAISERROR('InstructorID and CourseID are required.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Instructor_Course WHERE InstructorID = @InstructorID AND CourseID = @CourseID)
    BEGIN
        RAISERROR('Mapping not found.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        DELETE FROM Instructor_Course
        WHERE InstructorID = @InstructorID AND CourseID = @CourseID;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE OR ALTER PROCEDURE SelectInstructorCourse
    @InstructorID INT = NULL,
    @CourseID INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    SELECT InstructorID, CourseID
    FROM Instructor_Course
    WHERE (@InstructorID IS NULL OR InstructorID = @InstructorID)
      AND (@CourseID IS NULL OR CourseID = @CourseID);
END;
GO
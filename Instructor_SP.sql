

CREATE PROCEDURE InsertInstructor
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




CREATE PROCEDURE UpdateInstructor
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




CREATE PROCEDURE AssignInstructorToCourse
    @InstID INT, 
    @CourseID INT
AS
BEGIN
    INSERT INTO Instructor_Course (InstructorID, CourseID)
    VALUES (@InstID, @CourseID);
END;
GO
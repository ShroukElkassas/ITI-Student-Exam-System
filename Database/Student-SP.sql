
CREATE PROCEDURE InsertStudent
    @Name NVARCHAR(100), 
    @Email NVARCHAR(255), 
    @Phone NVARCHAR(20)
AS
BEGIN
    INSERT INTO Student (StudentName, Email, Phone)
    VALUES (@Name, @Email, @Phone);
END;
GO




CREATE PROCEDURE UpdateStudent
    @ID INT, 
    @Name NVARCHAR(100), 
    @Email NVARCHAR(255), 
    @Phone NVARCHAR(20)
AS
BEGIN
    UPDATE Student 
    SET StudentName = @Name, Email = @Email, Phone = @Phone
    WHERE StudentID = @ID;
END;
GO




CREATE PROCEDURE DeleteStudent @ID INT
AS
BEGIN
    DELETE FROM Student WHERE StudentID = @ID;
END;
GO




CREATE PROCEDURE AssignStudentToTrack
    @StudentID INT, 
    @TrackID INT
AS
BEGIN
    INSERT INTO Student_Track (StudentID, TrackID)
    VALUES (@StudentID, @TrackID);
END;
GO
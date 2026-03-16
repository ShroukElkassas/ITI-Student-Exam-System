USE ITI_ExaminationDB;
GO

CREATE OR ALTER PROCEDURE dbo.SelectStudentWithTrack
    @StudentID INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        s.StudentID,
        s.StudentName,
        s.Email,
        s.Phone,
        st.TrackID,
        t.TrackName
    FROM Student s
    OUTER APPLY (
        SELECT TOP 1 TrackID
        FROM Student_Track
        WHERE StudentID = s.StudentID
        ORDER BY TrackID
    ) st
    LEFT JOIN Track t ON t.TrackID = st.TrackID
    WHERE (@StudentID IS NULL OR s.StudentID = @StudentID);
END;
GO


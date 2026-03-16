USE ITI_ExaminationDB;
GO

SET NOCOUNT ON;

IF NOT EXISTS (SELECT 1 FROM Track)
BEGIN
    RAISERROR('No tracks found. Insert tracks before assigning students.', 16, 1);
    RETURN;
END

;WITH StudentsWithoutTrack AS (
    SELECT s.StudentID
    FROM Student s
    WHERE NOT EXISTS (
        SELECT 1
        FROM Student_Track st
        WHERE st.StudentID = s.StudentID
    )
)
INSERT INTO Student_Track (StudentID, TrackID)
SELECT s.StudentID, t.TrackID
FROM StudentsWithoutTrack s
CROSS APPLY (
    SELECT TOP 1 TrackID
    FROM Track
    ORDER BY NEWID()
) t;

SELECT
    (SELECT COUNT(*) FROM Student) AS TotalStudents,
    (SELECT COUNT(DISTINCT StudentID) FROM Student_Track) AS StudentsWithTrack,
    (SELECT COUNT(*) FROM Student_Track) AS TotalStudentTrackRows;
GO


-- 1. تقرير الطلاب حسب القسم
-- [REQ-12]
CREATE PROCEDURE Report_StudentsByDepartment
    @DepartmentNo INT
AS
BEGIN
    SELECT 
        S.StudentID, 
        S.StudentName AS Name, 
        S.Email, 
        S.Phone, 
        T.TrackName, 
        B.BranchName 
    FROM Student S
    JOIN Student_Track ST ON S.StudentID = ST.StudentID
    JOIN Track T ON ST.TrackID = T.TrackID
    JOIN Branch B ON T.BranchID = B.BranchID
    JOIN Instructor I ON I.DepartmentNo = @DepartmentNo -- الربط بالقسم المطلوب
    GROUP BY S.StudentID, S.StudentName, S.Email, S.Phone, T.TrackName, B.BranchName;
END;
GO

-- 2. تقرير درجات الطالب في جميع الكورسات
-- [REQ-13]
CREATE PROCEDURE Report_StudentGrades
    @StudentID INT
AS
BEGIN
    SELECT 
        C.CourseName, 
        E.ExamName, 
        SE.TotalGrade, 
        C.MaxDegree,
        -- حساب النسبة المئوية داخل SQL كما هو مطلوب 
        (CAST(SE.TotalGrade AS FLOAT) / NULLIF(C.MaxDegree, 0)) * 100 AS [Percentage*] 
    FROM StudentExam SE
    JOIN Exam E ON SE.ExamID = E.ExamID
    JOIN Course C ON E.CourseID = C.CourseID
    WHERE SE.StudentID = @StudentID;
END;
GO

-- 3. تقرير الكورسات التي يدرسها المحاضر وعدد الطلاب
-- [REQ-14]
CREATE PROCEDURE Report_InstructorCourses
    @InstructorID INT
AS
BEGIN
    SELECT 
        C.CourseName, 
        T.TrackName, 
        COUNT(ST.StudentID) AS StudentCount 
    FROM Instructor_Course IC
    JOIN Course C ON IC.CourseID = C.CourseID
    JOIN Track_Course TC ON C.CourseID = TC.CourseID
    JOIN Track T ON TC.TrackID = T.TrackID
    LEFT JOIN Student_Track ST ON T.TrackID = ST.TrackID -- لعد الطلاب في هذا التراك/الكورس
    WHERE IC.InstructorID = @InstructorID 
    GROUP BY C.CourseName, T.TrackName;
END;
GO


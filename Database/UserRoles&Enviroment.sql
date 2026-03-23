--lgines
IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = 'ExamAdmin')
BEGIN
    CREATE LOGIN ExamAdmin WITH PASSWORD = 'Admin@123';
END

IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = 'InstructorUser')
BEGIN
    CREATE LOGIN InstructorUser WITH PASSWORD = 'Instructor@123';
END

IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = 'StudentUser')
BEGIN
    CREATE LOGIN StudentUser WITH PASSWORD = 'Student@123';
END


USE ITI_ExaminationDB
GO

IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'AdminUser')
BEGIN
    CREATE USER AdminUser FOR LOGIN ExamAdmin;
END

IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'InstructorUser')
BEGIN
    CREATE USER InstructorUser FOR LOGIN InstructorUser;
END

IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'StudentUser')
BEGIN
    CREATE USER StudentUser FOR LOGIN StudentUser;
END

-- give roles for admin 
IF NOT EXISTS (
    SELECT 1
    FROM sys.database_role_members rm
    JOIN sys.database_principals r ON rm.role_principal_id = r.principal_id
    JOIN sys.database_principals m ON rm.member_principal_id = m.principal_id
    WHERE r.name = 'db_owner' AND m.name = 'AdminUser'
)
BEGIN
    ALTER ROLE db_owner ADD MEMBER AdminUser;
END

--give roles for instractor
IF NOT EXISTS (
    SELECT 1
    FROM sys.database_role_members rm
    JOIN sys.database_principals r ON rm.role_principal_id = r.principal_id
    JOIN sys.database_principals m ON rm.member_principal_id = m.principal_id
    WHERE r.name = 'db_datareader' AND m.name = 'InstructorUser'
)
BEGIN
    ALTER ROLE db_datareader ADD MEMBER InstructorUser;
END

-- Write access ONLY on exam and question related tables
GRANT INSERT, UPDATE, DELETE ON Question      TO InstructorUser;
GRANT INSERT, UPDATE, DELETE ON [Option]      TO InstructorUser;
GRANT INSERT, UPDATE, DELETE ON ModelAnswer   TO InstructorUser;
GRANT INSERT, UPDATE, DELETE ON Exam          TO InstructorUser;
GRANT INSERT, UPDATE, DELETE ON Exam_Question  TO InstructorUser;
GO

-- ROLE 3: Student ? db_datareader (read only)

-- Read-only access on all tables
IF NOT EXISTS (
    SELECT 1
    FROM sys.database_role_members rm
    JOIN sys.database_principals r ON rm.role_principal_id = r.principal_id
    JOIN sys.database_principals m ON rm.member_principal_id = m.principal_id
    WHERE r.name = 'db_datareader' AND m.name = 'StudentUser'
)
BEGIN
    ALTER ROLE db_datareader ADD MEMBER StudentUser;
END
GO

-- Allow student to submit answers and view their own results
GRANT EXECUTE ON OBJECT::SubmitExamAnswers TO StudentUser;
GO

-- Deny access to sensitive tables (student cannot see other students' answers)
GRANT EXECUTE ON OBJECT::Report_StudentGrades TO StudentUser;
DENY SELECT ON ModelAnswer   TO StudentUser;
GO

GRANT EXECUTE ON OBJECT::dbo.SelectBranch TO StudentUser;
GRANT EXECUTE ON OBJECT::dbo.SelectTrackByBranch TO StudentUser;
GRANT EXECUTE ON OBJECT::dbo.SelectCourseByTrack TO StudentUser;
GRANT EXECUTE ON OBJECT::dbo.SelectExam TO StudentUser;
GRANT EXECUTE ON OBJECT::dbo.SelectStudent TO StudentUser;
GRANT EXECUTE ON OBJECT::dbo.SelectStudentWithTrack TO StudentUser;
GRANT EXECUTE ON OBJECT::dbo.GetExamPaper TO StudentUser;
GRANT EXECUTE ON OBJECT::dbo.Report_StudentGrades TO StudentUser;
GRANT EXECUTE ON OBJECT::dbo.SubmitExamAnswers TO StudentUser;
GO

GRANT EXECUTE ON OBJECT::dbo.SelectBranch TO InstructorUser;
GRANT EXECUTE ON OBJECT::dbo.SelectTrackByBranch TO InstructorUser;
GRANT EXECUTE ON OBJECT::dbo.SelectCourseByTrack TO InstructorUser;
GRANT EXECUTE ON OBJECT::dbo.Report_StudentsByDepartment TO InstructorUser;
GRANT EXECUTE ON OBJECT::dbo.Report_StudentGrades TO InstructorUser;
GRANT EXECUTE ON OBJECT::dbo.Report_InstructorCourses TO InstructorUser;
GRANT EXECUTE ON OBJECT::dbo.GenerateExam TO InstructorUser;
GRANT EXECUTE ON OBJECT::dbo.InsertExam TO InstructorUser;
GRANT EXECUTE ON OBJECT::dbo.UpdateExam TO InstructorUser;
GRANT EXECUTE ON OBJECT::dbo.DeleteExam TO InstructorUser;
GRANT EXECUTE ON OBJECT::dbo.SelectExam TO InstructorUser;
GRANT EXECUTE ON OBJECT::dbo.InsertExamQuestion TO InstructorUser;
GRANT EXECUTE ON OBJECT::dbo.UpdateExamQuestion TO InstructorUser;
GRANT EXECUTE ON OBJECT::dbo.DeleteExamQuestion TO InstructorUser;
GRANT EXECUTE ON OBJECT::dbo.SelectExamQuestion TO InstructorUser;
GRANT EXECUTE ON OBJECT::dbo.InsertQuestion TO InstructorUser;
GRANT EXECUTE ON OBJECT::dbo.UpdateQuestion TO InstructorUser;
GRANT EXECUTE ON OBJECT::dbo.DeleteQuestion TO InstructorUser;
GRANT EXECUTE ON OBJECT::dbo.SelectQuestion TO InstructorUser;
GRANT EXECUTE ON OBJECT::dbo.InsertOption TO InstructorUser;
GRANT EXECUTE ON OBJECT::dbo.UpdateOption TO InstructorUser;
GRANT EXECUTE ON OBJECT::dbo.DeleteOption TO InstructorUser;
GRANT EXECUTE ON OBJECT::dbo.SelectOption TO InstructorUser;
GRANT EXECUTE ON OBJECT::dbo.InsertModelAnswer TO InstructorUser;
GRANT EXECUTE ON OBJECT::dbo.UpdateModelAnswer TO InstructorUser;
GRANT EXECUTE ON OBJECT::dbo.DeleteModelAnswer TO InstructorUser;
GRANT EXECUTE ON OBJECT::dbo.SelectModelAnswer TO InstructorUser;
GRANT EXECUTE ON OBJECT::dbo.SetModelAnswer TO InstructorUser;
GO

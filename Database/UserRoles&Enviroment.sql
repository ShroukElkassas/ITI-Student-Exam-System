--lgines
CREATE LOGIN ExamAdmin 
WITH PASSWORD = 'Admin@123';

CREATE LOGIN InstructorUser 
WITH PASSWORD = 'Instructor@123';

CREATE LOGIN StudentUser 
WITH PASSWORD = 'Student@123';


USE ITI_ExaminationDB
GO

CREATE USER AdminUser 
FOR LOGIN ExamAdmin;

CREATE USER InstructorUser 
FOR LOGIN InstructorUser;

CREATE USER StudentUser 
FOR LOGIN StudentUser;

-- give roles for admin 
ALTER ROLE db_owner 
ADD MEMBER AdminUser;

--give roles for instractor
ALTER ROLE db_datareader 
ADD MEMBER InstructorUser;

-- Write access ONLY on exam and question related tables
GRANT INSERT, UPDATE, DELETE ON Question      TO InstructorUser;
GRANT INSERT, UPDATE, DELETE ON [Option]      TO InstructorUser;
GRANT INSERT, UPDATE, DELETE ON ModelAnswer   TO InstructorUser;
GRANT INSERT, UPDATE, DELETE ON Exam          TO InstructorUser;
GRANT INSERT, UPDATE, DELETE ON Exam_Question  TO InstructorUser;
GO

-- ROLE 3: Student ? db_datareader (read only)

-- Read-only access on all tables
ALTER ROLE db_datareader ADD MEMBER StudentUser;
GO

-- Allow student to submit answers and view their own results
GRANT INSERT ON StudentExam   TO StudentUser;
GRANT INSERT ON StudentAnswer TO StudentUser;
GO

-- Deny access to sensitive tables (student cannot see other students' answers)
DENY SELECT ON StudentAnswer TO StudentUser;
DENY SELECT ON ModelAnswer   TO StudentUser;
GO
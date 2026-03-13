


-- SECTION 1: INSERT BRANCHES (3 branches)
-- ============================================================================
INSERT INTO Branch (BranchName, Location) VALUES 
    (N'Cairo Branch', N'Cairo, Egypt'),
    (N'Alexandria Branch', N'Alexandria, Egypt'),
    (N'Giza Branch', N'Giza, Egypt');
GO


-- SECTION 2: INSERT TRACKS (2+ per branch)
-- ============================================================================
INSERT INTO Track (TrackName, BranchID, DurationMonths) VALUES 
    (N'Web Development', 1, 6),
    (N'Database Administration', 1, 5),
    (N'Mobile Development', 2, 6),
    (N'Cloud Computing', 2, 5),
    (N'Data Science', 3, 7),
    (N'Cybersecurity', 3, 6);
GO

-- SECTION 3: INSERT COURSES (5+ courses)
-- ============================================================================
INSERT INTO Course (CourseName, MinDegree, MaxDegree) VALUES 
    (N'SQL Server Fundamentals', 40, 100),
    (N'Advanced SQL Queries', 40, 100),
    (N'Database Design', 40, 100),
    (N'C# Programming', 40, 100),
    (N'Web Development with ASP.NET', 40, 100),
    (N'JavaScript Essentials', 40, 100),
    (N'Python for Data Science', 40, 100);
GO


-- SECTION 4: LINK TRACKS TO COURSES
-- ============================================================================
INSERT INTO Track_Course (TrackID, CourseID) VALUES 
    (1, 4), (1, 5), (1, 6),  -- Web Development
    (2, 1), (2, 2), (2, 3),  -- Database Administration
    (3, 4), (3, 6),          -- Mobile Development
    (4, 1), (4, 2),          -- Cloud Computing
    (5, 7), (5, 2),          -- Data Science
    (6, 1), (6, 2);          -- Cybersecurity
GO


-- SECTION 5: INSERT INSTRUCTORS (3+ instructors)
-- ============================================================================
INSERT INTO Instructor (InstructorName, Email, DepartmentNo) VALUES 
    (N'Dr. Ahmed Hassan', N'ahmed.hassan@iti.edu.eg', 1),
    (N'Eng. Fatima Mohamed', N'fatima.mohamed@iti.edu.eg', 2),
    (N'Prof. Karim Ibrahim', N'karim.ibrahim@iti.edu.eg', 1),
    (N'Ms. Layla Saleh', N'layla.saleh@iti.edu.eg', 3),
    (N'Mr. Hassan Ali', N'hassan.ali@iti.edu.eg', 2);
GO


-- SECTION 6: ASSIGN INSTRUCTORS TO COURSES (2+ courses per instructor)
-- ============================================================================
INSERT INTO Instructor_Course (InstructorID, CourseID) VALUES 
    (1, 1), (1, 2), (1, 3),        -- Dr. Ahmed Hassan teaches SQL courses
    (2, 4), (2, 5),                -- Eng. Fatima Mohamed teaches C# and ASP.NET
    (3, 6), (3, 7),                -- Prof. Karim Ibrahim teaches JavaScript and Python
    (4, 1), (4, 2),                -- Ms. Layla Saleh teaches SQL courses
    (5, 4), (5, 5);                -- Mr. Hassan Ali teaches C# and ASP.NET
GO


-- SECTION 7: INSERT STUDENTS (20+ students)
-- ============================================================================
INSERT INTO Student (StudentName, Email, Phone) VALUES 
    (N'Mohamed Ali', N'student1@iti.edu.eg', N'01001234567'),
    (N'Fatma Ahmed', N'student2@iti.edu.eg', N'01001234568'),
    (N'Ali Mahmoud', N'student3@iti.edu.eg', N'01001234569'),
    (N'Sara Hassan', N'student4@iti.edu.eg', N'01001234570'),
    (N'Ahmed Ibrahim', N'student5@iti.edu.eg', N'01001234571'),
    (N'Layla Omar', N'student6@iti.edu.eg', N'01001234572'),
    (N'Mahmoud Salem', N'student7@iti.edu.eg', N'01001234573'),
    (N'Nour Khaled', N'student8@iti.edu.eg', N'01001234574'),
    (N'Hassan Mohamed', N'student9@iti.edu.eg', N'01001234575'),
    (N'Maryam Yassin', N'student10@iti.edu.eg', N'01001234576'),
    (N'Omar Farouk', N'student11@iti.edu.eg', N'01001234577'),
    (N'Dina Osama', N'student12@iti.edu.eg', N'01001234578'),
    (N'Khaled Hussein', N'student13@iti.edu.eg', N'01001234579'),
    (N'Rana Mahmoud', N'student14@iti.edu.eg', N'01001234580'),
    (N'Ibrahim Ali', N'student15@iti.edu.eg', N'01001234581'),
    (N'Yasmine Ahmed', N'student16@iti.edu.eg', N'01001234582'),
    (N'Sami Mohamed', N'student17@iti.edu.eg', N'01001234583'),
    (N'Hana Hassan', N'student18@iti.edu.eg', N'01001234584'),
    (N'Tarek Ibrahim', N'student19@iti.edu.eg', N'01001234585'),
    (N'Reem Ali', N'student20@iti.edu.eg', N'01001234586');
GO


-- SECTION 8: ASSIGN STUDENTS TO TRACKS
-- ============================================================================
INSERT INTO Student_Track (StudentID, TrackID) VALUES 
    (1, 1), (2, 1), (3, 1), (4, 1), (5, 1),     -- Track 1: Web Development
    (6, 2), (7, 2), (8, 2), (9, 2), (10, 2),   -- Track 2: Database Administration
    (11, 3), (12, 3), (13, 3), (14, 3),        -- Track 3: Mobile Development
    (15, 4), (16, 4), (17, 4),                 -- Track 4: Cloud Computing
    (18, 5), (19, 5), (20, 5);                 -- Track 5: Data Science
GO


-- SECTION 9: MCQ QUESTIONS — 32 Questions for CourseID = 1
-- ============================================================================
INSERT INTO Question (CourseID, QuestionText, QuestionType, Points) VALUES
-- Q1
(1, N'What is a primary key in a database table?', N'MCQ', 5),
-- Q2
(1, N'Which SQL keyword is used to retrieve data from a table?', N'MCQ', 5),
-- Q3
(1, N'What does the WHERE clause do in SQL?', N'MCQ', 5),
-- Q4
(1, N'Which of the following is a valid SQL Server data type?', N'MCQ', 5),
-- Q5
(1, N'What is the purpose of the JOIN clause?', N'MCQ', 5),
-- Q6
(1, N'Which aggregate function returns the number of rows?', N'MCQ', 5),
-- Q7
(1, N'What does the DISTINCT keyword do in a SELECT statement?', N'MCQ', 5),
-- Q8
(1, N'Which statement is used to insert new data into a table?', N'MCQ', 5),
-- Q9
(1, N'What is a foreign key?', N'MCQ', 5),
-- Q10
(1, N'Which SQL keyword sorts results in descending order?', N'MCQ', 5),
-- Q11
(1, N'What is normalization in database design?', N'MCQ', 5),
-- Q12
(1, N'Which clause is used to group rows with the same values?', N'MCQ', 5),
-- Q13
(1, N'What does the HAVING clause do?', N'MCQ', 5),
-- Q14
(1, N'Which keyword removes duplicate rows from query results?', N'MCQ', 5),
-- Q15
(1, N'What is an index in SQL Server?', N'MCQ', 5),
-- Q16
(1, N'Which type of JOIN returns only matching rows from both tables?', N'MCQ', 5),
-- Q17
(1, N'What does the TOP keyword do in SQL Server?', N'MCQ', 5),
-- Q18
(1, N'Which function returns the current date and time in SQL Server?', N'MCQ', 5),
-- Q19
(1, N'What is a stored procedure?', N'MCQ', 5),
-- Q20
(1, N'Which constraint ensures a column cannot contain NULL values?', N'MCQ', 5),
-- Q21
(1, N'What does the TRUNCATE TABLE statement do?', N'MCQ', 5),
-- Q22
(1, N'Which SQL clause is used to filter results after GROUP BY?', N'MCQ', 5),
-- Q23
(1, N'What is the difference between DELETE and TRUNCATE?', N'MCQ', 5),
-- Q24
(1, N'Which operator is used for pattern matching in SQL?', N'MCQ', 5),
-- Q25
(1, N'What is a view in SQL Server?', N'MCQ', 5),
-- Q26
(1, N'Which function returns the largest value in a column?', N'MCQ', 5),
-- Q27
(1, N'What does the COALESCE function do?', N'MCQ', 5),
-- Q28
(1, N'Which join type returns all rows from the left table and matching rows from the right?', N'MCQ', 5),
-- Q29
(1, N'What is the purpose of the IDENTITY property in SQL Server?', N'MCQ', 5),
-- Q30
(1, N'Which statement is used to modify existing data in a table?', N'MCQ', 5),
-- Q31
(1, N'What does the UNION operator do?', N'MCQ', 5),
-- Q32
(1, N'Which system function returns the name of the current database?', N'MCQ', 5);
GO


-- SECTION 10: OPTIONS FOR MCQ QUESTIONS 
-- ============================================================================
INSERT INTO [Option] (QuestionID, OptionText, OptionOrder) VALUES
-- Q1
(1, N'A unique identifier for each row in a table', 1),
(1, N'A column that stores large text data', 2),
(1, N'A backup of the database', 3),
(1, N'A temporary table used for calculations', 4),
-- Q2
(2, N'SELECT', 1),
(2, N'RETRIEVE', 2),
(2, N'FETCH', 3),
(2, N'GET', 4),
-- Q3
(3, N'Filters rows based on a condition', 1),
(3, N'Sorts the data alphabetically', 2),
(3, N'Groups similar records together', 3),
(3, N'Joins multiple tables', 4),
-- Q4
(4, N'INT', 1),
(4, N'NVARCHAR', 2),
(4, N'DATETIME', 3),
(4, N'All of the above', 4),
-- Q5
(5, N'Combines rows from two or more tables based on a condition', 1),
(5, N'Deletes duplicate rows', 2),
(5, N'Sorts data in ascending order', 3),
(5, N'Filters rows based on aggregate functions', 4),
-- Q6
(6, N'COUNT()', 1),
(6, N'SUM()', 2),
(6, N'AVG()', 3),
(6, N'MAX()', 4),
-- Q7
(7, N'Removes duplicate rows from results', 1),
(7, N'Sorts data in descending order', 2),
(7, N'Filters rows based on conditions', 3),
(7, N'Groups similar records together', 4),
-- Q8
(8, N'INSERT', 1),
(8, N'ADD', 2),
(8, N'APPEND', 3),
(8, N'CREATE', 4),
-- Q9
(9, N'A key that references a primary key in another table', 1),
(9, N'A key that uniquely identifies each row', 2),
(9, N'A key used for sorting data', 3),
(9, N'A key used for backup purposes', 4),
-- Q10
(10, N'DESC', 1),
(10, N'REVERSE', 2),
(10, N'DOWN', 3),
(10, N'SORT', 4),
-- Q11
(11, N'Organizing data to reduce redundancy and improve integrity', 1),
(11, N'Creating backups of the database', 2),
(11, N'Compressing data to save disk space', 3),
(11, N'Encrypting sensitive data', 4),
-- Q12
(12, N'GROUP BY', 1),
(12, N'ORDER BY', 2),
(12, N'WHERE', 3),
(12, N'DISTINCT', 4),
-- Q13
(13, N'Filters groups based on aggregate conditions', 1),
(13, N'Sorts data in ascending order', 2),
(13, N'Removes duplicate rows', 3),
(13, N'Joins multiple tables', 4),
-- Q14
(14, N'DISTINCT', 1),
(14, N'UNIQUE', 2),
(14, N'FILTER', 3),
(14, N'REMOVE', 4),
-- Q15
(15, N'A structure that speeds up data retrieval', 1),
(15, N'A backup copy of the table', 2),
(15, N'A temporary table for calculations', 3),
(15, N'A virtual view of the table', 4),
-- Q16
(16, N'INNER JOIN', 1),
(16, N'LEFT JOIN', 2),
(16, N'RIGHT JOIN', 3),
(16, N'FULL JOIN', 4),
-- Q17
(17, N'Limits the number of rows returned', 1),
(17, N'Returns the top aggregate value', 2),
(17, N'Sorts results in descending order', 3),
(17, N'Removes duplicate rows', 4),
-- Q18
(18, N'GETDATE()', 1),
(18, N'NOW()', 2),
(18, N'SYSDATE()', 3),
(18, N'CURDATE()', 4),
-- Q19
(19, N'A precompiled set of SQL statements stored in the database', 1),
(19, N'A temporary table used for calculations', 2),
(19, N'A backup procedure for the database', 3),
(19, N'A constraint that enforces data integrity', 4),
-- Q20
(20, N'NOT NULL', 1),
(20, N'UNIQUE', 2),
(20, N'DEFAULT', 3),
(20, N'CHECK', 4),
-- Q21
(21, N'Removes all rows from a table without logging individual row deletions', 1),
(21, N'Deletes only selected rows based on a condition', 2),
(21, N'Drops the table and recreates it', 3),
(21, N'Removes duplicate rows only', 4),
-- Q22
(22, N'HAVING', 1),
(22, N'WHERE', 2),
(22, N'FILTER', 3),
(22, N'BETWEEN', 4),
-- Q23
(23, N'DELETE can be rolled back; TRUNCATE cannot in most cases and resets identity', 1),
(23, N'TRUNCATE deletes selected rows; DELETE removes all rows', 2),
(23, N'They are identical in behavior', 3),
(23, N'DELETE is faster than TRUNCATE', 4),
-- Q24
(24, N'LIKE', 1),
(24, N'MATCH', 2),
(24, N'CONTAINS', 3),
(24, N'FIND', 4),
-- Q25
(25, N'A virtual table based on a SELECT query', 1),
(25, N'A permanent copy of a table stored separately', 2),
(25, N'A backup of the database', 3),
(25, N'A temporary table that exists only during a session', 4),
-- Q26
(26, N'MAX()', 1),
(26, N'TOP()', 2),
(26, N'UPPER()', 3),
(26, N'LARGE()', 4),
-- Q27
(27, N'Returns the first non-NULL value from a list of expressions', 1),
(27, N'Combines two strings together', 2),
(27, N'Converts a value to a different data type', 3),
(27, N'Rounds a number to the nearest integer', 4),
-- Q28
(28, N'LEFT JOIN', 1),
(28, N'RIGHT JOIN', 2),
(28, N'INNER JOIN', 3),
(28, N'FULL JOIN', 4),
-- Q29
(29, N'Automatically generates sequential unique integer values', 1),
(29, N'Enforces uniqueness on a column', 2),
(29, N'Creates a primary key automatically', 3),
(29, N'Prevents NULL values in a column', 4),
-- Q30
(30, N'UPDATE', 1),
(30, N'MODIFY', 2),
(30, N'CHANGE', 3),
(30, N'ALTER', 4),
-- Q31
(31, N'Combines the result sets of two SELECT statements removing duplicates', 1),
(31, N'Joins two tables based on a common column', 2),
(31, N'Returns only the matching rows from two tables', 3),
(31, N'Filters rows using a subquery', 4),
-- Q32
(32, N'DB_NAME()', 1),
(32, N'DATABASE()', 2),
(32, N'CURRENT_DB()', 3),
(32, N'GET_DATABASE()', 4);
GO


-- SECTION 11: MODEL ANSWERS FOR MCQ QUESTIONS (Q1–Q32)
-- ============================================================================
INSERT INTO ModelAnswer (QuestionID, OptionID) VALUES
(1, 1),
(2, 5),
(3, 9),
(4, 16),
(5, 17),
(6, 21),
(7, 25),
(8, 29),
(9, 33),
(10, 37),
(11, 41),
(12, 45),
(13, 49),
(14, 53),
(15, 57),
(16, 61),
(17, 65),
(18, 69),
(19, 73),
(20, 77),
(21, 81),
(22, 85),
(23, 89),
(24, 93),
(25, 97),
(26, 101),
(27, 105),
(28, 109),
(29, 113),
(30, 117),
(31, 121),
(32, 125);
GO

-- SECTION 12: TRUE/FALSE QUESTIONS — 22 Questions for CourseID = 1
-- ============================================================================
INSERT INTO Question (CourseID, QuestionText, QuestionType, Points) VALUES
-- Q33
(1, N'A NULL value is the same as an empty string in SQL Server.', N'TF', 5),
-- Q34
(1, N'The UNION operator combines rows from multiple SELECT statements.', N'TF', 5),
-- Q35
(1, N'A view is a permanent physical table stored in the database.', N'TF', 5),
-- Q36
(1, N'The LIKE operator is used for pattern matching in SQL.', N'TF', 5),
-- Q37
(1, N'A transaction can be rolled back using the ROLLBACK statement.', N'TF', 5),
-- Q38
(1, N'The CROSS JOIN returns the Cartesian product of two tables.', N'TF', 5),
-- Q39
(1, N'An index always improves query performance regardless of the situation.', N'TF', 5),
-- Q40
(1, N'The IDENTITY property automatically generates unique sequential values.', N'TF', 5),
-- Q41
(1, N'A stored procedure can return multiple result sets.', N'TF', 5),
-- Q42
(1, N'The CHECK constraint ensures data integrity at the column level.', N'TF', 5),
-- Q43
(1, N'A view can contain data from multiple tables.', N'TF', 5),
-- Q44
(1, N'The CASE statement is used for conditional logic in SQL.', N'TF', 5),
-- Q45
(1, N'A trigger is executed automatically when a specified event occurs.', N'TF', 5),
-- Q46
(1, N'The BETWEEN operator includes both the lower and upper boundary values.', N'TF', 5),
-- Q47
(1, N'A cursor is used to process rows one at a time in SQL Server.', N'TF', 5),
-- Q48
(1, N'The TRUNCATE statement can be rolled back inside a transaction.', N'TF', 5),
-- Q49
(1, N'A primary key column automatically has a UNIQUE constraint.', N'TF', 5),
-- Q50
(1, N'The ORDER BY clause can only sort data in ascending order.', N'TF', 5),
-- Q51
(1, N'SQL Server allows multiple NULL values in a UNIQUE constrained column.', N'TF', 5),
-- Q52
(1, N'A subquery can be used inside a WHERE clause.', N'TF', 5),
-- Q53
(1, N'The FULL OUTER JOIN returns all rows from both tables including non-matching rows.', N'TF', 5),
-- Q54
(1, N'Indexes slow down INSERT and UPDATE operations because they must be maintained.', N'TF', 5);
GO


-- SECTION 13: OPTIONS FOR TRUE/FALSE QUESTIONS (22 × 2 = 44 options)
-- ============================================================================
INSERT INTO [Option] (QuestionID, OptionText, OptionOrder) VALUES
(33, N'True', 1), (33, N'False', 2),
(34, N'True', 1), (34, N'False', 2),
(35, N'True', 1), (35, N'False', 2),
(36, N'True', 1), (36, N'False', 2),
(37, N'True', 1), (37, N'False', 2),
(38, N'True', 1), (38, N'False', 2),
(39, N'True', 1), (39, N'False', 2),
(40, N'True', 1), (40, N'False', 2),
(41, N'True', 1), (41, N'False', 2),
(42, N'True', 1), (42, N'False', 2),
(43, N'True', 1), (43, N'False', 2),
(44, N'True', 1), (44, N'False', 2),
(45, N'True', 1), (45, N'False', 2),
(46, N'True', 1), (46, N'False', 2),
(47, N'True', 1), (47, N'False', 2),
(48, N'True', 1), (48, N'False', 2),
(49, N'True', 1), (49, N'False', 2),
(50, N'True', 1), (50, N'False', 2),
(51, N'True', 1), (51, N'False', 2),
(52, N'True', 1), (52, N'False', 2),
(53, N'True', 1), (53, N'False', 2),
(54, N'True', 1), (54, N'False', 2);
GO


-- SECTION 14: MODEL ANSWERS FOR TRUE/FALSE QUESTIONS
-- ============================================================================
INSERT INTO ModelAnswer (QuestionID, OptionID) VALUES
(33, 130),
(34, 131),
(35, 136),
(36, 137),
(37, 139),
(38, 141),
(39, 144),
(40, 145),
(41, 147),
(42, 149),
(43, 151),
(44, 153),
(45, 155),
(46, 157),
(47, 159),
(48, 161),
(49, 163),
(50, 166),
(51, 167),
(52, 169),
(53, 171),
(54, 172);
GO
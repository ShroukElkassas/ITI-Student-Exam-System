

CREATE PROCEDURE GenerateExam 
    @CourseID INT,
    @ExamName NVARCHAR(150),
    @NumMCQ INT,
    @NumTF INT
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        -- 1. التأكد من توافر عدد كافٍ من الأسئلة في بنك الأسئلة 
        DECLARE @AvailableMCQ INT, @AvailableTF INT;
        
        SELECT @AvailableMCQ = COUNT(*) FROM Question 
        WHERE CourseID = @CourseID AND QuestionType = 'MCQ';
        
        SELECT @AvailableTF = COUNT(*) FROM Question 
        WHERE CourseID = @CourseID AND QuestionType = 'TF';

        IF (@AvailableMCQ < @NumMCQ OR @AvailableTF < @NumTF)
        BEGIN
            RAISERROR('Error: Not enough questions in the bank for this course.', 16, 1);
            ROLLBACK TRANSACTION;
            RETURN;
        END

        -- 2. إنشاء رأس الامتحان (Exam Header) 
        DECLARE @NewExamID INT;
        INSERT INTO Exam (ExamName, CourseID, CreatedDate, TotalQuestions)
        VALUES (@ExamName, @CourseID, GETDATE(), (@NumMCQ + @NumTF));

        SET @NewExamID = SCOPE_IDENTITY();

        -- 3. اختيار الأسئلة عشوائياً وإضافتها لجدول Exam_Question 
        -- نستخدم NEWID() لضمان العشوائية المطلوبة في المواصفات
        INSERT INTO Exam_Question (ExamID, QuestionID, OrderNo)
        SELECT @NewExamID, QuestionID, ROW_NUMBER() OVER (ORDER BY NEWID())
        FROM (
            -- اختيار أسئلة MCQ
            SELECT TOP (@NumMCQ) QuestionID 
            FROM Question 
            WHERE CourseID = @CourseID AND QuestionType = 'MCQ' 
            ORDER BY NEWID()
            
            UNION ALL
            
            -- اختيار أسئلة T/F
            SELECT TOP (@NumTF) QuestionID 
            FROM Question 
            WHERE CourseID = @CourseID AND QuestionType = 'TF' 
            ORDER BY NEWID()
        ) AS RandomQuestions;

        COMMIT TRANSACTION;
        PRINT 'Exam Generated Successfully!';
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        DECLARE @ErrMsg NVARCHAR(4000) = ERROR_MESSAGE();
        RAISERROR(@ErrMsg, 16, 1);
    END CATCH
END


--#####################################################################
--#####################################################################

CREATE PROCEDURE SubmitExamAnswers
    @StudentID INT,
    @ExamID INT,
    @StartTime DATETIME,
    @EndTime DATETIME,
    @Answers XML
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        -- 1. تسجيل محاولة الامتحان في جدول StudentExam 
        DECLARE @StudentExamID INT;
        INSERT INTO StudentExam (StudentID, ExamID, StartTime, EndTime, TotalGrade)
        VALUES (@StudentID, @ExamID, @StartTime, @EndTime, 0);

        SET @StudentExamID = SCOPE_IDENTITY();
        INSERT INTO StudentAnswer (StudentExamID, QuestionID, ChosenOptionID)
        SELECT 
            @StudentExamID,
            T.Item.value('(QuestionID)[1]', 'INT'),
            T.Item.value('(ChosenOptionID)[1]', 'INT')
        FROM @Answers.nodes('/Answers/Answer') AS T(Item);

        COMMIT TRANSACTION;
        PRINT 'Exam Answers Submitted Successfully!';
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        DECLARE @ErrMsg NVARCHAR(4000) = ERROR_MESSAGE();
        RAISERROR(@ErrMsg, 16, 1);
    END CATCH
END



--#####################################################################
--#####################################################################


CREATE PROCEDURE CorrectExam
    @StudentExamID INT
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        -- 1. حساب مجموع الدرجات بناءً على مطابقة الإجابات
        -- إذا كانت ChosenOptionID تطابق OptionID في ModelAnswer، يأخذ الطالب درجة السؤال 
        DECLARE @CalculatedGrade INT = 0;

        SELECT @CalculatedGrade = SUM(ISNULL(Q.Points, 0))
        FROM StudentAnswer SA
        JOIN Question Q ON SA.QuestionID = Q.QuestionID
        JOIN ModelAnswer MA ON Q.QuestionID = MA.QuestionID
        WHERE SA.StudentExamID = @StudentExamID
          AND SA.ChosenOptionID = MA.OptionID; -- شرط الإجابة الصحيحة 

        -- 2. تحديث جدول StudentExam بالدرجة النهائية 
        UPDATE StudentExam
        SET TotalGrade = ISNULL(@CalculatedGrade, 0)
        WHERE StudentExamID = @StudentExamID;

        COMMIT TRANSACTION;
        PRINT 'Exam Corrected and Grade Updated Successfully!';
    END TRY
    BEGIN CATCH
        -- التراجع عن أي تغييرات في حال حدوث خطأ لضمان سلامة البيانات 
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        DECLARE @ErrMsg NVARCHAR(4000) = ERROR_MESSAGE();
        RAISERROR(@ErrMsg, 16, 1);
    END CATCH
END




--#####################################################################
--#####################################################################


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


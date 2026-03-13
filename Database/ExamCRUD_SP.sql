USE ITI_ExaminationDB;
GO

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
GO


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
GO



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
GO




CREATE PROCEDURE SelectQuestion
    @QuestionID INT = NULL,
    @CourseID INT = NULL,
    @QuestionType NVARCHAR(10) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    SELECT QuestionID, CourseID, QuestionText, QuestionType, Points
    FROM Question
    WHERE (@QuestionID IS NULL OR QuestionID = @QuestionID)
      AND (@CourseID IS NULL OR CourseID = @CourseID)
      AND (@QuestionType IS NULL OR QuestionType = @QuestionType);
END;
GO

CREATE PROCEDURE InsertOption
    @QuestionID INT,
    @OptionText NVARCHAR(MAX),
    @OptionOrder INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    IF @QuestionID IS NULL
    BEGIN
        RAISERROR('QuestionID cannot be null.', 16, 1);
        RETURN;
    END

    IF @OptionText IS NULL OR LTRIM(RTRIM(@OptionText)) = ''
    BEGIN
        RAISERROR('OptionText cannot be empty.', 16, 1);
        RETURN;
    END

    DECLARE @QuestionType NVARCHAR(10);
    SELECT @QuestionType = QuestionType
    FROM Question
    WHERE QuestionID = @QuestionID;

    IF @QuestionType IS NULL
    BEGIN
        RAISERROR('Question not found.', 16, 1);
        RETURN;
    END

    DECLARE @MaxAllowed INT = CASE WHEN @QuestionType = 'MCQ' THEN 4 WHEN @QuestionType = 'TF' THEN 2 ELSE 0 END;
    IF @MaxAllowed = 0
    BEGIN
        RAISERROR('Unsupported QuestionType.', 16, 1);
        RETURN;
    END

    DECLARE @CurrentCount INT;
    SELECT @CurrentCount = COUNT(*) FROM [Option] WHERE QuestionID = @QuestionID;

    IF @CurrentCount >= @MaxAllowed
    BEGIN
        RAISERROR('Cannot insert more options for this question type.', 16, 1);
        RETURN;
    END

    IF @OptionOrder IS NULL
    BEGIN
        SELECT @OptionOrder = ISNULL(MAX(OptionOrder), 0) + 1
        FROM [Option]
        WHERE QuestionID = @QuestionID;
    END

    IF @OptionOrder < 1 OR @OptionOrder > @MaxAllowed
    BEGIN
        RAISERROR('OptionOrder is out of range for this question type.', 16, 1);
        RETURN;
    END

    IF EXISTS (SELECT 1 FROM [Option] WHERE QuestionID = @QuestionID AND OptionOrder = @OptionOrder)
    BEGIN
        RAISERROR('OptionOrder already exists for this question.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        INSERT INTO [Option] (QuestionID, OptionText, OptionOrder)
        VALUES (@QuestionID, @OptionText, @OptionOrder);

        SELECT SCOPE_IDENTITY() AS NewOptionID;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE PROCEDURE UpdateOption
    @OptionID INT,
    @OptionText NVARCHAR(MAX) = NULL,
    @OptionOrder INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    IF @OptionID IS NULL
    BEGIN
        RAISERROR('OptionID cannot be null.', 16, 1);
        RETURN;
    END

    DECLARE @QuestionID INT;
    SELECT @QuestionID = QuestionID
    FROM [Option]
    WHERE OptionID = @OptionID;

    IF @QuestionID IS NULL
    BEGIN
        RAISERROR('Option not found.', 16, 1);
        RETURN;
    END

    IF @OptionText IS NOT NULL AND LTRIM(RTRIM(@OptionText)) = ''
    BEGIN
        RAISERROR('OptionText cannot be empty.', 16, 1);
        RETURN;
    END

    DECLARE @QuestionType NVARCHAR(10);
    SELECT @QuestionType = QuestionType
    FROM Question
    WHERE QuestionID = @QuestionID;

    DECLARE @MaxAllowed INT = CASE WHEN @QuestionType = 'MCQ' THEN 4 WHEN @QuestionType = 'TF' THEN 2 ELSE 0 END;
    IF @MaxAllowed = 0
    BEGIN
        RAISERROR('Unsupported QuestionType.', 16, 1);
        RETURN;
    END

    IF @OptionOrder IS NOT NULL
    BEGIN
        IF @OptionOrder < 1 OR @OptionOrder > @MaxAllowed
        BEGIN
            RAISERROR('OptionOrder is out of range for this question type.', 16, 1);
            RETURN;
        END

        IF EXISTS (
            SELECT 1
            FROM [Option]
            WHERE QuestionID = @QuestionID
              AND OptionOrder = @OptionOrder
              AND OptionID <> @OptionID
        )
        BEGIN
            RAISERROR('OptionOrder already exists for this question.', 16, 1);
            RETURN;
        END
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        UPDATE [Option]
        SET OptionText = COALESCE(@OptionText, OptionText),
            OptionOrder = COALESCE(@OptionOrder, OptionOrder)
        WHERE OptionID = @OptionID;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE PROCEDURE DeleteOption
    @OptionID INT
AS
BEGIN
    SET NOCOUNT ON;

    IF @OptionID IS NULL
    BEGIN
        RAISERROR('OptionID cannot be null.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM [Option] WHERE OptionID = @OptionID)
    BEGIN
        RAISERROR('Option not found.', 16, 1);
        RETURN;
    END

    IF EXISTS (SELECT 1 FROM ModelAnswer WHERE OptionID = @OptionID)
    BEGIN
        RAISERROR('Cannot delete option: it is set as a model answer.', 16, 1);
        RETURN;
    END

    IF EXISTS (SELECT 1 FROM StudentAnswer WHERE ChosenOptionID = @OptionID)
    BEGIN
        RAISERROR('Cannot delete option: it is used in student answers.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        DELETE FROM [Option] WHERE OptionID = @OptionID;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE PROCEDURE SelectOption
    @OptionID INT = NULL,
    @QuestionID INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    SELECT OptionID, QuestionID, OptionText, OptionOrder
    FROM [Option]
    WHERE (@OptionID IS NULL OR OptionID = @OptionID)
      AND (@QuestionID IS NULL OR QuestionID = @QuestionID)
    ORDER BY QuestionID, OptionOrder, OptionID;
END;
GO

CREATE PROCEDURE InsertModelAnswer
    @QuestionID INT,
    @OptionID INT
AS
BEGIN
    SET NOCOUNT ON;

    IF @QuestionID IS NULL OR @OptionID IS NULL
    BEGIN
        RAISERROR('QuestionID and OptionID are required.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Question WHERE QuestionID = @QuestionID)
    BEGIN
        RAISERROR('Question not found.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM [Option] WHERE OptionID = @OptionID AND QuestionID = @QuestionID)
    BEGIN
        RAISERROR('Option does not belong to the question.', 16, 1);
        RETURN;
    END

    IF EXISTS (SELECT 1 FROM ModelAnswer WHERE QuestionID = @QuestionID)
    BEGIN
        RAISERROR('Model answer already exists for this question.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        INSERT INTO ModelAnswer (QuestionID, OptionID)
        VALUES (@QuestionID, @OptionID);

        SELECT SCOPE_IDENTITY() AS NewModelAnswerID;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE PROCEDURE UpdateModelAnswer
    @ModelAnswerID INT,
    @OptionID INT
AS
BEGIN
    SET NOCOUNT ON;

    IF @ModelAnswerID IS NULL OR @OptionID IS NULL
    BEGIN
        RAISERROR('ModelAnswerID and OptionID are required.', 16, 1);
        RETURN;
    END

    DECLARE @QuestionID INT;
    SELECT @QuestionID = QuestionID
    FROM ModelAnswer
    WHERE ModelAnswerID = @ModelAnswerID;

    IF @QuestionID IS NULL
    BEGIN
        RAISERROR('ModelAnswer not found.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM [Option] WHERE OptionID = @OptionID AND QuestionID = @QuestionID)
    BEGIN
        RAISERROR('Option does not belong to the question.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        UPDATE ModelAnswer
        SET OptionID = @OptionID
        WHERE ModelAnswerID = @ModelAnswerID;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE PROCEDURE DeleteModelAnswer
    @ModelAnswerID INT = NULL,
    @QuestionID INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    IF @ModelAnswerID IS NULL AND @QuestionID IS NULL
    BEGIN
        RAISERROR('ModelAnswerID or QuestionID is required.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        IF @ModelAnswerID IS NOT NULL
        BEGIN
            DELETE FROM ModelAnswer WHERE ModelAnswerID = @ModelAnswerID;
        END
        ELSE
        BEGIN
            DELETE FROM ModelAnswer WHERE QuestionID = @QuestionID;
        END

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE PROCEDURE SelectModelAnswer
    @ModelAnswerID INT = NULL,
    @QuestionID INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        ma.ModelAnswerID,
        ma.QuestionID,
        ma.OptionID,
        o.OptionText,
        o.OptionOrder
    FROM ModelAnswer ma
    JOIN [Option] o ON ma.OptionID = o.OptionID
    WHERE (@ModelAnswerID IS NULL OR ma.ModelAnswerID = @ModelAnswerID)
      AND (@QuestionID IS NULL OR ma.QuestionID = @QuestionID);
END;
GO

CREATE PROCEDURE SetModelAnswer
    @QuestionID INT,
    @OptionID INT
AS
BEGIN
    SET NOCOUNT ON;

    IF @QuestionID IS NULL OR @OptionID IS NULL
    BEGIN
        RAISERROR('QuestionID and OptionID are required.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Question WHERE QuestionID = @QuestionID)
    BEGIN
        RAISERROR('Question not found.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM [Option] WHERE OptionID = @OptionID AND QuestionID = @QuestionID)
    BEGIN
        RAISERROR('Option does not belong to the question.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        IF EXISTS (SELECT 1 FROM ModelAnswer WHERE QuestionID = @QuestionID)
        BEGIN
            UPDATE ModelAnswer
            SET OptionID = @OptionID
            WHERE QuestionID = @QuestionID;
        END
        ELSE
        BEGIN
            INSERT INTO ModelAnswer (QuestionID, OptionID)
            VALUES (@QuestionID, @OptionID);
        END

        SELECT ModelAnswerID, QuestionID, OptionID
        FROM ModelAnswer
        WHERE QuestionID = @QuestionID;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO


--#####################################################################
--#####################################################################

CREATE PROCEDURE InsertStudent
    @StudentName NVARCHAR(100),
    @Email NVARCHAR(255) = NULL,
    @Phone NVARCHAR(20) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    IF @StudentName IS NULL OR LTRIM(RTRIM(@StudentName)) = ''
    BEGIN
        RAISERROR('StudentName cannot be empty.', 16, 1);
        RETURN;
    END

    IF @Email IS NOT NULL AND EXISTS (SELECT 1 FROM Student WHERE Email = @Email)
    BEGIN
        RAISERROR('Email already exists.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        INSERT INTO Student (StudentName, Email, Phone)
        VALUES (@StudentName, @Email, @Phone);

        SELECT SCOPE_IDENTITY() AS NewStudentID;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE PROCEDURE UpdateStudent
    @StudentID INT,
    @StudentName NVARCHAR(100) = NULL,
    @Email NVARCHAR(255) = NULL,
    @Phone NVARCHAR(20) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    IF @StudentID IS NULL
    BEGIN
        RAISERROR('StudentID cannot be null.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Student WHERE StudentID = @StudentID)
    BEGIN
        RAISERROR('Student not found.', 16, 1);
        RETURN;
    END

    IF @StudentName IS NOT NULL AND LTRIM(RTRIM(@StudentName)) = ''
    BEGIN
        RAISERROR('StudentName cannot be empty.', 16, 1);
        RETURN;
    END

    IF @Email IS NOT NULL AND EXISTS (SELECT 1 FROM Student WHERE Email = @Email AND StudentID <> @StudentID)
    BEGIN
        RAISERROR('Email already exists.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        UPDATE Student
        SET StudentName = COALESCE(@StudentName, StudentName),
            Email = COALESCE(@Email, Email),
            Phone = COALESCE(@Phone, Phone)
        WHERE StudentID = @StudentID;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE PROCEDURE DeleteStudent
    @StudentID INT
AS
BEGIN
    SET NOCOUNT ON;

    IF @StudentID IS NULL
    BEGIN
        RAISERROR('StudentID cannot be null.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Student WHERE StudentID = @StudentID)
    BEGIN
        RAISERROR('Student not found.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        DELETE FROM Student WHERE StudentID = @StudentID;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE PROCEDURE SelectStudent
    @StudentID INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    SELECT StudentID, StudentName, Email, Phone
    FROM Student
    WHERE (@StudentID IS NULL OR StudentID = @StudentID);
END;
GO

CREATE PROCEDURE InsertStudentTrack
    @StudentID INT,
    @TrackID INT
AS
BEGIN
    SET NOCOUNT ON;

    IF @StudentID IS NULL OR @TrackID IS NULL
    BEGIN
        RAISERROR('StudentID and TrackID are required.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Student WHERE StudentID = @StudentID)
    BEGIN
        RAisERROR('Student not found.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Track WHERE TrackID = @TrackID)
    BEGIN
        RAISERROR('Track not found.', 16, 1);
        RETURN;
    END

    IF EXISTS (SELECT 1 FROM Student_Track WHERE StudentID = @StudentID AND TrackID = @TrackID)
    BEGIN
        RAISERROR('Mapping already exists.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        INSERT INTO Student_Track (StudentID, TrackID)
        VALUES (@StudentID, @TrackID);

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE PROCEDURE UpdateStudentTrack
    @StudentID INT,
    @TrackID INT,
    @NewStudentID INT,
    @NewTrackID INT
AS
BEGIN
    SET NOCOUNT ON;

    IF @StudentID IS NULL OR @TrackID IS NULL OR @NewStudentID IS NULL OR @NewTrackID IS NULL
    BEGIN
        RAISERROR('All parameters are required.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Student_Track WHERE StudentID = @StudentID AND TrackID = @TrackID)
    BEGIN
        RAISERROR('Mapping not found.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Student WHERE StudentID = @NewStudentID)
    BEGIN
        RAISERROR('New Student not found.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Track WHERE TrackID = @NewTrackID)
    BEGIN
        RAISERROR('New Track not found.', 16, 1);
        RETURN;
    END

    IF EXISTS (SELECT 1 FROM Student_Track WHERE StudentID = @NewStudentID AND TrackID = @NewTrackID)
    BEGIN
        RAISERROR('Target mapping already exists.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        UPDATE Student_Track
        SET StudentID = @NewStudentID,
            TrackID = @NewTrackID
        WHERE StudentID = @StudentID AND TrackID = @TrackID;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE PROCEDURE DeleteStudentTrack
    @StudentID INT,
    @TrackID INT
AS
BEGIN
    SET NOCOUNT ON;

    IF @StudentID IS NULL OR @TrackID IS NULL
    BEGIN
        RAISERROR('StudentID and TrackID are required.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Student_Track WHERE StudentID = @StudentID AND TrackID = @TrackID)
    BEGIN
        RAISERROR('Mapping not found.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        DELETE FROM Student_Track
        WHERE StudentID = @StudentID AND TrackID = @TrackID;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE PROCEDURE SelectStudentTrack
    @StudentID INT = NULL,
    @TrackID INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    SELECT StudentID, TrackID
    FROM Student_Track
    WHERE (@StudentID IS NULL OR StudentID = @StudentID)
      AND (@TrackID IS NULL OR TrackID = @TrackID);
END;
GO

CREATE PROCEDURE InsertTrackCourse
    @TrackID INT,
    @CourseID INT
AS
BEGIN
    SET NOCOUNT ON;

    IF @TrackID IS NULL OR @CourseID IS NULL
    BEGIN
        RAISERROR('TrackID and CourseID are required.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Track WHERE TrackID = @TrackID)
    BEGIN
        RAISERROR('Track not found.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Course WHERE CourseID = @CourseID)
    BEGIN
        RAISERROR('Course not found.', 16, 1);
        RETURN;
    END

    IF EXISTS (SELECT 1 FROM Track_Course WHERE TrackID = @TrackID AND CourseID = @CourseID)
    BEGIN
        RAISERROR('Mapping already exists.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        INSERT INTO Track_Course (TrackID, CourseID)
        VALUES (@TrackID, @CourseID);

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE PROCEDURE UpdateTrackCourse
    @TrackID INT,
    @CourseID INT,
    @NewTrackID INT,
    @NewCourseID INT
AS
BEGIN
    SET NOCOUNT ON;

    IF @TrackID IS NULL OR @CourseID IS NULL OR @NewTrackID IS NULL OR @NewCourseID IS NULL
    BEGIN
        RAISERROR('All parameters are required.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Track_Course WHERE TrackID = @TrackID AND CourseID = @CourseID)
    BEGIN
        RAISERROR('Mapping not found.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Track WHERE TrackID = @NewTrackID)
    BEGIN
        RAISERROR('New Track not found.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Course WHERE CourseID = @NewCourseID)
    BEGIN
        RAISERROR('New Course not found.', 16, 1);
        RETURN;
    END

    IF EXISTS (SELECT 1 FROM Track_Course WHERE TrackID = @NewTrackID AND CourseID = @NewCourseID)
    BEGIN
        RAISERROR('Target mapping already exists.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        UPDATE Track_Course
        SET TrackID = @NewTrackID,
            CourseID = @NewCourseID
        WHERE TrackID = @TrackID AND CourseID = @CourseID;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE PROCEDURE DeleteTrackCourse
    @TrackID INT,
    @CourseID INT
AS
BEGIN
    SET NOCOUNT ON;

    IF @TrackID IS NULL OR @CourseID IS NULL
    BEGIN
        RAISERROR('TrackID and CourseID are required.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Track_Course WHERE TrackID = @TrackID AND CourseID = @CourseID)
    BEGIN
        RAISERROR('Mapping not found.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        DELETE FROM Track_Course
        WHERE TrackID = @TrackID AND CourseID = @CourseID;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE PROCEDURE SelectTrackCourse
    @TrackID INT = NULL,
    @CourseID INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    SELECT TrackID, CourseID
    FROM Track_Course
    WHERE (@TrackID IS NULL OR TrackID = @TrackID)
      AND (@CourseID IS NULL OR CourseID = @CourseID);
END;
GO

--#####################################################################
--#####################################################################

CREATE PROCEDURE InsertExam
    @ExamName NVARCHAR(150),
    @CourseID INT,
    @TotalQuestions INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    IF @CourseID IS NULL
    BEGIN
        RAISERROR('CourseID cannot be null.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Course WHERE CourseID = @CourseID)
    BEGIN
        RAISERROR('Course not found.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        INSERT INTO Exam (ExamName, CourseID, TotalQuestions)
        VALUES (@ExamName, @CourseID, @TotalQuestions);

        SELECT SCOPE_IDENTITY() AS NewExamID;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE PROCEDURE UpdateExam
    @ExamID INT,
    @ExamName NVARCHAR(150) = NULL,
    @CourseID INT = NULL,
    @TotalQuestions INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    IF @ExamID IS NULL
    BEGIN
        RAISERROR('ExamID cannot be null.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Exam WHERE ExamID = @ExamID)
    BEGIN
        RAISERROR('Exam not found.', 16, 1);
        RETURN;
    END

    IF @CourseID IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Course WHERE CourseID = @CourseID)
    BEGIN
        RAISERROR('Course not found.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        UPDATE Exam
        SET ExamName = COALESCE(@ExamName, ExamName),
            CourseID = COALESCE(@CourseID, CourseID),
            TotalQuestions = COALESCE(@TotalQuestions, TotalQuestions)
        WHERE ExamID = @ExamID;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE PROCEDURE DeleteExam
    @ExamID INT
AS
BEGIN
    SET NOCOUNT ON;

    IF @ExamID IS NULL
    BEGIN
        RAISERROR('ExamID cannot be null.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Exam WHERE ExamID = @ExamID)
    BEGIN
        RAISERROR('Exam not found.', 16, 1);
        RETURN;
    END

    IF EXISTS (SELECT 1 FROM StudentExam WHERE ExamID = @ExamID)
    BEGIN
        RAISERROR('Cannot delete exam: student attempts exist.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        DELETE FROM Exam WHERE ExamID = @ExamID;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE PROCEDURE SelectExam
    @ExamID INT = NULL,
    @CourseID INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    SELECT ExamID, ExamName, CourseID, CreatedDate, TotalQuestions
    FROM Exam
    WHERE (@ExamID IS NULL OR ExamID = @ExamID)
      AND (@CourseID IS NULL OR CourseID = @CourseID);
END;
GO

CREATE PROCEDURE InsertExamQuestion
    @ExamID INT,
    @QuestionID INT,
    @OrderNo INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    IF @ExamID IS NULL OR @QuestionID IS NULL
    BEGIN
        RAISERROR('ExamID and QuestionID are required.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Exam WHERE ExamID = @ExamID)
    BEGIN
        RAISERROR('Exam not found.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Question WHERE QuestionID = @QuestionID)
    BEGIN
        RAISERROR('Question not found.', 16, 1);
        RETURN;
    END

    IF EXISTS (SELECT 1 FROM Exam_Question WHERE ExamID = @ExamID AND QuestionID = @QuestionID)
    BEGIN
        RAISERROR('Mapping already exists.', 16, 1);
        RETURN;
    END

    IF @OrderNo IS NULL
    BEGIN
        SELECT @OrderNo = ISNULL(MAX(OrderNo), 0) + 1
        FROM Exam_Question
        WHERE ExamID = @ExamID;
    END

    IF EXISTS (SELECT 1 FROM Exam_Question WHERE ExamID = @ExamID AND OrderNo = @OrderNo)
    BEGIN
        RAISERROR('OrderNo already exists for this exam.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        INSERT INTO Exam_Question (ExamID, QuestionID, OrderNo)
        VALUES (@ExamID, @QuestionID, @OrderNo);

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE PROCEDURE UpdateExamQuestion
    @ExamID INT,
    @QuestionID INT,
    @OrderNo INT
AS
BEGIN
    SET NOCOUNT ON;

    IF @ExamID IS NULL OR @QuestionID IS NULL OR @OrderNo IS NULL
    BEGIN
        RAISERROR('ExamID, QuestionID and OrderNo are required.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Exam_Question WHERE ExamID = @ExamID AND QuestionID = @QuestionID)
    BEGIN
        RAISERROR('Mapping not found.', 16, 1);
        RETURN;
    END

    IF EXISTS (SELECT 1 FROM Exam_Question WHERE ExamID = @ExamID AND OrderNo = @OrderNo AND QuestionID <> @QuestionID)
    BEGIN
        RAISERROR('OrderNo already exists for this exam.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        UPDATE Exam_Question
        SET OrderNo = @OrderNo
        WHERE ExamID = @ExamID AND QuestionID = @QuestionID;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE PROCEDURE DeleteExamQuestion
    @ExamID INT,
    @QuestionID INT
AS
BEGIN
    SET NOCOUNT ON;

    IF @ExamID IS NULL OR @QuestionID IS NULL
    BEGIN
        RAISERROR('ExamID and QuestionID are required.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Exam_Question WHERE ExamID = @ExamID AND QuestionID = @QuestionID)
    BEGIN
        RAISERROR('Mapping not found.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        DELETE FROM Exam_Question
        WHERE ExamID = @ExamID AND QuestionID = @QuestionID;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE PROCEDURE SelectExamQuestion
    @ExamID INT = NULL,
    @QuestionID INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    SELECT ExamID, QuestionID, OrderNo
    FROM Exam_Question
    WHERE (@ExamID IS NULL OR ExamID = @ExamID)
      AND (@QuestionID IS NULL OR QuestionID = @QuestionID)
    ORDER BY ExamID, OrderNo;
END;
GO

--#####################################################################
--#####################################################################

CREATE PROCEDURE InsertStudentExam
    @StudentID INT,
    @ExamID INT,
    @StartTime DATETIME = NULL,
    @EndTime DATETIME = NULL
AS
BEGIN
    SET NOCOUNT ON;

    IF @StudentID IS NULL OR @ExamID IS NULL
    BEGIN
        RAISERROR('StudentID and ExamID are required.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Student WHERE StudentID = @StudentID)
    BEGIN
        RAISERROR('Student not found.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Exam WHERE ExamID = @ExamID)
    BEGIN
        RAISERROR('Exam not found.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        INSERT INTO StudentExam (StudentID, ExamID, StartTime, EndTime, TotalGrade)
        VALUES (@StudentID, @ExamID, @StartTime, @EndTime, 0);

        SELECT SCOPE_IDENTITY() AS NewStudentExamID;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE PROCEDURE UpdateStudentExam
    @StudentExamID INT,
    @StartTime DATETIME = NULL,
    @EndTime DATETIME = NULL,
    @TotalGrade INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    IF @StudentExamID IS NULL
    BEGIN
        RAISERROR('StudentExamID cannot be null.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM StudentExam WHERE StudentExamID = @StudentExamID)
    BEGIN
        RAISERROR('StudentExam not found.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        UPDATE StudentExam
        SET StartTime = COALESCE(@StartTime, StartTime),
            EndTime = COALESCE(@EndTime, EndTime),
            TotalGrade = COALESCE(@TotalGrade, TotalGrade)
        WHERE StudentExamID = @StudentExamID;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE PROCEDURE DeleteStudentExam
    @StudentExamID INT
AS
BEGIN
    SET NOCOUNT ON;

    IF @StudentExamID IS NULL
    BEGIN
        RAISERROR('StudentExamID cannot be null.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM StudentExam WHERE StudentExamID = @StudentExamID)
    BEGIN
        RAISERROR('StudentExam not found.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        DELETE FROM StudentExam WHERE StudentExamID = @StudentExamID;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE PROCEDURE SelectStudentExam
    @StudentExamID INT = NULL,
    @StudentID INT = NULL,
    @ExamID INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    SELECT StudentExamID, StudentID, ExamID, StartTime, EndTime, TotalGrade
    FROM StudentExam
    WHERE (@StudentExamID IS NULL OR StudentExamID = @StudentExamID)
      AND (@StudentID IS NULL OR StudentID = @StudentID)
      AND (@ExamID IS NULL OR ExamID = @ExamID);
END;
GO

CREATE PROCEDURE InsertStudentAnswer
    @StudentExamID INT,
    @QuestionID INT,
    @ChosenOptionID INT
AS
BEGIN
    SET NOCOUNT ON;

    IF @StudentExamID IS NULL OR @QuestionID IS NULL OR @ChosenOptionID IS NULL
    BEGIN
        RAISERROR('StudentExamID, QuestionID and ChosenOptionID are required.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM StudentExam WHERE StudentExamID = @StudentExamID)
    BEGIN
        RAISERROR('StudentExam not found.', 16, 1);
        RETURN;
    END

    DECLARE @ExamID INT;
    SELECT @ExamID = ExamID FROM StudentExam WHERE StudentExamID = @StudentExamID;

    IF NOT EXISTS (SELECT 1 FROM Exam_Question WHERE ExamID = @ExamID AND QuestionID = @QuestionID)
    BEGIN
        RAISERROR('Question is not part of this exam.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM [Option] WHERE OptionID = @ChosenOptionID AND QuestionID = @QuestionID)
    BEGIN
        RAISERROR('Chosen option does not belong to the question.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        INSERT INTO StudentAnswer (StudentExamID, QuestionID, ChosenOptionID)
        VALUES (@StudentExamID, @QuestionID, @ChosenOptionID);

        SELECT SCOPE_IDENTITY() AS NewStudentAnswerID;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE PROCEDURE UpdateStudentAnswer
    @StudentAnswerID INT,
    @ChosenOptionID INT
AS
BEGIN
    SET NOCOUNT ON;

    IF @StudentAnswerID IS NULL OR @ChosenOptionID IS NULL
    BEGIN
        RAISERROR('StudentAnswerID and ChosenOptionID are required.', 16, 1);
        RETURN;
    END

    DECLARE @QuestionID INT;
    SELECT @QuestionID = QuestionID
    FROM StudentAnswer
    WHERE StudentAnswerID = @StudentAnswerID;

    IF @QuestionID IS NULL
    BEGIN
        RAISERROR('StudentAnswer not found.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM [Option] WHERE OptionID = @ChosenOptionID AND QuestionID = @QuestionID)
    BEGIN
        RAISERROR('Chosen option does not belong to the question.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        UPDATE StudentAnswer
        SET ChosenOptionID = @ChosenOptionID
        WHERE StudentAnswerID = @StudentAnswerID;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE PROCEDURE DeleteStudentAnswer
    @StudentAnswerID INT
AS
BEGIN
    SET NOCOUNT ON;

    IF @StudentAnswerID IS NULL
    BEGIN
        RAISERROR('StudentAnswerID cannot be null.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM StudentAnswer WHERE StudentAnswerID = @StudentAnswerID)
    BEGIN
        RAISERROR('StudentAnswer not found.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        DELETE FROM StudentAnswer WHERE StudentAnswerID = @StudentAnswerID;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE PROCEDURE SelectStudentAnswer
    @StudentAnswerID INT = NULL,
    @StudentExamID INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    SELECT StudentAnswerID, StudentExamID, QuestionID, ChosenOptionID
    FROM StudentAnswer
    WHERE (@StudentAnswerID IS NULL OR StudentAnswerID = @StudentAnswerID)
      AND (@StudentExamID IS NULL OR StudentExamID = @StudentExamID);
END;
GO


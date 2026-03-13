USE ITI_ExaminationDB;
GO

 /*Purpose : Insert a new course
 Inputs  : @CourseName NVARCHAR(100), @MinDegree INT, @MaxDegree INT
 Output  : New CourseID via SCOPE_IDENTITY()
 */
CREATE PROCEDURE InsertCourse
    @CourseName NVARCHAR(100),
    @MinDegree  INT = NULL,
    @MaxDegree  INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    IF @CourseName IS NULL OR LTRIM(RTRIM(@CourseName)) = ''
    BEGIN
        RAISERROR('CourseName cannot be empty.', 16, 1);
        RETURN;
    END

    IF @MinDegree IS NOT NULL AND @MaxDegree IS NOT NULL AND @MinDegree > @MaxDegree
    BEGIN
        RAISERROR('MinDegree cannot be greater than MaxDegree.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION

            INSERT INTO Course (CourseName, MinDegree, MaxDegree)
            VALUES (@CourseName, @MinDegree, @MaxDegree);

            SELECT SCOPE_IDENTITY() AS NewCourseID;

        COMMIT TRANSACTION
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- -------------------------------------------------------

/* Purpose : Update an existing course
 Inputs  : @CourseID INT, @CourseName NVARCHAR(100), @MinDegree INT, @MaxDegree INT
 Output  : Success / error message
 */
CREATE PROCEDURE UpdateCourse
    @CourseID   INT,
    @CourseName NVARCHAR(100),
    @MinDegree  INT = NULL,
    @MaxDegree  INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    IF @CourseID IS NULL
    BEGIN
        RAISERROR('CourseID cannot be null.', 16, 1);
        RETURN;
    END

    IF @CourseName IS NULL OR LTRIM(RTRIM(@CourseName)) = ''
    BEGIN
        RAISERROR('CourseName cannot be empty.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Course WHERE CourseID = @CourseID)
    BEGIN
        RAISERROR('Course not found.', 16, 1);
        RETURN;
    END

    IF @MinDegree IS NOT NULL AND @MaxDegree IS NOT NULL AND @MinDegree > @MaxDegree
    BEGIN
        RAISERROR('MinDegree cannot be greater than MaxDegree.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION

            UPDATE Course
            SET CourseName = @CourseName,
                MinDegree  = @MinDegree,
                MaxDegree  = @MaxDegree
            WHERE CourseID = @CourseID;

            PRINT 'Course updated successfully.';

        COMMIT TRANSACTION
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- -------------------------------------------------------

/* Purpose : Delete a course — blocked if exams or questions exist (NO ACTION FK)
 Inputs  : @CourseID INT
 Output  : Success / error message
 */
CREATE PROCEDURE DeleteCourse
    @CourseID INT
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

    IF EXISTS (SELECT 1 FROM Exam WHERE CourseID = @CourseID)
    BEGIN
        RAISERROR('Cannot delete course: existing exams are linked to it.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION

            DELETE FROM Course WHERE CourseID = @CourseID;

            PRINT 'Course deleted successfully.';

        COMMIT TRANSACTION
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- -------------------------------------------------------

/* Purpose : Select courses — all, by CourseID, or filtered by TrackID
 Inputs  : @CourseID INT (optional), @TrackID INT (optional)
 Output  : Course rows, with TrackName if filtered by track
 */
CREATE PROCEDURE SelectCourseByTrack
    @CourseID INT = NULL,
    @TrackID  INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    IF @TrackID IS NOT NULL
    BEGIN
        SELECT  c.CourseID,
                c.CourseName,
                c.MinDegree,
                c.MaxDegree,
                t.TrackID,
                t.TrackName
        FROM Course c
        JOIN Track_Course tc ON c.CourseID = tc.CourseID
        JOIN Track t         ON tc.TrackID  = t.TrackID
        WHERE t.TrackID = @TrackID
          AND (@CourseID IS NULL OR c.CourseID = @CourseID);
    END
    ELSE
    BEGIN
        SELECT CourseID, CourseName, MinDegree, MaxDegree
        FROM Course
        WHERE (@CourseID IS NULL OR CourseID = @CourseID);
    END
END;
GO



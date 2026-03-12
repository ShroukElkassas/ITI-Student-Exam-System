USE ITI_ExaminationDB;
GO

 /*Purpose : Insert a new track under a branch
 Inputs  : @TrackName NVARCHAR(100), @BranchID INT, @DurationMonths INT
 Output  : New TrackID via SCOPE_IDENTITY()
 */
CREATE PROCEDURE InsertTrack
    @TrackName      NVARCHAR(100),
    @BranchID       INT,
    @DurationMonths INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    IF @TrackName IS NULL OR LTRIM(RTRIM(@TrackName)) = ''
    BEGIN
        RAISERROR('TrackName cannot be empty.', 16, 1);
        RETURN;
    END

    IF @BranchID IS NULL
    BEGIN
        RAISERROR('BranchID cannot be null.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Branch WHERE BranchID = @BranchID)
    BEGIN
        RAISERROR('BranchID does not exist.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION

            INSERT INTO Track (TrackName, BranchID, DurationMonths)
            VALUES (@TrackName, @BranchID, @DurationMonths);

            SELECT SCOPE_IDENTITY() AS NewTrackID;

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

/* Purpose : Update an existing track
 Inputs  : @TrackID INT, @TrackName NVARCHAR(100), @BranchID INT, @DurationMonths INT
 Output  : Success / error message
 */
CREATE PROCEDURE UpdateTrack
    @TrackID        INT,
    @TrackName      NVARCHAR(100),
    @BranchID       INT,
    @DurationMonths INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    IF @TrackID IS NULL
    BEGIN
        RAISERROR('TrackID cannot be null.', 16, 1);
        RETURN;
    END

    IF @TrackName IS NULL OR LTRIM(RTRIM(@TrackName)) = ''
    BEGIN
        RAISERROR('TrackName cannot be empty.', 16, 1);
        RETURN;
    END

    IF @BranchID IS NULL
    BEGIN
        RAISERROR('BranchID cannot be null.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Track WHERE TrackID = @TrackID)
    BEGIN
        RAISERROR('Track not found.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Branch WHERE BranchID = @BranchID)
    BEGIN
        RAISERROR('BranchID does not exist.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION

            UPDATE Track
            SET TrackName      = @TrackName,
                BranchID       = @BranchID,
                DurationMonths = @DurationMonths
            WHERE TrackID = @TrackID;

            PRINT 'Track updated successfully.';

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

/* Purpose : Delete a track (cascades to Track_Course)
 Inputs  : @TrackID INT
 Output  : Success / error message
 */
CREATE PROCEDURE DeleteTrack
    @TrackID INT
AS
BEGIN
    SET NOCOUNT ON;

    IF @TrackID IS NULL
    BEGIN
        RAISERROR('TrackID cannot be null.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Track WHERE TrackID = @TrackID)
    BEGIN
        RAISERROR('Track not found.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION

            DELETE FROM Track WHERE TrackID = @TrackID;

            PRINT 'Track deleted successfully.';

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

/* Purpose : Select tracks — all, by TrackID, or filtered by BranchID
 Inputs  : @TrackID INT (optional), @BranchID INT (optional)
 Output  : Track rows with BranchName joined
 */
CREATE PROCEDURE SelectTrackByBranch
    @TrackID  INT = NULL,
    @BranchID INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    SELECT  t.TrackID,
            t.TrackName,
            t.DurationMonths,
            b.BranchID,
            b.BranchName
    FROM Track t
    JOIN Branch b ON t.BranchID = b.BranchID
    WHERE (@TrackID  IS NULL OR t.TrackID  = @TrackID)
      AND (@BranchID IS NULL OR t.BranchID = @BranchID);
END;
GO


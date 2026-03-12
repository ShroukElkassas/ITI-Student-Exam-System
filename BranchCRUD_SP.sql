USE ITI_ExaminationDB;
GO

/* Purpose : Insert a new branch
Inputs  : @BranchName NVARCHAR(100), @Location NVARCHAR(200)
 Output  : New BranchID via SCOPE_IDENTITY()
 */
CREATE PROCEDURE InsertBranch
    @BranchName NVARCHAR(100),
    @Location   NVARCHAR(200) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    IF @BranchName IS NULL OR LTRIM(RTRIM(@BranchName)) = ''
    BEGIN
        RAISERROR('BranchName cannot be empty.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION

            INSERT INTO Branch (BranchName, Location)
            VALUES (@BranchName, @Location);

            SELECT SCOPE_IDENTITY() AS NewBranchID;

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

/* Purpose : Update an existing branch
 Inputs  : @BranchID INT, @BranchName NVARCHAR(100), @Location NVARCHAR(200)
 Output  : Success / error message
 */
CREATE PROCEDURE UpdateBranch
    @BranchID   INT,
    @BranchName NVARCHAR(100),
    @Location   NVARCHAR(200) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    IF @BranchID IS NULL
    BEGIN
        RAISERROR('BranchID cannot be null.', 16, 1);
        RETURN;
    END

    IF @BranchName IS NULL OR LTRIM(RTRIM(@BranchName)) = ''
    BEGIN
        RAISERROR('BranchName cannot be empty.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Branch WHERE BranchID = @BranchID)
    BEGIN
        RAISERROR('Branch not found.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION

            UPDATE Branch
            SET BranchName = @BranchName,
                Location   = @Location
            WHERE BranchID = @BranchID;

            PRINT 'Branch updated successfully.';

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

/* Purpose : Delete a branch (cascades to Track, Student_Track)
 Inputs  : @BranchID INT
 Output  : Success / error message
 */
CREATE PROCEDURE DeleteBranch
    @BranchID INT
AS
BEGIN
    SET NOCOUNT ON;

    IF @BranchID IS NULL
    BEGIN
        RAISERROR('BranchID cannot be null.', 16, 1);
        RETURN;
    END

    IF NOT EXISTS (SELECT 1 FROM Branch WHERE BranchID = @BranchID)
    BEGIN
        RAISERROR('Branch not found.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION

            DELETE FROM Branch WHERE BranchID = @BranchID;

            PRINT 'Branch deleted successfully.';

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

/* Purpose : Select one branch by ID, or all branches if no ID given
 Inputs  : @BranchID INT (optional — pass NULL to get all)
 Output  : Branch rows (BranchID, BranchName, Location)
 */
CREATE PROCEDURE SelectBranch
    @BranchID INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    SELECT BranchID, BranchName, Location
    FROM Branch
    WHERE (@BranchID IS NULL OR BranchID = @BranchID);
END;
GO

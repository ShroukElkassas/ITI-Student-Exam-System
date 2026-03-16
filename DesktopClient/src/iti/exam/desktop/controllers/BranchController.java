package iti.exam.desktop.controllers;

import iti.exam.desktop.db.SqlParam;
import iti.exam.desktop.db.StoredProcExecutor;
import iti.exam.desktop.db.StoredProcResult;
import iti.exam.desktop.db.StoredProcedures;
import iti.exam.desktop.models.Branch;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class BranchController {
    private final StoredProcExecutor executor;

    public BranchController(StoredProcExecutor executor) {
        this.executor = executor;
    }

    public int insertBranch(String branchName, String location) throws SQLException {
        StoredProcResult result = executor.execute(
                StoredProcedures.INSERT_BRANCH,
                SqlParam.in(Types.NVARCHAR, branchName),
                SqlParam.in(Types.NVARCHAR, location)
        );

        List<Map<String, Object>> rows = result.firstResultSetOrEmpty();
        if (rows.isEmpty()) {
            throw new SQLException("InsertBranch did not return NewBranchID.");
        }
        Object value = rows.get(0).get("NewBranchID");
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).intValue();
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    public void updateBranch(int branchId, String branchName, String location) throws SQLException {
        executor.execute(
                StoredProcedures.UPDATE_BRANCH,
                SqlParam.in(Types.INTEGER, branchId),
                SqlParam.in(Types.NVARCHAR, branchName),
                SqlParam.in(Types.NVARCHAR, location)
        );
    }

    public void deleteBranch(int branchId) throws SQLException {
        executor.execute(
                StoredProcedures.DELETE_BRANCH,
                SqlParam.in(Types.INTEGER, branchId)
        );
    }

    public List<Branch> selectBranches(Integer branchId) throws SQLException {
        StoredProcResult result = executor.execute(
                StoredProcedures.SELECT_BRANCH,
                SqlParam.in(Types.INTEGER, branchId)
        );

        List<Branch> branches = new ArrayList<Branch>();
        for (Map<String, Object> row : result.firstResultSetOrEmpty()) {
            int id = toInt(row.get("BranchID"));
            String name = toString(row.get("BranchName"));
            String loc = toString(row.get("Location"));
            branches.add(new Branch(id, name, loc));
        }
        return branches;
    }

    private static int toInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).intValue();
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private static String toString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}


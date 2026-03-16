package iti.exam.desktop.controllers;

import iti.exam.desktop.db.SqlParam;
import iti.exam.desktop.db.StoredProcExecutor;
import iti.exam.desktop.db.StoredProcResult;
import iti.exam.desktop.db.StoredProcedures;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

public final class OptionController {
    private final StoredProcExecutor executor;

    public OptionController(StoredProcExecutor executor) {
        this.executor = executor;
    }

    public int insertOption(int questionId, String optionText, Integer optionOrder) throws SQLException {
        StoredProcResult result = executor.execute(
                StoredProcedures.INSERT_OPTION,
                SqlParam.in(Types.INTEGER, questionId),
                SqlParam.in(Types.NVARCHAR, optionText),
                SqlParam.in(Types.INTEGER, optionOrder)
        );

        List<Map<String, Object>> rows = result.firstResultSetOrEmpty();
        if (rows.isEmpty()) {
            return 0;
        }
        Object value = rows.get(0).get("NewOptionID");
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).intValue();
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    public void updateOption(int optionId, String optionText, Integer optionOrder) throws SQLException {
        executor.execute(
                StoredProcedures.UPDATE_OPTION,
                SqlParam.in(Types.INTEGER, optionId),
                SqlParam.in(Types.NVARCHAR, optionText),
                SqlParam.in(Types.INTEGER, optionOrder)
        );
    }

    public void deleteOption(int optionId) throws SQLException {
        executor.execute(
                StoredProcedures.DELETE_OPTION,
                SqlParam.in(Types.INTEGER, optionId)
        );
    }

    public List<Map<String, Object>> selectOptions(Integer optionId, Integer questionId) throws SQLException {
        StoredProcResult result = executor.execute(
                StoredProcedures.SELECT_OPTION,
                SqlParam.in(Types.INTEGER, optionId),
                SqlParam.in(Types.INTEGER, questionId)
        );
        return result.firstResultSetOrEmpty();
    }
}


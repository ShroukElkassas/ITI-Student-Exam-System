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

public final class ModelAnswerController {
    private final StoredProcExecutor executor;

    public ModelAnswerController(StoredProcExecutor executor) {
        this.executor = executor;
    }

    public int insertModelAnswer(int questionId, int optionId) throws SQLException {
        StoredProcResult result = executor.execute(
                StoredProcedures.INSERT_MODEL_ANSWER,
                SqlParam.in(Types.INTEGER, questionId),
                SqlParam.in(Types.INTEGER, optionId)
        );

        List<Map<String, Object>> rows = result.firstResultSetOrEmpty();
        if (rows.isEmpty()) {
            return 0;
        }
        Object value = rows.get(0).get("NewModelAnswerID");
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).intValue();
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    public void updateModelAnswer(int modelAnswerId, int optionId) throws SQLException {
        executor.execute(
                StoredProcedures.UPDATE_MODEL_ANSWER,
                SqlParam.in(Types.INTEGER, modelAnswerId),
                SqlParam.in(Types.INTEGER, optionId)
        );
    }

    public void deleteModelAnswer(Integer modelAnswerId, Integer questionId) throws SQLException {
        executor.execute(
                StoredProcedures.DELETE_MODEL_ANSWER,
                SqlParam.in(Types.INTEGER, modelAnswerId),
                SqlParam.in(Types.INTEGER, questionId)
        );
    }

    public List<Map<String, Object>> selectModelAnswer(Integer modelAnswerId, Integer questionId) throws SQLException {
        StoredProcResult result = executor.execute(
                StoredProcedures.SELECT_MODEL_ANSWER,
                SqlParam.in(Types.INTEGER, modelAnswerId),
                SqlParam.in(Types.INTEGER, questionId)
        );
        return result.firstResultSetOrEmpty();
    }

    public List<Map<String, Object>> setModelAnswer(int questionId, int optionId) throws SQLException {
        StoredProcResult result = executor.execute(
                StoredProcedures.SET_MODEL_ANSWER,
                SqlParam.in(Types.INTEGER, questionId),
                SqlParam.in(Types.INTEGER, optionId)
        );
        return result.firstResultSetOrEmpty();
    }
}


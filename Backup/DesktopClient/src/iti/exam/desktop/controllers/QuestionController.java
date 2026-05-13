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

public final class QuestionController {
    private final StoredProcExecutor executor;

    public QuestionController(StoredProcExecutor executor) {
        this.executor = executor;
    }

    public int insertQuestion(int courseId, String text, String type, Integer points) throws SQLException {
        StoredProcResult result = executor.execute(
                StoredProcedures.INSERT_QUESTION,
                SqlParam.in(Types.INTEGER, courseId),
                SqlParam.in(Types.NVARCHAR, text),
                SqlParam.in(Types.NVARCHAR, type),
                SqlParam.in(Types.INTEGER, points)
        );
        List<Map<String, Object>> rows = result.firstResultSetOrEmpty();
        if (rows.isEmpty()) {
            return 0;
        }
        Object value = rows.get(0).get("NewQuestionID");
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).intValue();
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    public void updateQuestion(int questionId, String text, int points) throws SQLException {
        executor.execute(
                StoredProcedures.UPDATE_QUESTION,
                SqlParam.in(Types.INTEGER, questionId),
                SqlParam.in(Types.NVARCHAR, text),
                SqlParam.in(Types.INTEGER, points)
        );
    }

    public void deleteQuestion(int questionId) throws SQLException {
        executor.execute(
                StoredProcedures.DELETE_QUESTION,
                SqlParam.in(Types.INTEGER, questionId)
        );
    }

    public List<Map<String, Object>> selectQuestions(Integer questionId, Integer courseId, String questionType) throws SQLException {
        StoredProcResult result = executor.execute(
                StoredProcedures.SELECT_QUESTION,
                SqlParam.in(Types.INTEGER, questionId),
                SqlParam.in(Types.INTEGER, courseId),
                SqlParam.in(Types.NVARCHAR, questionType)
        );
        return result.firstResultSetOrEmpty();
    }
}

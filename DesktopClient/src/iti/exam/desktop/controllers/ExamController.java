package iti.exam.desktop.controllers;

import iti.exam.desktop.db.SqlParam;
import iti.exam.desktop.db.StoredProcExecutor;
import iti.exam.desktop.db.StoredProcResult;
import iti.exam.desktop.db.StoredProcedures;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Map;

public final class ExamController {
    private final StoredProcExecutor executor;

    public ExamController(StoredProcExecutor executor) {
        this.executor = executor;
    }

    public void generateExam(int courseId, String examName, int numMcq, int numTf) throws SQLException {
        executor.execute(
                StoredProcedures.GENERATE_EXAM,
                SqlParam.in(Types.INTEGER, courseId),
                SqlParam.in(Types.NVARCHAR, examName),
                SqlParam.in(Types.INTEGER, numMcq),
                SqlParam.in(Types.INTEGER, numTf)
        );
    }

    public void submitExamAnswers(int studentId, int examId, Timestamp startTime, Timestamp endTime, String answersXml) throws SQLException {
        executor.execute(
                StoredProcedures.SUBMIT_EXAM_ANSWERS,
                SqlParam.in(Types.INTEGER, studentId),
                SqlParam.in(Types.INTEGER, examId),
                SqlParam.in(Types.TIMESTAMP, startTime),
                SqlParam.in(Types.TIMESTAMP, endTime),
                SqlParam.in(Types.SQLXML, answersXml)
        );
    }

    public void correctExam(int studentExamId) throws SQLException {
        executor.execute(
                StoredProcedures.CORRECT_EXAM,
                SqlParam.in(Types.INTEGER, studentExamId)
        );
    }

    public int insertExam(String examName, int courseId, Integer totalQuestions) throws SQLException {
        StoredProcResult result = executor.execute(
                StoredProcedures.INSERT_EXAM,
                SqlParam.in(Types.NVARCHAR, examName),
                SqlParam.in(Types.INTEGER, courseId),
                SqlParam.in(Types.INTEGER, totalQuestions)
        );
        return readNewId(result, "NewExamID");
    }

    public void updateExam(int examId, String examName, Integer courseId, Integer totalQuestions) throws SQLException {
        executor.execute(
                StoredProcedures.UPDATE_EXAM,
                SqlParam.in(Types.INTEGER, examId),
                SqlParam.in(Types.NVARCHAR, examName),
                SqlParam.in(Types.INTEGER, courseId),
                SqlParam.in(Types.INTEGER, totalQuestions)
        );
    }

    public void deleteExam(int examId) throws SQLException {
        executor.execute(
                StoredProcedures.DELETE_EXAM,
                SqlParam.in(Types.INTEGER, examId)
        );
    }

    public List<Map<String, Object>> selectExam(Integer examId, Integer courseId) throws SQLException {
        StoredProcResult result = executor.execute(
                StoredProcedures.SELECT_EXAM,
                SqlParam.in(Types.INTEGER, examId),
                SqlParam.in(Types.INTEGER, courseId)
        );
        return result.firstResultSetOrEmpty();
    }

    public void insertExamQuestion(int examId, int questionId, Integer orderNo) throws SQLException {
        executor.execute(
                StoredProcedures.INSERT_EXAM_QUESTION,
                SqlParam.in(Types.INTEGER, examId),
                SqlParam.in(Types.INTEGER, questionId),
                SqlParam.in(Types.INTEGER, orderNo)
        );
    }

    public void updateExamQuestion(int examId, int questionId, int orderNo) throws SQLException {
        executor.execute(
                StoredProcedures.UPDATE_EXAM_QUESTION,
                SqlParam.in(Types.INTEGER, examId),
                SqlParam.in(Types.INTEGER, questionId),
                SqlParam.in(Types.INTEGER, orderNo)
        );
    }

    public void deleteExamQuestion(int examId, int questionId) throws SQLException {
        executor.execute(
                StoredProcedures.DELETE_EXAM_QUESTION,
                SqlParam.in(Types.INTEGER, examId),
                SqlParam.in(Types.INTEGER, questionId)
        );
    }

    public List<Map<String, Object>> selectExamQuestion(Integer examId, Integer questionId) throws SQLException {
        StoredProcResult result = executor.execute(
                StoredProcedures.SELECT_EXAM_QUESTION,
                SqlParam.in(Types.INTEGER, examId),
                SqlParam.in(Types.INTEGER, questionId)
        );
        return result.firstResultSetOrEmpty();
    }

    public int insertStudentExam(int studentId, int examId, Timestamp startTime, Timestamp endTime) throws SQLException {
        StoredProcResult result = executor.execute(
                StoredProcedures.INSERT_STUDENT_EXAM,
                SqlParam.in(Types.INTEGER, studentId),
                SqlParam.in(Types.INTEGER, examId),
                SqlParam.in(Types.TIMESTAMP, startTime),
                SqlParam.in(Types.TIMESTAMP, endTime)
        );
        return readNewId(result, "NewStudentExamID");
    }

    public void updateStudentExam(int studentExamId, Timestamp startTime, Timestamp endTime, Integer totalGrade) throws SQLException {
        executor.execute(
                StoredProcedures.UPDATE_STUDENT_EXAM,
                SqlParam.in(Types.INTEGER, studentExamId),
                SqlParam.in(Types.TIMESTAMP, startTime),
                SqlParam.in(Types.TIMESTAMP, endTime),
                SqlParam.in(Types.INTEGER, totalGrade)
        );
    }

    public void deleteStudentExam(int studentExamId) throws SQLException {
        executor.execute(
                StoredProcedures.DELETE_STUDENT_EXAM,
                SqlParam.in(Types.INTEGER, studentExamId)
        );
    }

    public List<Map<String, Object>> selectStudentExam(Integer studentExamId, Integer studentId, Integer examId) throws SQLException {
        StoredProcResult result = executor.execute(
                StoredProcedures.SELECT_STUDENT_EXAM,
                SqlParam.in(Types.INTEGER, studentExamId),
                SqlParam.in(Types.INTEGER, studentId),
                SqlParam.in(Types.INTEGER, examId)
        );
        return result.firstResultSetOrEmpty();
    }

    public int insertStudentAnswer(int studentExamId, int questionId, int chosenOptionId) throws SQLException {
        StoredProcResult result = executor.execute(
                StoredProcedures.INSERT_STUDENT_ANSWER,
                SqlParam.in(Types.INTEGER, studentExamId),
                SqlParam.in(Types.INTEGER, questionId),
                SqlParam.in(Types.INTEGER, chosenOptionId)
        );
        return readNewId(result, "NewStudentAnswerID");
    }

    public void updateStudentAnswer(int studentAnswerId, int chosenOptionId) throws SQLException {
        executor.execute(
                StoredProcedures.UPDATE_STUDENT_ANSWER,
                SqlParam.in(Types.INTEGER, studentAnswerId),
                SqlParam.in(Types.INTEGER, chosenOptionId)
        );
    }

    public void deleteStudentAnswer(int studentAnswerId) throws SQLException {
        executor.execute(
                StoredProcedures.DELETE_STUDENT_ANSWER,
                SqlParam.in(Types.INTEGER, studentAnswerId)
        );
    }

    public List<Map<String, Object>> selectStudentAnswer(Integer studentAnswerId, Integer studentExamId) throws SQLException {
        StoredProcResult result = executor.execute(
                StoredProcedures.SELECT_STUDENT_ANSWER,
                SqlParam.in(Types.INTEGER, studentAnswerId),
                SqlParam.in(Types.INTEGER, studentExamId)
        );
        return result.firstResultSetOrEmpty();
    }

    private static int readNewId(StoredProcResult result, String key) throws SQLException {
        List<Map<String, Object>> rows = result.firstResultSetOrEmpty();
        if (rows.isEmpty()) {
            throw new SQLException("Stored procedure did not return " + key + ".");
        }
        Object value = rows.get(0).get(key);
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).intValue();
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }
}


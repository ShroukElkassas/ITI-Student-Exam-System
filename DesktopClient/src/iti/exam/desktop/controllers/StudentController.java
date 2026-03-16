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

public final class StudentController {
    private final StoredProcExecutor executor;

    public StudentController(StoredProcExecutor executor) {
        this.executor = executor;
    }

    public int insertStudent(String name, String email, String phone) throws SQLException {
        StoredProcResult result = executor.execute(
                StoredProcedures.INSERT_STUDENT,
                SqlParam.in(Types.NVARCHAR, name),
                SqlParam.in(Types.NVARCHAR, email),
                SqlParam.in(Types.NVARCHAR, phone)
        );
        List<Map<String, Object>> rows = result.firstResultSetOrEmpty();
        if (rows.isEmpty()) {
            return 0;
        }
        Object value = rows.get(0).get("NewStudentID");
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).intValue();
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    public void updateStudent(int studentId, String name, String email, String phone) throws SQLException {
        executor.execute(
                StoredProcedures.UPDATE_STUDENT,
                SqlParam.in(Types.INTEGER, studentId),
                SqlParam.in(Types.NVARCHAR, name),
                SqlParam.in(Types.NVARCHAR, email),
                SqlParam.in(Types.NVARCHAR, phone)
        );
    }

    public void deleteStudent(int studentId) throws SQLException {
        executor.execute(
                StoredProcedures.DELETE_STUDENT,
                SqlParam.in(Types.INTEGER, studentId)
        );
    }

    public List<Map<String, Object>> selectStudents(Integer studentId) throws SQLException {
        StoredProcResult result = executor.execute(
                StoredProcedures.SELECT_STUDENT,
                SqlParam.in(Types.INTEGER, studentId)
        );
        return result.firstResultSetOrEmpty();
    }

    public void assignStudentToTrack(int studentId, int trackId) throws SQLException {
        executor.execute(
                StoredProcedures.INSERT_STUDENT_TRACK,
                SqlParam.in(Types.INTEGER, studentId),
                SqlParam.in(Types.INTEGER, trackId)
        );
    }
}

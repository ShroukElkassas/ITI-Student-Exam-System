package iti.exam.desktop.controllers;

import iti.exam.desktop.db.SqlParam;
import iti.exam.desktop.db.StoredProcExecutor;
import iti.exam.desktop.db.StoredProcResult;
import iti.exam.desktop.db.StoredProcedures;

import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

public final class ReportController {
    private final StoredProcExecutor executor;

    public ReportController(StoredProcExecutor executor) {
        this.executor = executor;
    }

    public List<Map<String, Object>> studentsByDepartment(int departmentNo) throws SQLException {
        StoredProcResult result = executor.execute(
                StoredProcedures.REPORT_STUDENTS_BY_DEPARTMENT,
                SqlParam.in(Types.INTEGER, departmentNo)
        );
        return result.firstResultSetOrEmpty();
    }

    public List<Map<String, Object>> studentGrades(int studentId) throws SQLException {
        StoredProcResult result = executor.execute(
                StoredProcedures.REPORT_STUDENT_GRADES,
                SqlParam.in(Types.INTEGER, studentId)
        );
        return result.firstResultSetOrEmpty();
    }

    public List<Map<String, Object>> instructorCourses(int instructorId) throws SQLException {
        StoredProcResult result = executor.execute(
                StoredProcedures.REPORT_INSTRUCTOR_COURSES,
                SqlParam.in(Types.INTEGER, instructorId)
        );
        return result.firstResultSetOrEmpty();
    }
}


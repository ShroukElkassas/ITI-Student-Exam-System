package iti.exam.desktop.controllers;

import iti.exam.desktop.db.SqlParam;
import iti.exam.desktop.db.StoredProcExecutor;
import iti.exam.desktop.db.StoredProcResult;
import iti.exam.desktop.db.StoredProcedures;

import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

public final class InstructorController {
    private final StoredProcExecutor executor;

    public InstructorController(StoredProcExecutor executor) {
        this.executor = executor;
    }

    public void insertInstructor(String name, String email, int departmentNo) throws SQLException {
        executor.execute(
                StoredProcedures.INSERT_INSTRUCTOR,
                SqlParam.in(Types.NVARCHAR, name),
                SqlParam.in(Types.NVARCHAR, email),
                SqlParam.in(Types.INTEGER, departmentNo)
        );
    }

    public void updateInstructor(int instructorId, String name, String email, int departmentNo) throws SQLException {
        executor.execute(
                StoredProcedures.UPDATE_INSTRUCTOR,
                SqlParam.in(Types.INTEGER, instructorId),
                SqlParam.in(Types.NVARCHAR, name),
                SqlParam.in(Types.NVARCHAR, email),
                SqlParam.in(Types.INTEGER, departmentNo)
        );
    }

    public void assignInstructorToCourse(int instructorId, int courseId) throws SQLException {
        executor.execute(
                StoredProcedures.ASSIGN_INSTRUCTOR_TO_COURSE,
                SqlParam.in(Types.INTEGER, instructorId),
                SqlParam.in(Types.INTEGER, courseId)
        );
    }

    public void deleteInstructorCourse(int instructorId, int courseId) throws SQLException {
        executor.execute(
                StoredProcedures.DELETE_INSTRUCTOR_COURSE,
                SqlParam.in(Types.INTEGER, instructorId),
                SqlParam.in(Types.INTEGER, courseId)
        );
    }

    public List<Map<String, Object>> selectInstructorCourse(Integer instructorId, Integer courseId) throws SQLException {
        StoredProcResult result = executor.execute(
                StoredProcedures.SELECT_INSTRUCTOR_COURSE,
                SqlParam.in(Types.INTEGER, instructorId),
                SqlParam.in(Types.INTEGER, courseId)
        );
        return result.firstResultSetOrEmpty();
    }
}


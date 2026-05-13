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

    public void deleteInstructor(int instructorId) throws SQLException {
        executor.execute(
                StoredProcedures.DELETE_INSTRUCTOR,
                SqlParam.in(Types.INTEGER, instructorId)
        );
    }

    public List<Map<String, Object>> selectInstructors(Integer instructorId, Integer departmentNo) throws SQLException {
        StoredProcResult result = executor.execute(
                StoredProcedures.SELECT_INSTRUCTOR,
                SqlParam.in(Types.INTEGER, instructorId),
                SqlParam.in(Types.INTEGER, departmentNo)
        );
        return result.firstResultSetOrEmpty();
    }

    public void assignInstructorToCourse(int instructorId, int courseId) throws SQLException {
        try {
            executor.execute(
                    StoredProcedures.ASSIGN_INSTRUCTOR_TO_COURSE,
                    SqlParam.in(Types.INTEGER, instructorId),
                    SqlParam.in(Types.INTEGER, courseId)
            );
        } catch (SQLException ex) {
            if (isMissingProcedure(ex)) {
                executor.execute(
                        StoredProcedures.INSERT_INSTRUCTOR_COURSE,
                        SqlParam.in(Types.INTEGER, instructorId),
                        SqlParam.in(Types.INTEGER, courseId)
                );
                return;
            }
            throw ex;
        }
    }

    public void updateInstructorCourse(int instructorId, int courseId, int newInstructorId, int newCourseId) throws SQLException {
        executor.execute(
                StoredProcedures.UPDATE_INSTRUCTOR_COURSE,
                SqlParam.in(Types.INTEGER, instructorId),
                SqlParam.in(Types.INTEGER, courseId),
                SqlParam.in(Types.INTEGER, newInstructorId),
                SqlParam.in(Types.INTEGER, newCourseId)
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

    private static boolean isMissingProcedure(SQLException ex) {
        String msg = ex.getMessage();
        if (msg == null) {
            return false;
        }
        msg = msg.toLowerCase();
        return msg.contains("could not find stored procedure") || msg.contains("not found");
    }
}

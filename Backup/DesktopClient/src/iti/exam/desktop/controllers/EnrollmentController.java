package iti.exam.desktop.controllers;

import iti.exam.desktop.db.SqlParam;
import iti.exam.desktop.db.StoredProcExecutor;
import iti.exam.desktop.db.StoredProcResult;
import iti.exam.desktop.db.StoredProcedures;

import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

public final class EnrollmentController {
    private final StoredProcExecutor executor;

    public EnrollmentController(StoredProcExecutor executor) {
        this.executor = executor;
    }

    public void insertStudentTrack(int studentId, int trackId) throws SQLException {
        executor.execute(
                StoredProcedures.INSERT_STUDENT_TRACK,
                SqlParam.in(Types.INTEGER, studentId),
                SqlParam.in(Types.INTEGER, trackId)
        );
    }

    public void updateStudentTrack(int studentId, int trackId, int newStudentId, int newTrackId) throws SQLException {
        executor.execute(
                StoredProcedures.UPDATE_STUDENT_TRACK,
                SqlParam.in(Types.INTEGER, studentId),
                SqlParam.in(Types.INTEGER, trackId),
                SqlParam.in(Types.INTEGER, newStudentId),
                SqlParam.in(Types.INTEGER, newTrackId)
        );
    }

    public void deleteStudentTrack(int studentId, int trackId) throws SQLException {
        executor.execute(
                StoredProcedures.DELETE_STUDENT_TRACK,
                SqlParam.in(Types.INTEGER, studentId),
                SqlParam.in(Types.INTEGER, trackId)
        );
    }

    public List<Map<String, Object>> selectStudentTrack(Integer studentId, Integer trackId) throws SQLException {
        StoredProcResult result = executor.execute(
                StoredProcedures.SELECT_STUDENT_TRACK,
                SqlParam.in(Types.INTEGER, studentId),
                SqlParam.in(Types.INTEGER, trackId)
        );
        return result.firstResultSetOrEmpty();
    }

    public void insertTrackCourse(int trackId, int courseId) throws SQLException {
        executor.execute(
                StoredProcedures.INSERT_TRACK_COURSE,
                SqlParam.in(Types.INTEGER, trackId),
                SqlParam.in(Types.INTEGER, courseId)
        );
    }

    public void updateTrackCourse(int trackId, int courseId, int newTrackId, int newCourseId) throws SQLException {
        executor.execute(
                StoredProcedures.UPDATE_TRACK_COURSE,
                SqlParam.in(Types.INTEGER, trackId),
                SqlParam.in(Types.INTEGER, courseId),
                SqlParam.in(Types.INTEGER, newTrackId),
                SqlParam.in(Types.INTEGER, newCourseId)
        );
    }

    public void deleteTrackCourse(int trackId, int courseId) throws SQLException {
        executor.execute(
                StoredProcedures.DELETE_TRACK_COURSE,
                SqlParam.in(Types.INTEGER, trackId),
                SqlParam.in(Types.INTEGER, courseId)
        );
    }

    public List<Map<String, Object>> selectTrackCourse(Integer trackId, Integer courseId) throws SQLException {
        StoredProcResult result = executor.execute(
                StoredProcedures.SELECT_TRACK_COURSE,
                SqlParam.in(Types.INTEGER, trackId),
                SqlParam.in(Types.INTEGER, courseId)
        );
        return result.firstResultSetOrEmpty();
    }
}


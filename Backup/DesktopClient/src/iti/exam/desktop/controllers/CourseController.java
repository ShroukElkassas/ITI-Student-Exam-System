package iti.exam.desktop.controllers;

import iti.exam.desktop.db.SqlParam;
import iti.exam.desktop.db.StoredProcExecutor;
import iti.exam.desktop.db.StoredProcResult;
import iti.exam.desktop.db.StoredProcedures;
import iti.exam.desktop.models.Course;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CourseController {
    private final StoredProcExecutor executor;

    public CourseController(StoredProcExecutor executor) {
        this.executor = executor;
    }

    public int insertCourse(String courseName, Integer minDegree, Integer maxDegree) throws SQLException {
        StoredProcResult result = executor.execute(
                StoredProcedures.INSERT_COURSE,
                SqlParam.in(Types.NVARCHAR, courseName),
                SqlParam.in(Types.INTEGER, minDegree),
                SqlParam.in(Types.INTEGER, maxDegree)
        );

        List<Map<String, Object>> rows = result.firstResultSetOrEmpty();
        if (rows.isEmpty()) {
            throw new SQLException("InsertCourse did not return NewCourseID.");
        }
        Object value = rows.get(0).get("NewCourseID");
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).intValue();
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    public void updateCourse(int courseId, String courseName, Integer minDegree, Integer maxDegree) throws SQLException {
        executor.execute(
                StoredProcedures.UPDATE_COURSE,
                SqlParam.in(Types.INTEGER, courseId),
                SqlParam.in(Types.NVARCHAR, courseName),
                SqlParam.in(Types.INTEGER, minDegree),
                SqlParam.in(Types.INTEGER, maxDegree)
        );
    }

    public void deleteCourse(int courseId) throws SQLException {
        executor.execute(
                StoredProcedures.DELETE_COURSE,
                SqlParam.in(Types.INTEGER, courseId)
        );
    }

    public List<Course> selectCourses(Integer courseId, Integer trackId) throws SQLException {
        StoredProcResult result = executor.execute(
                StoredProcedures.SELECT_COURSE_BY_TRACK,
                SqlParam.in(Types.INTEGER, courseId),
                SqlParam.in(Types.INTEGER, trackId)
        );

        List<Course> courses = new ArrayList<Course>();
        for (Map<String, Object> row : result.firstResultSetOrEmpty()) {
            int id = toInt(row.get("CourseID"));
            String name = toString(row.get("CourseName"));
            Integer min = toNullableInt(row.get("MinDegree"));
            Integer max = toNullableInt(row.get("MaxDegree"));
            Integer tId = row.containsKey("TrackID") ? toNullableInt(row.get("TrackID")) : null;
            String tName = row.containsKey("TrackName") ? toString(row.get("TrackName")) : null;
            courses.add(new Course(id, name, min, max, tId, tName));
        }
        return courses;
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

    private static Integer toNullableInt(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).intValue();
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.valueOf(String.valueOf(value));
    }

    private static String toString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}


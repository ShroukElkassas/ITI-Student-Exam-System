package iti.exam.desktop.controllers;

import iti.exam.desktop.db.SqlParam;
import iti.exam.desktop.db.StoredProcExecutor;
import iti.exam.desktop.db.StoredProcResult;
import iti.exam.desktop.db.StoredProcedures;
import iti.exam.desktop.models.Track;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class TrackController {
    private final StoredProcExecutor executor;

    public TrackController(StoredProcExecutor executor) {
        this.executor = executor;
    }

    public int insertTrack(String trackName, int branchId, Integer durationMonths) throws SQLException {
        StoredProcResult result = executor.execute(
                StoredProcedures.INSERT_TRACK,
                SqlParam.in(Types.NVARCHAR, trackName),
                SqlParam.in(Types.INTEGER, branchId),
                SqlParam.in(Types.INTEGER, durationMonths)
        );

        List<Map<String, Object>> rows = result.firstResultSetOrEmpty();
        if (rows.isEmpty()) {
            throw new SQLException("InsertTrack did not return NewTrackID.");
        }
        Object value = rows.get(0).get("NewTrackID");
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).intValue();
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    public void updateTrack(int trackId, String trackName, int branchId, Integer durationMonths) throws SQLException {
        executor.execute(
                StoredProcedures.UPDATE_TRACK,
                SqlParam.in(Types.INTEGER, trackId),
                SqlParam.in(Types.NVARCHAR, trackName),
                SqlParam.in(Types.INTEGER, branchId),
                SqlParam.in(Types.INTEGER, durationMonths)
        );
    }

    public void deleteTrack(int trackId) throws SQLException {
        executor.execute(
                StoredProcedures.DELETE_TRACK,
                SqlParam.in(Types.INTEGER, trackId)
        );
    }

    public List<Track> selectTracks(Integer trackId, Integer branchId) throws SQLException {
        StoredProcResult result = executor.execute(
                StoredProcedures.SELECT_TRACK_BY_BRANCH,
                SqlParam.in(Types.INTEGER, trackId),
                SqlParam.in(Types.INTEGER, branchId)
        );

        List<Track> tracks = new ArrayList<Track>();
        for (Map<String, Object> row : result.firstResultSetOrEmpty()) {
            int id = toInt(row.get("TrackID"));
            String name = toString(row.get("TrackName"));
            Integer months = toNullableInt(row.get("DurationMonths"));
            int bId = toInt(row.get("BranchID"));
            String bName = toString(row.get("BranchName"));
            tracks.add(new Track(id, name, months, bId, bName));
        }
        return tracks;
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


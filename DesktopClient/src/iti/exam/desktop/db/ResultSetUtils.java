package iti.exam.desktop.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ResultSetUtils {
    private ResultSetUtils() {
    }

    public static List<Map<String, Object>> readAllRows(ResultSet resultSet) throws SQLException {
        ResultSetMetaData meta = resultSet.getMetaData();
        int colCount = meta.getColumnCount();

        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        while (resultSet.next()) {
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            for (int i = 1; i <= colCount; i++) {
                String key = meta.getColumnLabel(i);
                if (key == null || key.trim().isEmpty()) {
                    key = meta.getColumnName(i);
                }
                row.put(key, resultSet.getObject(i));
            }
            rows.add(row);
        }
        return rows;
    }
}


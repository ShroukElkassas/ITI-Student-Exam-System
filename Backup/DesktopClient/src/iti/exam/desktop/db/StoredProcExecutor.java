package iti.exam.desktop.db;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class StoredProcExecutor {
    private final DbConnectionFactory connectionFactory;

    public StoredProcExecutor(DbConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        ensureDriverLoaded();
    }

    public StoredProcResult execute(String procName, SqlParam... params) throws SQLException {
        try (Connection connection = connectionFactory.openConnection()) {
            return execute(connection, procName, params);
        }
    }

    public StoredProcResult execute(Connection connection, String procName, SqlParam... params) throws SQLException {
        String callSql = buildCallSql(procName, params == null ? 0 : params.length);

        try (CallableStatement stmt = connection.prepareCall(callSql)) {
            List<Integer> outIndexes = new ArrayList<Integer>();
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    int index = i + 1;
                    SqlParam param = params[i];
                    if (param.isOut()) {
                        stmt.registerOutParameter(index, param.getSqlType());
                        outIndexes.add(index);
                    } else {
                        Object value = param.getValue();
                        int sqlType = param.getSqlType();
                        if (sqlType == Types.SQLXML && value instanceof String) {
                            stmt.setString(index, (String) value);
                        } else {
                            stmt.setObject(index, value, sqlType);
                        }
                    }
                }
            }

            boolean hasResultSet = stmt.execute();
            int updateCount = stmt.getUpdateCount();
            List<List<Map<String, Object>>> resultSets = new ArrayList<List<Map<String, Object>>>();

            while (true) {
                if (hasResultSet) {
                    try (ResultSet rs = stmt.getResultSet()) {
                        resultSets.add(ResultSetUtils.readAllRows(rs));
                    }
                } else {
                    int count = stmt.getUpdateCount();
                    if (count == -1) {
                        break;
                    }
                    updateCount = count;
                }
                hasResultSet = stmt.getMoreResults();
            }

            List<Object> outValues = new ArrayList<Object>();
            for (int i = 0; i < outIndexes.size(); i++) {
                outValues.add(stmt.getObject(outIndexes.get(i)));
            }

            return new StoredProcResult(updateCount, resultSets, outValues);
        }
    }

    private static String buildCallSql(String procName, int paramCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("{call ").append(procName).append("(");
        for (int i = 0; i < paramCount; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("?");
        }
        sb.append(")}");
        return sb.toString();
    }

    private static void ensureDriverLoaded() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("SQL Server JDBC driver not found on the classpath.", e);
        }
    }
}

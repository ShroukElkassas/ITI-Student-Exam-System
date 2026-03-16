package iti.exam.desktop.controllers;

import iti.exam.desktop.db.DbConnectionFactory;
import iti.exam.desktop.db.ResultSetUtils;
import iti.exam.desktop.db.SqlParam;
import iti.exam.desktop.db.StoredProcExecutor;
import iti.exam.desktop.db.StoredProcResult;
import iti.exam.desktop.models.StoredProcedureInfo;
import iti.exam.desktop.models.StoredProcedureParam;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ProcedureCatalogController {
    private final DbConnectionFactory connectionFactory;
    private final StoredProcExecutor executor;

    public ProcedureCatalogController(DbConnectionFactory connectionFactory, StoredProcExecutor executor) {
        this.connectionFactory = connectionFactory;
        this.executor = executor;
    }

    public List<StoredProcedureInfo> listStoredProcedures() throws SQLException {
        String sql = "SELECT SCHEMA_NAME(p.schema_id) AS SchemaName, p.name AS ProcedureName " +
                "FROM sys.procedures p " +
                "WHERE p.is_ms_shipped = 0 " +
                "ORDER BY SchemaName, ProcedureName";

        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            List<StoredProcedureInfo> result = new ArrayList<StoredProcedureInfo>();
            List<Map<String, Object>> rows = ResultSetUtils.readAllRows(rs);
            for (Map<String, Object> row : rows) {
                String schema = String.valueOf(row.get("SchemaName"));
                String name = String.valueOf(row.get("ProcedureName"));
                result.add(new StoredProcedureInfo(schema, name));
            }
            return result;
        }
    }

    public List<StoredProcedureParam> getProcedureParams(String schemaName, String procedureName) throws SQLException {
        String fullName = schemaName + "." + procedureName;
        String sql =
                "SELECT p.parameter_id AS ParameterId, p.name AS ParamName, p.is_output AS IsOutput, t.name AS TypeName " +
                "FROM sys.parameters p " +
                "JOIN sys.types t ON p.user_type_id = t.user_type_id " +
                "WHERE p.object_id = OBJECT_ID(?) " +
                "ORDER BY p.parameter_id";

        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, fullName);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Map<String, Object>> rows = ResultSetUtils.readAllRows(rs);
                List<StoredProcedureParam> params = new ArrayList<StoredProcedureParam>();
                for (Map<String, Object> row : rows) {
                    int id = ((Number) row.get("ParameterId")).intValue();
                    String name = String.valueOf(row.get("ParamName"));
                    boolean isOutput = ((Number) row.get("IsOutput")).intValue() == 1;
                    String typeName = String.valueOf(row.get("TypeName"));
                    params.add(new StoredProcedureParam(id, name, isOutput, typeName));
                }
                return params;
            }
        }
    }

    public StoredProcResult call(String schemaName, String procedureName, Map<String, Object> paramValuesByName) throws SQLException {
        List<StoredProcedureParam> params = getProcedureParams(schemaName, procedureName);
        String fullName = schemaName + "." + procedureName;

        Map<String, Object> normalized = new HashMap<String, Object>();
        if (paramValuesByName != null) {
            for (Map.Entry<String, Object> entry : paramValuesByName.entrySet()) {
                if (entry.getKey() == null) {
                    continue;
                }
                normalized.put(entry.getKey().toLowerCase(Locale.ROOT), entry.getValue());
            }
        }

        SqlParam[] sqlParams = new SqlParam[params.size()];
        for (int i = 0; i < params.size(); i++) {
            StoredProcedureParam p = params.get(i);
            int sqlType = mapSqlServerTypeToJdbcType(p.getSqlTypeName());
            if (p.isOutput()) {
                sqlParams[i] = SqlParam.out(sqlType);
            } else {
                Object val = normalized.get(p.getName().toLowerCase(Locale.ROOT));
                sqlParams[i] = SqlParam.in(sqlType, val);
            }
        }

        return executor.execute(fullName, sqlParams);
    }

    private static int mapSqlServerTypeToJdbcType(String sqlTypeName) {
        if (sqlTypeName == null) {
            return Types.OTHER;
        }
        String t = sqlTypeName.toLowerCase(Locale.ROOT);
        if ("int".equals(t)) return Types.INTEGER;
        if ("bigint".equals(t)) return Types.BIGINT;
        if ("smallint".equals(t)) return Types.SMALLINT;
        if ("tinyint".equals(t)) return Types.TINYINT;
        if ("bit".equals(t)) return Types.BIT;
        if ("nvarchar".equals(t) || "nchar".equals(t) || "ntext".equals(t)) return Types.NVARCHAR;
        if ("varchar".equals(t) || "char".equals(t) || "text".equals(t)) return Types.VARCHAR;
        if ("datetime".equals(t) || "smalldatetime".equals(t) || "datetime2".equals(t)) return Types.TIMESTAMP;
        if ("date".equals(t)) return Types.DATE;
        if ("time".equals(t)) return Types.TIME;
        if ("decimal".equals(t) || "numeric".equals(t) || "money".equals(t) || "smallmoney".equals(t)) return Types.NUMERIC;
        if ("float".equals(t)) return Types.DOUBLE;
        if ("real".equals(t)) return Types.REAL;
        if ("uniqueidentifier".equals(t)) return Types.VARCHAR;
        if ("xml".equals(t)) return Types.SQLXML;
        if ("varbinary".equals(t) || "binary".equals(t) || "image".equals(t)) return Types.VARBINARY;
        return Types.OTHER;
    }
}


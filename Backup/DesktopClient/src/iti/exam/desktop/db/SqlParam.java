package iti.exam.desktop.db;

import java.sql.Types;

public final class SqlParam {
    private final int sqlType;
    private final Object value;
    private final boolean out;

    private SqlParam(int sqlType, Object value, boolean out) {
        this.sqlType = sqlType;
        this.value = value;
        this.out = out;
    }

    public static SqlParam in(int sqlType, Object value) {
        return new SqlParam(sqlType, value, false);
    }

    public static SqlParam out(int sqlType) {
        return new SqlParam(sqlType, null, true);
    }

    public static SqlParam outNumber() {
        return out(Types.NUMERIC);
    }

    public int getSqlType() {
        return sqlType;
    }

    public Object getValue() {
        return value;
    }

    public boolean isOut() {
        return out;
    }
}


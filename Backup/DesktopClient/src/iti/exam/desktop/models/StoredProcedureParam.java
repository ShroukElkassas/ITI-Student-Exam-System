package iti.exam.desktop.models;

public final class StoredProcedureParam {
    private final int parameterId;
    private final String name;
    private final boolean output;
    private final String sqlTypeName;

    public StoredProcedureParam(int parameterId, String name, boolean output, String sqlTypeName) {
        this.parameterId = parameterId;
        this.name = name;
        this.output = output;
        this.sqlTypeName = sqlTypeName;
    }

    public int getParameterId() {
        return parameterId;
    }

    public String getName() {
        return name;
    }

    public boolean isOutput() {
        return output;
    }

    public String getSqlTypeName() {
        return sqlTypeName;
    }
}


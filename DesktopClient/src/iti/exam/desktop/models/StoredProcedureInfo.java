package iti.exam.desktop.models;

public final class StoredProcedureInfo {
    private final String schemaName;
    private final String procedureName;

    public StoredProcedureInfo(String schemaName, String procedureName) {
        this.schemaName = schemaName;
        this.procedureName = procedureName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getProcedureName() {
        return procedureName;
    }

    public String getFullName() {
        return schemaName + "." + procedureName;
    }

    @Override
    public String toString() {
        return getFullName();
    }
}


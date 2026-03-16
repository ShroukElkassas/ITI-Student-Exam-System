package iti.exam.desktop.db;

public final class DbConfigLoader {
    private DbConfigLoader() {
    }

    public static DbConfig fromEnvironment() {
        String host = envOrDefault("DB_HOST", "localhost");
        String port = envOrDefault("DB_PORT", "1433");
        String database = envOrDefault("DB_NAME", "ITI_ExaminationDB");
        String user = envOrDefault("DB_USER", "sa");
        String password = envOrDefault("DB_PASSWORD", "123456");
        return new DbConfig(host, port, database, user, password);
    }

    private static String envOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null) {
            return defaultValue;
        }
        value = value.trim();
        return value.isEmpty() ? defaultValue : value;
    }
}


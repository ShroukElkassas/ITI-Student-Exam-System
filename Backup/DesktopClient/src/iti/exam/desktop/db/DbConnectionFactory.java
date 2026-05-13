package iti.exam.desktop.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DbConnectionFactory {
    private final DbConfig config;

    public DbConnectionFactory(DbConfig config) {
        this.config = config;
    }

    public Connection openConnection() throws SQLException {
        String url = buildJdbcUrl();
        return DriverManager.getConnection(url, config.getUser(), config.getPassword());
    }

    private String buildJdbcUrl() {
        String urlFromEnv = System.getenv("DB_URL");
        if (urlFromEnv != null && !urlFromEnv.trim().isEmpty()) {
            return urlFromEnv.trim();
        }

        return "jdbc:sqlserver://" + config.getHost() + ":" + config.getPort()
                + ";databaseName=" + config.getDatabase()
                + ";encrypt=true;trustServerCertificate=true;loginTimeout=5;";
    }
}


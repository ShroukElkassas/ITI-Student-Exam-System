import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        String host = envOrDefault("DB_HOST", "localhost");
        String port = envOrDefault("DB_PORT", "1433");
        String database = envOrDefault("DB_NAME", "ITI_ExaminationDB");
        String user = envOrDefault("DB_USER", "sa");
        String password = envOrDefault("DB_PASSWORD", "123456");

        if (password == null || password.trim().isEmpty()) {
            System.err.println("Database password is missing.");
            return;
        }

        String url = System.getenv("DB_URL");
        if (url == null || url.trim().isEmpty()) {
            url = "jdbc:sqlserver://" + host + ":" + port
                    + ";databaseName=" + database
                    + ";encrypt=true;trustServerCertificate=true;loginTimeout=5;";
        }

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("SQL Server JDBC driver not found on the classpath.");
            System.err.println("Expected: com.microsoft.sqlserver:mssql-jdbc");
            return;
        }

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT DB_NAME() AS DatabaseName, SYSTEM_USER AS LoginName");
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                System.out.println("Connected successfully.");
                System.out.println("Database: " + resultSet.getString("DatabaseName"));
                System.out.println("Login: " + resultSet.getString("LoginName"));
            } else {
                System.out.println("Connected, but no result returned.");
            }
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
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

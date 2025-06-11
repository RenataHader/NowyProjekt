package org.example;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class DatabaseInitializer {

    private static final String CONFIG_PATH = "database.properties";
    private static String dbName;
    private static String dbUser;
    private static String dbPassword;
    private static String dbHost;
    private static String dbPort;

    public static void initialize() {
        loadConfiguration();

        String baseUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/";
        String fullUrl = baseUrl + dbName;

        try (Connection conn = DriverManager.getConnection(baseUrl, dbUser, dbPassword)) {
            if (!databaseExists(conn)) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("CREATE DATABASE " + dbName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (Connection conn = DriverManager.getConnection(fullUrl, dbUser, dbPassword)) {
            createTables(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void loadConfiguration() {
        try (InputStream input = new FileInputStream(CONFIG_PATH)) {
            Properties props = new Properties();
            props.load(input);

            dbName = props.getProperty("db.name");
            dbUser = props.getProperty("db.user");
            dbPassword = props.getProperty("db.password");
            dbHost = props.getProperty("db.host", "localhost");
            dbPort = props.getProperty("db.port", "3306");

        } catch (Exception e) {
            throw new RuntimeException("Nie mozna zaladować konfiguracji", e);
        }
    }

    private static boolean databaseExists(Connection conn) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getCatalogs()) {
            while (rs.next()) {
                if (dbName.equalsIgnoreCase(rs.getString(1))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS players (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS games (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    type VARCHAR(50) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS game_results (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    game_id INT,
                    player_id INT,
                    score INT,
                    winner BOOLEAN,
                    FOREIGN KEY (game_id) REFERENCES games(id),
                    FOREIGN KEY (player_id) REFERENCES players(id)
                )
            """);
        }
    }

    public static Connection getConnection() throws SQLException {
        String fullUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName;
        return DriverManager.getConnection(fullUrl, dbUser, dbPassword);
    }
}

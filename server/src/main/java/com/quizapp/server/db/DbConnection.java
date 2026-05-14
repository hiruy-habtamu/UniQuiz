package com.quizapp.server.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_NAME = "quizapp";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "root";

    public Connection getConnection() throws SQLException {
        String url = System.getenv("DB_URL");
        if (url == null || url.isBlank()) {
            url = buildJdbcUrl();
        }

        return DriverManager.getConnection(url, getSetting("DB_USER", DEFAULT_USER),
                getSetting("DB_PASSWORD", DEFAULT_PASSWORD));
    }

    private String buildJdbcUrl() {
        String host = getSetting("DB_HOST", DEFAULT_HOST);
        String port = getSetting("DB_PORT", DEFAULT_PORT);
        String database = getSetting("DB_NAME", DEFAULT_NAME);
        return "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    private String getSetting(String key, String fallback) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? fallback : value;
    }
}

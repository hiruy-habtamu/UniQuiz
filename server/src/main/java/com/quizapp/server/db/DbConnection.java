package com.quizapp.server.db;

import com.quizapp.shared.config.AppEnv;

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
        String url = AppEnv.get("DB_URL", null);
        if (url == null || url.isBlank()) {
            url = buildJdbcUrl();
        }

        return DriverManager.getConnection(url, AppEnv.get("DB_USER", DEFAULT_USER),
                AppEnv.get("DB_PASSWORD", DEFAULT_PASSWORD));
    }

    private String buildJdbcUrl() {
        String host = AppEnv.get("DB_HOST", DEFAULT_HOST);
        String port = AppEnv.get("DB_PORT", DEFAULT_PORT);
        String database = AppEnv.get("DB_NAME", DEFAULT_NAME);
        return "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }
}

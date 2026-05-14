package com.quizapp.server.dao;

import com.quizapp.server.db.DbConnection;
import com.quizapp.shared.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {
    private static final String BASE_SELECT = """
            SELECT id, username, password_hash, full_name, role, batch_id, created_at
            FROM users
            """;

    private final DbConnection dbConnection;

    public UserDao() {
        this(new DbConnection());
    }

    public UserDao(DbConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public Optional<User> findById(int id) throws SQLException {
        String sql = BASE_SELECT + "WHERE id = ?";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(resultSet));
            }
        }
    }

    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = BASE_SELECT + "WHERE username = ?";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(resultSet));
            }
        }
    }

    public List<User> findAll() throws SQLException {
        String sql = BASE_SELECT + "ORDER BY created_at DESC, id DESC";
        List<User> users = new ArrayList<>();

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                users.add(mapRow(resultSet));
            }
        }

        return users;
    }

    public List<User> findByBatchId(int batchId) throws SQLException {
        String sql = BASE_SELECT + "WHERE batch_id = ? ORDER BY full_name ASC, id ASC";
        List<User> users = new ArrayList<>();

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, batchId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(mapRow(resultSet));
                }
            }
        }

        return users;
    }

    public int insert(User user) throws SQLException {
        String sql = """
                INSERT INTO users (username, password_hash, full_name, role, batch_id)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getFullName());
            statement.setString(4, user.getRole());
            if (user.getBatchId() == null) {
                statement.setNull(5, Types.INTEGER);
            } else {
                statement.setInt(5, user.getBatchId());
            }
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    user.setId(id);
                    return id;
                }
            }
        }

        throw new SQLException("Inserting user failed: no generated key returned.");
    }

    public void updateBatch(int userId, Integer batchId) throws SQLException {
        String sql = "UPDATE users SET batch_id = ? WHERE id = ?";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (batchId == null) {
                statement.setNull(1, Types.INTEGER);
            } else {
                statement.setInt(1, batchId);
            }
            statement.setInt(2, userId);
            statement.executeUpdate();
        }
    }

    private User mapRow(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setUsername(resultSet.getString("username"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setFullName(resultSet.getString("full_name"));
        user.setRole(resultSet.getString("role"));
        int batchId = resultSet.getInt("batch_id");
        user.setBatchId(resultSet.wasNull() ? null : batchId);
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        user.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        return user;
    }
}

package com.quizapp.server.dao;

import com.quizapp.server.db.DbConnection;
import com.quizapp.shared.model.Quiz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QuizDao {
    private static final String BASE_SELECT = """
            SELECT id, title, semester_id, created_by, time_limit_secs, passing_score, status, created_at
            FROM quizzes
            """;

    private final DbConnection dbConnection;

    public QuizDao() {
        this(new DbConnection());
    }

    public QuizDao(DbConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public Optional<Quiz> findById(int id) throws SQLException {
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

    public List<Quiz> findAll() throws SQLException {
        String sql = BASE_SELECT + "ORDER BY created_at DESC, id DESC";
        List<Quiz> quizzes = new ArrayList<>();

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                quizzes.add(mapRow(resultSet));
            }
        }

        return quizzes;
    }

    public List<Quiz> findBySemesterId(int semesterId) throws SQLException {
        String sql = BASE_SELECT + "WHERE semester_id = ? ORDER BY created_at DESC, id DESC";
        List<Quiz> quizzes = new ArrayList<>();

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, semesterId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    quizzes.add(mapRow(resultSet));
                }
            }
        }

        return quizzes;
    }

    public List<Quiz> findByStatus(String status) throws SQLException {
        String sql = BASE_SELECT + "WHERE status = ? ORDER BY created_at DESC, id DESC";
        List<Quiz> quizzes = new ArrayList<>();

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    quizzes.add(mapRow(resultSet));
                }
            }
        }

        return quizzes;
    }

    public List<Quiz> findByCreatedBy(int teacherId) throws SQLException {
        String sql = BASE_SELECT + "WHERE created_by = ? ORDER BY created_at DESC, id DESC";
        List<Quiz> quizzes = new ArrayList<>();

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, teacherId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    quizzes.add(mapRow(resultSet));
                }
            }
        }

        return quizzes;
    }

    public int insert(Quiz quiz) throws SQLException {
        String sql = """
                INSERT INTO quizzes (title, semester_id, created_by, time_limit_secs, passing_score, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, quiz.getTitle());
            statement.setInt(2, quiz.getSemesterId());
            statement.setInt(3, quiz.getCreatedBy());
            statement.setInt(4, quiz.getTimeLimitSecs());
            statement.setInt(5, quiz.getPassingScore());
            statement.setString(6, quiz.getStatus());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    quiz.setId(id);
                    return id;
                }
            }
        }

        throw new SQLException("Inserting quiz failed: no generated key returned.");
    }

    public void updateStatus(int quizId, String status) throws SQLException {
        String sql = "UPDATE quizzes SET status = ? WHERE id = ?";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, quizId);
            statement.executeUpdate();
        }
    }

    private Quiz mapRow(ResultSet resultSet) throws SQLException {
        Quiz quiz = new Quiz();
        quiz.setId(resultSet.getInt("id"));
        quiz.setTitle(resultSet.getString("title"));
        quiz.setSemesterId(resultSet.getInt("semester_id"));
        quiz.setCreatedBy(resultSet.getInt("created_by"));
        quiz.setTimeLimitSecs(resultSet.getInt("time_limit_secs"));
        quiz.setPassingScore(resultSet.getInt("passing_score"));
        quiz.setStatus(resultSet.getString("status"));
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        quiz.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        return quiz;
    }
}

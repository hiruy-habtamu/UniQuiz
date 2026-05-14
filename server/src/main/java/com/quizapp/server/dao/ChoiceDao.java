package com.quizapp.server.dao;

import com.quizapp.server.db.DbConnection;
import com.quizapp.shared.model.Choice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChoiceDao {
    private static final String BASE_SELECT = "SELECT id, question_id, body, is_correct FROM choices ";

    private final DbConnection dbConnection;

    public ChoiceDao() {
        this(new DbConnection());
    }

    public ChoiceDao(DbConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public Optional<Choice> findById(int id) throws SQLException {
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

    public List<Choice> findAll() throws SQLException {
        String sql = BASE_SELECT + "ORDER BY question_id ASC, id ASC";
        List<Choice> choices = new ArrayList<>();

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                choices.add(mapRow(resultSet));
            }
        }

        return choices;
    }

    public List<Choice> findByQuestionId(int questionId) throws SQLException {
        String sql = BASE_SELECT + "WHERE question_id = ? ORDER BY id ASC";
        List<Choice> choices = new ArrayList<>();

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, questionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    choices.add(mapRow(resultSet));
                }
            }
        }

        return choices;
    }

    public int insert(Choice choice) throws SQLException {
        String sql = "INSERT INTO choices (question_id, body, is_correct) VALUES (?, ?, ?)";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, choice.getQuestionId());
            statement.setString(2, choice.getBody());
            statement.setBoolean(3, choice.isCorrect());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    choice.setId(id);
                    return id;
                }
            }
        }

        throw new SQLException("Inserting choice failed: no generated key returned.");
    }

    public Optional<Choice> findCorrectByQuestionId(int questionId) throws SQLException {
        String sql = BASE_SELECT + "WHERE question_id = ? AND is_correct = TRUE LIMIT 1";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, questionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(resultSet));
            }
        }
    }

    private Choice mapRow(ResultSet resultSet) throws SQLException {
        Choice choice = new Choice();
        choice.setId(resultSet.getInt("id"));
        choice.setQuestionId(resultSet.getInt("question_id"));
        choice.setBody(resultSet.getString("body"));
        choice.setCorrect(resultSet.getBoolean("is_correct"));
        return choice;
    }
}

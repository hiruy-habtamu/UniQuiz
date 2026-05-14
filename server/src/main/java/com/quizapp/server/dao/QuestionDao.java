package com.quizapp.server.dao;

import com.quizapp.server.db.DbConnection;
import com.quizapp.shared.model.Question;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QuestionDao {
    private static final String BASE_SELECT = "SELECT id, quiz_id, body, position FROM questions ";

    private final DbConnection dbConnection;

    public QuestionDao() {
        this(new DbConnection());
    }

    public QuestionDao(DbConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public Optional<Question> findById(int id) throws SQLException {
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

    public List<Question> findAll() throws SQLException {
        String sql = BASE_SELECT + "ORDER BY quiz_id ASC, position ASC, id ASC";
        List<Question> questions = new ArrayList<>();

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                questions.add(mapRow(resultSet));
            }
        }

        return questions;
    }

    public List<Question> findByQuizId(int quizId) throws SQLException {
        String sql = BASE_SELECT + "WHERE quiz_id = ? ORDER BY position ASC, id ASC";
        List<Question> questions = new ArrayList<>();

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, quizId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    questions.add(mapRow(resultSet));
                }
            }
        }

        return questions;
    }

    public int insert(Question question) throws SQLException {
        String sql = "INSERT INTO questions (quiz_id, body, position) VALUES (?, ?, ?)";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, question.getQuizId());
            statement.setString(2, question.getBody());
            statement.setInt(3, question.getPosition());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    question.setId(id);
                    return id;
                }
            }
        }

        throw new SQLException("Inserting question failed: no generated key returned.");
    }

    private Question mapRow(ResultSet resultSet) throws SQLException {
        Question question = new Question();
        question.setId(resultSet.getInt("id"));
        question.setQuizId(resultSet.getInt("quiz_id"));
        question.setBody(resultSet.getString("body"));
        question.setPosition(resultSet.getInt("position"));
        return question;
    }
}

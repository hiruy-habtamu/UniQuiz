package com.quizapp.server.dao;

import com.quizapp.server.db.DbConnection;
import com.quizapp.shared.model.Answer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AnswerDao {
    private static final String BASE_SELECT = """
            SELECT id, student_id, quiz_id, question_id, choice_id, answered_at, violation_count, force_submitted
            FROM answers
            """;

    private final DbConnection dbConnection;

    public AnswerDao() {
        this(new DbConnection());
    }

    public AnswerDao(DbConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public Optional<Answer> findById(int id) throws SQLException {
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

    public List<Answer> findByStudentAndQuiz(int studentId, int quizId) throws SQLException {
        String sql = BASE_SELECT + "WHERE student_id = ? AND quiz_id = ? ORDER BY question_id ASC, id ASC";
        List<Answer> answers = new ArrayList<>();

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            statement.setInt(2, quizId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    answers.add(mapRow(resultSet));
                }
            }
        }

        return answers;
    }

    public boolean hasAnyAnswerForStudentQuiz(int studentId, int quizId) throws SQLException {
        String sql = "SELECT 1 FROM answers WHERE student_id = ? AND quiz_id = ? LIMIT 1";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            statement.setInt(2, quizId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public Optional<Answer> findByStudentQuizAndQuestion(int studentId, int quizId, int questionId) throws SQLException {
        String sql = BASE_SELECT + "WHERE student_id = ? AND quiz_id = ? AND question_id = ? LIMIT 1";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            statement.setInt(2, quizId);
            statement.setInt(3, questionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(resultSet));
            }
        }
    }

    public List<Answer> findByQuizId(int quizId) throws SQLException {
        String sql = BASE_SELECT + "WHERE quiz_id = ? ORDER BY student_id ASC, question_id ASC, id ASC";
        List<Answer> answers = new ArrayList<>();

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, quizId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    answers.add(mapRow(resultSet));
                }
            }
        }

        return answers;
    }

    public int insert(Answer answer) throws SQLException {
        String sql = """
                INSERT INTO answers (student_id, quiz_id, question_id, choice_id, violation_count, force_submitted)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, answer.getStudentId());
            statement.setInt(2, answer.getQuizId());
            statement.setInt(3, answer.getQuestionId());
            statement.setInt(4, answer.getChoiceId());
            statement.setInt(5, answer.getViolationCount());
            statement.setBoolean(6, answer.isForceSubmitted());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    answer.setId(id);
                    return id;
                }
            }
        }

        throw new SQLException("Inserting answer failed: no generated key returned.");
    }

    public void updateViolationState(int answerId, int violationCount, boolean forceSubmitted) throws SQLException {
        String sql = "UPDATE answers SET violation_count = ?, force_submitted = ? WHERE id = ?";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, violationCount);
            statement.setBoolean(2, forceSubmitted);
            statement.setInt(3, answerId);
            statement.executeUpdate();
        }
    }

    private Answer mapRow(ResultSet resultSet) throws SQLException {
        Answer answer = new Answer();
        answer.setId(resultSet.getInt("id"));
        answer.setStudentId(resultSet.getInt("student_id"));
        answer.setQuizId(resultSet.getInt("quiz_id"));
        answer.setQuestionId(resultSet.getInt("question_id"));
        answer.setChoiceId(resultSet.getInt("choice_id"));
        Timestamp answeredAt = resultSet.getTimestamp("answered_at");
        answer.setAnsweredAt(answeredAt == null ? null : answeredAt.toLocalDateTime());
        answer.setViolationCount(resultSet.getInt("violation_count"));
        answer.setForceSubmitted(resultSet.getBoolean("force_submitted"));
        return answer;
    }
}

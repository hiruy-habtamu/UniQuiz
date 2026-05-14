package com.quizapp.server.dao;

import com.quizapp.server.db.DbConnection;
import com.quizapp.shared.model.Enrollment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EnrollmentDao {
    private static final String BASE_SELECT = "SELECT student_id, section_id FROM enrollments ";

    private final DbConnection dbConnection;

    public EnrollmentDao() {
        this(new DbConnection());
    }

    public EnrollmentDao(DbConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public Optional<Enrollment> findByStudentAndSection(int studentId, int sectionId) throws SQLException {
        String sql = BASE_SELECT + "WHERE student_id = ? AND section_id = ?";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            statement.setInt(2, sectionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(resultSet));
            }
        }
    }

    public List<Enrollment> findByStudentId(int studentId) throws SQLException {
        String sql = BASE_SELECT + "WHERE student_id = ? ORDER BY section_id ASC";
        List<Enrollment> enrollments = new ArrayList<>();

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    enrollments.add(mapRow(resultSet));
                }
            }
        }

        return enrollments;
    }

    public List<Enrollment> findBySectionId(int sectionId) throws SQLException {
        String sql = BASE_SELECT + "WHERE section_id = ? ORDER BY student_id ASC";
        List<Enrollment> enrollments = new ArrayList<>();

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, sectionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    enrollments.add(mapRow(resultSet));
                }
            }
        }

        return enrollments;
    }

    public void insert(Enrollment enrollment) throws SQLException {
        String sql = "INSERT INTO enrollments (student_id, section_id) VALUES (?, ?)";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, enrollment.getStudentId());
            statement.setInt(2, enrollment.getSectionId());
            statement.executeUpdate();
        }
    }

    public void delete(int studentId, int sectionId) throws SQLException {
        String sql = "DELETE FROM enrollments WHERE student_id = ? AND section_id = ?";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            statement.setInt(2, sectionId);
            statement.executeUpdate();
        }
    }

    private Enrollment mapRow(ResultSet resultSet) throws SQLException {
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(resultSet.getInt("student_id"));
        enrollment.setSectionId(resultSet.getInt("section_id"));
        return enrollment;
    }
}

package com.quizapp.server.dao;

import com.quizapp.server.db.DbConnection;
import com.quizapp.shared.model.TeacherSection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TeacherSectionDao {
    private static final String BASE_SELECT = "SELECT teacher_id, section_id FROM teacher_sections ";

    private final DbConnection dbConnection;

    public TeacherSectionDao() {
        this(new DbConnection());
    }

    public TeacherSectionDao(DbConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public Optional<TeacherSection> findByTeacherAndSection(int teacherId, int sectionId) throws SQLException {
        String sql = BASE_SELECT + "WHERE teacher_id = ? AND section_id = ?";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, teacherId);
            statement.setInt(2, sectionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(resultSet));
            }
        }
    }

    public List<TeacherSection> findByTeacherId(int teacherId) throws SQLException {
        String sql = BASE_SELECT + "WHERE teacher_id = ? ORDER BY section_id ASC";
        List<TeacherSection> assignments = new ArrayList<>();

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, teacherId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    assignments.add(mapRow(resultSet));
                }
            }
        }

        return assignments;
    }

    public List<TeacherSection> findBySectionId(int sectionId) throws SQLException {
        String sql = BASE_SELECT + "WHERE section_id = ? ORDER BY teacher_id ASC";
        List<TeacherSection> assignments = new ArrayList<>();

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, sectionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    assignments.add(mapRow(resultSet));
                }
            }
        }

        return assignments;
    }

    public void insert(TeacherSection teacherSection) throws SQLException {
        String sql = "INSERT INTO teacher_sections (teacher_id, section_id) VALUES (?, ?)";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, teacherSection.getTeacherId());
            statement.setInt(2, teacherSection.getSectionId());
            statement.executeUpdate();
        }
    }

    public void delete(int teacherId, int sectionId) throws SQLException {
        String sql = "DELETE FROM teacher_sections WHERE teacher_id = ? AND section_id = ?";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, teacherId);
            statement.setInt(2, sectionId);
            statement.executeUpdate();
        }
    }

    private TeacherSection mapRow(ResultSet resultSet) throws SQLException {
        TeacherSection assignment = new TeacherSection();
        assignment.setTeacherId(resultSet.getInt("teacher_id"));
        assignment.setSectionId(resultSet.getInt("section_id"));
        return assignment;
    }
}

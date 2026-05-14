package com.quizapp.server.dao;

import com.quizapp.server.db.DbConnection;
import com.quizapp.shared.model.Section;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SectionDao {
    private static final String BASE_SELECT = """
            SELECT id, name, batch_id, semester_id
            FROM sections
            """;

    private final DbConnection dbConnection;

    public SectionDao() {
        this(new DbConnection());
    }

    public SectionDao(DbConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public Optional<Section> findById(int id) throws SQLException {
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

    public List<Section> findAll() throws SQLException {
        String sql = BASE_SELECT + "ORDER BY semester_id DESC, batch_id ASC, name ASC";
        List<Section> sections = new ArrayList<>();

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                sections.add(mapRow(resultSet));
            }
        }

        return sections;
    }

    public List<Section> findBySemesterId(int semesterId) throws SQLException {
        String sql = BASE_SELECT + "WHERE semester_id = ? ORDER BY batch_id ASC, name ASC";
        List<Section> sections = new ArrayList<>();

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, semesterId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    sections.add(mapRow(resultSet));
                }
            }
        }

        return sections;
    }

    public Optional<Section> findByNameBatchAndSemester(String name, int batchId, int semesterId) throws SQLException {
        String sql = BASE_SELECT + "WHERE name = ? AND batch_id = ? AND semester_id = ?";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setInt(2, batchId);
            statement.setInt(3, semesterId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(resultSet));
            }
        }
    }

    public int insert(Section section) throws SQLException {
        String sql = "INSERT INTO sections (name, batch_id, semester_id) VALUES (?, ?, ?)";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, section.getName());
            statement.setInt(2, section.getBatchId());
            statement.setInt(3, section.getSemesterId());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    section.setId(id);
                    return id;
                }
            }
        }

        throw new SQLException("Inserting section failed: no generated key returned.");
    }

    private Section mapRow(ResultSet resultSet) throws SQLException {
        Section section = new Section();
        section.setId(resultSet.getInt("id"));
        section.setName(resultSet.getString("name"));
        section.setBatchId(resultSet.getInt("batch_id"));
        section.setSemesterId(resultSet.getInt("semester_id"));
        return section;
    }
}

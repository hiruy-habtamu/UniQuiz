package com.quizapp.server.dao;

import com.quizapp.server.db.DbConnection;
import com.quizapp.shared.model.Semester;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SemesterDao {
    private static final String BASE_SELECT = """
            SELECT id, academic_year_id, name, start_date, end_date, is_active, created_by
            FROM semesters
            """;

    private final DbConnection dbConnection;

    public SemesterDao() {
        this(new DbConnection());
    }

    public SemesterDao(DbConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public Optional<Semester> findById(int id) throws SQLException {
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

    public Optional<Semester> findActive() throws SQLException {
        String sql = BASE_SELECT + "WHERE is_active = TRUE LIMIT 1";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                return Optional.empty();
            }
            return Optional.of(mapRow(resultSet));
        }
    }

    public List<Semester> findAll() throws SQLException {
        String sql = BASE_SELECT + "ORDER BY start_date DESC, id DESC";
        List<Semester> semesters = new ArrayList<>();

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                semesters.add(mapRow(resultSet));
            }
        }

        return semesters;
    }

    public List<Semester> findByAcademicYearId(int academicYearId) throws SQLException {
        String sql = BASE_SELECT + "WHERE academic_year_id = ? ORDER BY start_date ASC, id ASC";
        List<Semester> semesters = new ArrayList<>();

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, academicYearId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    semesters.add(mapRow(resultSet));
                }
            }
        }

        return semesters;
    }

    public int insert(Semester semester) throws SQLException {
        String sql = """
                INSERT INTO semesters (academic_year_id, name, start_date, end_date, is_active, created_by)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, semester.getAcademicYearId());
            statement.setString(2, semester.getName());
            statement.setDate(3, Date.valueOf(semester.getStartDate()));
            statement.setDate(4, Date.valueOf(semester.getEndDate()));
            statement.setBoolean(5, semester.isActive());
            statement.setInt(6, semester.getCreatedBy());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    semester.setId(id);
                    return id;
                }
            }
        }

        throw new SQLException("Inserting semester failed: no generated key returned.");
    }

    public void deactivateAllInAcademicYearExcept(int academicYearId, int semesterId) throws SQLException {
        String sql = "UPDATE semesters SET is_active = FALSE WHERE academic_year_id = ? AND id <> ?";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, academicYearId);
            statement.setInt(2, semesterId);
            statement.executeUpdate();
        }
    }

    private Semester mapRow(ResultSet resultSet) throws SQLException {
        Semester semester = new Semester();
        semester.setId(resultSet.getInt("id"));
        semester.setAcademicYearId(resultSet.getInt("academic_year_id"));
        semester.setName(resultSet.getString("name"));
        semester.setStartDate(resultSet.getDate("start_date").toLocalDate());
        semester.setEndDate(resultSet.getDate("end_date").toLocalDate());
        semester.setActive(resultSet.getBoolean("is_active"));
        semester.setCreatedBy(resultSet.getInt("created_by"));
        return semester;
    }
}

package com.quizapp.server.dao;

import com.quizapp.server.db.DbConnection;
import com.quizapp.shared.model.AcademicYear;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AcademicYearDao {
    private static final String BASE_SELECT = """
            SELECT id, label, start_date, end_date, is_active
            FROM academic_years
            """;

    private final DbConnection dbConnection;

    public AcademicYearDao() {
        this(new DbConnection());
    }

    public AcademicYearDao(DbConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public Optional<AcademicYear> findByLabel(String label) throws SQLException {
        String sql = BASE_SELECT + "WHERE label = ?";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, label);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(resultSet));
            }
        }
    }

    public Optional<AcademicYear> findActive() throws SQLException {
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

    public List<AcademicYear> findAll() throws SQLException {
        String sql = BASE_SELECT + "ORDER BY start_date DESC, id DESC";
        List<AcademicYear> years = new ArrayList<>();

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                years.add(mapRow(resultSet));
            }
        }

        return years;
    }

    public int insert(AcademicYear academicYear) throws SQLException {
        String sql = """
                INSERT INTO academic_years (label, start_date, end_date, is_active)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, academicYear.getLabel());
            statement.setDate(2, Date.valueOf(academicYear.getStartDate()));
            statement.setDate(3, Date.valueOf(academicYear.getEndDate()));
            statement.setBoolean(4, academicYear.isActive());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    academicYear.setId(id);
                    return id;
                }
            }
        }

        throw new SQLException("Inserting academic year failed: no generated key returned.");
    }

    public void deactivateAllExcept(String label) throws SQLException {
        String sql = "UPDATE academic_years SET is_active = FALSE WHERE label <> ?";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, label);
            statement.executeUpdate();
        }
    }

    private AcademicYear mapRow(ResultSet resultSet) throws SQLException {
        AcademicYear year = new AcademicYear();
        year.setId(resultSet.getInt("id"));
        year.setLabel(resultSet.getString("label"));
        year.setStartDate(resultSet.getDate("start_date").toLocalDate());
        year.setEndDate(resultSet.getDate("end_date").toLocalDate());
        year.setActive(resultSet.getBoolean("is_active"));
        return year;
    }
}

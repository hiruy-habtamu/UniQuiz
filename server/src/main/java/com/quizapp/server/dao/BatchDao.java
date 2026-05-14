package com.quizapp.server.dao;

import com.quizapp.server.db.DbConnection;
import com.quizapp.shared.model.Batch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BatchDao {
    private static final String BASE_SELECT = """
            SELECT id, entry_year, program, created_by
            FROM batches
            """;

    private final DbConnection dbConnection;

    public BatchDao() {
        this(new DbConnection());
    }

    public BatchDao(DbConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public Optional<Batch> findById(int id) throws SQLException {
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

    public Optional<Batch> findByEntryYearAndProgram(int entryYear, String program) throws SQLException {
        String sql = BASE_SELECT + "WHERE entry_year = ? AND program = ?";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, entryYear);
            statement.setString(2, program);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(resultSet));
            }
        }
    }

    public List<Batch> findAll() throws SQLException {
        String sql = BASE_SELECT + "ORDER BY entry_year DESC, program ASC";
        List<Batch> batches = new ArrayList<>();

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                batches.add(mapRow(resultSet));
            }
        }

        return batches;
    }

    public int insert(Batch batch) throws SQLException {
        String sql = "INSERT INTO batches (entry_year, program, created_by) VALUES (?, ?, ?)";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, batch.getEntryYear());
            statement.setString(2, batch.getProgram());
            statement.setInt(3, batch.getCreatedBy());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    batch.setId(id);
                    return id;
                }
            }
        }

        throw new SQLException("Inserting batch failed: no generated key returned.");
    }

    private Batch mapRow(ResultSet resultSet) throws SQLException {
        Batch batch = new Batch();
        batch.setId(resultSet.getInt("id"));
        batch.setEntryYear(resultSet.getInt("entry_year"));
        batch.setProgram(resultSet.getString("program"));
        batch.setCreatedBy(resultSet.getInt("created_by"));
        return batch;
    }
}

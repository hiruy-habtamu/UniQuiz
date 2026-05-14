package com.quizapp.server.service;

import com.quizapp.server.dao.BatchDao;
import com.quizapp.server.dao.UserDao;
import com.quizapp.shared.model.Batch;
import com.quizapp.shared.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class BatchService {
    private final BatchDao batchDao;
    private final UserDao userDao;

    public BatchService() {
        this(new BatchDao(), new UserDao());
    }

    public BatchService(BatchDao batchDao, UserDao userDao) {
        this.batchDao = batchDao;
        this.userDao = userDao;
    }

    public List<Batch> getAllBatches() throws SQLException {
        return batchDao.findAll();
    }

    public Optional<Batch> getBatch(int id) throws SQLException {
        return batchDao.findById(id);
    }

    public int createBatch(Batch batch) throws SQLException {
        validateBatch(batch);
        User creator = userDao.findById(batch.getCreatedBy())
                .orElseThrow(() -> new IllegalArgumentException("Batch creator not found."));
        if (!"TEACHER".equalsIgnoreCase(creator.getRole())) {
            throw new IllegalArgumentException("Only teachers can create batches.");
        }
        if (batchDao.findByEntryYearAndProgram(batch.getEntryYear(), batch.getProgram()).isPresent()) {
            throw new IllegalArgumentException("Batch already exists for this entry year and program.");
        }
        return batchDao.insert(batch);
    }

    private void validateBatch(Batch batch) {
        if (batch == null) {
            throw new IllegalArgumentException("Batch is required.");
        }
        if (batch.getEntryYear() <= 0) {
            throw new IllegalArgumentException("Batch entry year is required.");
        }
        if (batch.getProgram() == null || batch.getProgram().isBlank()) {
            throw new IllegalArgumentException("Batch program is required.");
        }
    }
}

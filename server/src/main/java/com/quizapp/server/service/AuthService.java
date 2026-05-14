package com.quizapp.server.service;

import com.quizapp.server.dao.BatchDao;
import com.quizapp.server.dao.UserDao;
import com.quizapp.shared.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AuthService {
    private final UserDao userDao;
    private final BatchDao batchDao;

    public AuthService() {
        this(new UserDao(), new BatchDao());
    }

    public AuthService(UserDao userDao, BatchDao batchDao) {
        this.userDao = userDao;
        this.batchDao = batchDao;
    }

    public int register(String username, String rawPassword, String fullName, String role, Integer batchId)
            throws SQLException {
        validateRegistration(username, rawPassword, fullName, role, batchId);
        if (userDao.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists.");
        }

        boolean student = "STUDENT".equalsIgnoreCase(role);

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(BCrypt.hashpw(rawPassword, BCrypt.gensalt()));
        user.setFullName(fullName);
        user.setRole(role == null ? null : role.toUpperCase());
        user.setBatchId(student ? batchId : null);
        return userDao.insert(user);
    }

    public Optional<User> authenticate(String username, String rawPassword) throws SQLException {
        Optional<User> user = userDao.findByUsername(username);
        if (user.isEmpty()) {
            return Optional.empty();
        }

        return BCrypt.checkpw(rawPassword, user.get().getPasswordHash()) ? user : Optional.empty();
    }

    public Optional<User> getUser(int userId) throws SQLException {
        return userDao.findById(userId);
    }

    public List<User> getUsers() throws SQLException {
        return userDao.findAll();
    }

    private void validateRegistration(String username, String rawPassword, String fullName,
                                      String role, Integer batchId) throws SQLException {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name is required.");
        }
        if (!"TEACHER".equalsIgnoreCase(role) && !"STUDENT".equalsIgnoreCase(role)) {
            throw new IllegalArgumentException("Role must be TEACHER or STUDENT.");
        }
        if ("STUDENT".equalsIgnoreCase(role)) {
            if (batchId == null) {
                throw new IllegalArgumentException("Students must be assigned to a batch.");
            }
            batchDao.findById(batchId).orElseThrow(() -> new IllegalArgumentException("Batch not found."));
        } else if (batchId != null) {
            throw new IllegalArgumentException("Teachers cannot be assigned to a batch.");
        }
    }
}

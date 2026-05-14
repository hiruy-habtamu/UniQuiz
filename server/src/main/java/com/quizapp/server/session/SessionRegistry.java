package com.quizapp.server.session;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SessionRegistry {
    private final Map<String, QuizSession> sessions = new ConcurrentHashMap<>();

    public QuizSession getOrCreate(int quizId, int studentId) {
        return sessions.computeIfAbsent(key(quizId, studentId), ignored -> new QuizSession(quizId, studentId));
    }

    public Optional<QuizSession> find(int quizId, int studentId) {
        return Optional.ofNullable(sessions.get(key(quizId, studentId)));
    }

    public void remove(int quizId, int studentId) {
        sessions.remove(key(quizId, studentId));
    }

    public void removeStudentSessions(int studentId) {
        sessions.entrySet().removeIf(entry -> entry.getValue().getStudentId() == studentId);
    }

    public Collection<QuizSession> getAll() {
        return sessions.values();
    }

    private String key(int quizId, int studentId) {
        return quizId + ":" + studentId;
    }
}

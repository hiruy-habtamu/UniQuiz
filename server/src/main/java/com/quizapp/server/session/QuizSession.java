package com.quizapp.server.session;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class QuizSession {
    private final int quizId;
    private final int studentId;
    private final Instant createdAt;
    private final Set<Integer> answeredQuestionIds = Collections.synchronizedSet(new HashSet<>());

    private volatile int violationCount;
    private volatile boolean submitted;

    public QuizSession(int quizId, int studentId) {
        this.quizId = quizId;
        this.studentId = studentId;
        this.createdAt = Instant.now();
    }

    public int getQuizId() { return quizId; }
    public int getStudentId() { return studentId; }
    public Instant getCreatedAt() { return createdAt; }
    public int getViolationCount() { return violationCount; }
    public boolean isSubmitted() { return submitted; }
    public Set<Integer> getAnsweredQuestionIds() { return Set.copyOf(answeredQuestionIds); }

    public void recordAnswer(int questionId) {
        answeredQuestionIds.add(questionId);
    }

    public boolean hasAnswered(int questionId) {
        return answeredQuestionIds.contains(questionId);
    }

    public void setViolationCount(int violationCount) {
        this.violationCount = violationCount;
    }

    public void markSubmitted() {
        this.submitted = true;
    }
}

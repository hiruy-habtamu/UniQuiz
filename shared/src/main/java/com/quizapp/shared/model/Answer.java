package com.quizapp.shared.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Answer implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int studentId;
    private int quizId;
    private int questionId;
    private int choiceId;
    private LocalDateTime answeredAt;
    private int violationCount;
    private boolean forceSubmitted;

    public Answer() {}

    public int getId() { return id; }
    public int getStudentId() { return studentId; }
    public int getQuizId() { return quizId; }
    public int getQuestionId() { return questionId; }
    public int getChoiceId() { return choiceId; }
    public LocalDateTime getAnsweredAt() { return answeredAt; }
    public int getViolationCount() { return violationCount; }
    public boolean isForceSubmitted() { return forceSubmitted; }

    public void setId(int id) { this.id = id; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }
    public void setChoiceId(int choiceId) { this.choiceId = choiceId; }
    public void setAnsweredAt(LocalDateTime answeredAt) { this.answeredAt = answeredAt; }
    public void setViolationCount(int violationCount) { this.violationCount = violationCount; }
    public void setForceSubmitted(boolean forceSubmitted) { this.forceSubmitted = forceSubmitted; }
}

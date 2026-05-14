package com.quizapp.shared.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Quiz implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String title;
    private int semesterId;
    private int createdBy;
    private int timeLimitSecs;
    private int passingScore;
    private String status;
    private LocalDateTime createdAt;

    public Quiz() {}

    public int getId() { return id; }
    public String getTitle() { return title; }
    public int getSemesterId() { return semesterId; }
    public int getCreatedBy() { return createdBy; }
    public int getTimeLimitSecs() { return timeLimitSecs; }
    public int getPassingScore() { return passingScore; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setSemesterId(int semesterId) { this.semesterId = semesterId; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }
    public void setTimeLimitSecs(int timeLimitSecs) { this.timeLimitSecs = timeLimitSecs; }
    public void setPassingScore(int passingScore) { this.passingScore = passingScore; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

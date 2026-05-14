package com.quizapp.shared.model;

import java.io.Serializable;

public class Choice implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int questionId;
    private String body;
    private boolean correct;

    public Choice() {}

    public int getId() { return id; }
    public int getQuestionId() { return questionId; }
    public String getBody() { return body; }
    public boolean isCorrect() { return correct; }

    public void setId(int id) { this.id = id; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }
    public void setBody(String body) { this.body = body; }
    public void setCorrect(boolean correct) { this.correct = correct; }
}

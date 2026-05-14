package com.quizapp.shared.model;

import java.io.Serializable;

public class Question implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int quizId;
    private String body;
    private int position;

    public Question() {}

    public int getId() { return id; }
    public int getQuizId() { return quizId; }
    public String getBody() { return body; }
    public int getPosition() { return position; }

    public void setId(int id) { this.id = id; }
    public void setQuizId(int quizId) { this.quizId = quizId; }
    public void setBody(String body) { this.body = body; }
    public void setPosition(int position) { this.position = position; }
}

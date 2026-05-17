package com.quizapp.shared.model;

import java.io.Serializable;

public class Section implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private int batchId;
    private int semesterId;

    public Section() {}

    public int getId() { return id; }
    public String getName() { return name; }
    public int getBatchId() { return batchId; }
    public int getSemesterId() { return semesterId; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setBatchId(int batchId) { this.batchId = batchId; }
    public void setSemesterId(int semesterId) { this.semesterId = semesterId; }
    @Override
    public String toString() {
        return name;
    }
}

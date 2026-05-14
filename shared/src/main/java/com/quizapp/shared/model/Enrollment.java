package com.quizapp.shared.model;

import java.io.Serializable;

public class Enrollment implements Serializable {
    private static final long serialVersionUID = 1L;

    private int studentId;
    private int sectionId;

    public Enrollment() {}

    public int getStudentId() { return studentId; }
    public int getSectionId() { return sectionId; }

    public void setStudentId(int studentId) { this.studentId = studentId; }
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }
}

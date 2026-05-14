package com.quizapp.shared.model;

import java.io.Serializable;

public class TeacherSection implements Serializable {
    private static final long serialVersionUID = 1L;

    private int teacherId;
    private int sectionId;

    public TeacherSection() {}

    public int getTeacherId() { return teacherId; }
    public int getSectionId() { return sectionId; }

    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }
}

package com.quizapp.shared.message.academic;

import com.quizapp.shared.message.Message;

public class AssignStudentSectionMessage extends Message {
    private static final long serialVersionUID = 1L;

    private final int studentId;
    private final int sectionId;

    public AssignStudentSectionMessage(int studentId, int sectionId) {
        this.studentId = studentId;
        this.sectionId = sectionId;
    }

    public int getStudentId() { return studentId; }
    public int getSectionId() { return sectionId; }
}

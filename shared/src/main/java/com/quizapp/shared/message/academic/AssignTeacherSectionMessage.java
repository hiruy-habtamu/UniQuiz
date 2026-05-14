package com.quizapp.shared.message.academic;

import com.quizapp.shared.message.Message;

public class AssignTeacherSectionMessage extends Message {
    private static final long serialVersionUID = 1L;

    private final int teacherId;
    private final int sectionId;

    public AssignTeacherSectionMessage(int teacherId, int sectionId) {
        this.teacherId = teacherId;
        this.sectionId = sectionId;
    }

    public int getTeacherId() { return teacherId; }
    public int getSectionId() { return sectionId; }
}

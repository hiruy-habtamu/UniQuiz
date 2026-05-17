package com.quizapp.shared.message.quiz;

import com.quizapp.shared.message.Message;

public class GetTeacherQuizzesMessage extends Message {
    private static final long serialVersionUID = 1L;

    private final int teacherId;

    public GetTeacherQuizzesMessage(int teacherId) {
        this.teacherId = teacherId;
    }

    public int getTeacherId() {
        return teacherId;
    }
}

package com.quizapp.shared.message.quiz;

import com.quizapp.shared.message.Message;

public class GetStudentActiveQuizzesMessage extends Message {
    private static final long serialVersionUID = 1L;

    private final int studentId;

    public GetStudentActiveQuizzesMessage(int studentId) {
        this.studentId = studentId;
    }

    public int getStudentId() {
        return studentId;
    }
}

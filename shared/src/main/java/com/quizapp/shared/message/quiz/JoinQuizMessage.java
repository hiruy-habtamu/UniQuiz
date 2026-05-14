package com.quizapp.shared.message.quiz;

import com.quizapp.shared.message.Message;

public class JoinQuizMessage extends Message {
    private static final long serialVersionUID = 1L;

    private final int quizId;
    private final int studentId;

    public JoinQuizMessage(int quizId, int studentId) {
        this.quizId = quizId;
        this.studentId = studentId;
    }

    public int getQuizId() { return quizId; }
    public int getStudentId() { return studentId; }
}

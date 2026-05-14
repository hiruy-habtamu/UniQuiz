package com.quizapp.shared.message.quiz;

import com.quizapp.shared.message.Message;

public class CloseQuizMessage extends Message {
    private static final long serialVersionUID = 1L;

    private final int quizId;

    public CloseQuizMessage(int quizId) {
        this.quizId = quizId;
    }

    public int getQuizId() { return quizId; }
}

package com.quizapp.shared.message.quiz;

import com.quizapp.shared.message.Message;

public class StartQuizMessage extends Message {
    private static final long serialVersionUID = 1L;

    private final int quizId;

    public StartQuizMessage(int quizId) {
        this.quizId = quizId;
    }

    public int getQuizId() { return quizId; }
}

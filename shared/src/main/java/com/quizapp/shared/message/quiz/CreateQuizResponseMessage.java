package com.quizapp.shared.message.quiz;

import com.quizapp.shared.message.Message;

public class CreateQuizResponseMessage extends Message {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String reason;
    private final int quizId;

    public CreateQuizResponseMessage(boolean success, String reason, int quizId) {
        this.success = success;
        this.reason = reason;
        this.quizId = quizId;
    }

    public boolean isSuccess() { return success; }
    public String getReason() { return reason; }
    public int getQuizId() { return quizId; }
}

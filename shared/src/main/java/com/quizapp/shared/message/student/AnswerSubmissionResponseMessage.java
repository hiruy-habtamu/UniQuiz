package com.quizapp.shared.message.student;

import com.quizapp.shared.message.Message;

public class AnswerSubmissionResponseMessage extends Message {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String reason;
    private final int answerId;

    public AnswerSubmissionResponseMessage(boolean success, String reason, int answerId) {
        this.success = success;
        this.reason = reason;
        this.answerId = answerId;
    }

    public boolean isSuccess() { return success; }
    public String getReason() { return reason; }
    public int getAnswerId() { return answerId; }
}

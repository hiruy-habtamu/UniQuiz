package com.quizapp.shared.message.academic;

import com.quizapp.shared.message.Message;

public class ActionResponseMessage extends Message {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String reason;

    public ActionResponseMessage(boolean success, String reason) {
        this.success = success;
        this.reason = reason;
    }

    public boolean isSuccess() { return success; }
    public String getReason() { return reason; }
}

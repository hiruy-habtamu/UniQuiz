package com.quizapp.shared.message.auth;

import com.quizapp.shared.message.Message;

public class RegisterResponseMessage extends Message {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String  reason;

    public RegisterResponseMessage(boolean success, String reason) {
        this.success = success;
        this.reason  = reason;
    }

    public boolean isSuccess() { return success; }
    public String  getReason() { return reason;  }
}
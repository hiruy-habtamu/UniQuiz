package com.quizapp.shared.message.academic;

import com.quizapp.shared.message.Message;

public class SetSemesterActiveMessage extends Message {
    private static final long serialVersionUID = 1L;

    private final int semesterId;
    private final boolean active;

    public SetSemesterActiveMessage(int semesterId, boolean active) {
        this.semesterId = semesterId;
        this.active = active;
    }

    public int getSemesterId() {
        return semesterId;
    }

    public boolean isActive() {
        return active;
    }
}

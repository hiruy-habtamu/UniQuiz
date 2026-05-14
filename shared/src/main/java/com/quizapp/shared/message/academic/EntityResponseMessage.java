package com.quizapp.shared.message.academic;

import com.quizapp.shared.message.Message;

public class EntityResponseMessage extends Message {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String reason;
    private final int entityId;

    public EntityResponseMessage(boolean success, String reason, int entityId) {
        this.success = success;
        this.reason = reason;
        this.entityId = entityId;
    }

    public boolean isSuccess() { return success; }
    public String getReason() { return reason; }
    public int getEntityId() { return entityId; }
}

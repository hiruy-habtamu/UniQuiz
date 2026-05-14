package com.quizapp.shared.message.academic;

import com.quizapp.shared.message.Message;

public class GetSectionsMessage extends Message {
    private static final long serialVersionUID = 1L;

    private final int batchId;

    public GetSectionsMessage(int batchId) {
        this.batchId = batchId;
    }

    public int getBatchId() { return batchId; }
}

package com.quizapp.shared.message.academic;

import com.quizapp.shared.message.Message;
import com.quizapp.shared.model.Batch;

public class CreateBatchMessage extends Message {
    private static final long serialVersionUID = 1L;

    private final Batch batch;

    public CreateBatchMessage(Batch batch) {
        this.batch = batch;
    }

    public Batch getBatch() { return batch; }
}

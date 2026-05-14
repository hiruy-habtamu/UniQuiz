package com.quizapp.shared.message.academic;

import com.quizapp.shared.message.Message;
import com.quizapp.shared.model.Batch;

import java.util.List;

public class GetBatchesResponseMessage extends Message {
    private static final long serialVersionUID = 1L;

    private final List<Batch> batches;

    public GetBatchesResponseMessage(List<Batch> batches) {
        this.batches = batches;
    }

    public List<Batch> getBatches() { return batches; }
}

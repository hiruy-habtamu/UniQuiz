package com.quizapp.shared.message.academic;

import com.quizapp.shared.message.Message;
import com.quizapp.shared.model.Section;

import java.util.List;

public class GetAllSectionsResponseMessage extends Message {
    private static final long serialVersionUID = 1L;

    private final List<Section> sections;

    public GetAllSectionsResponseMessage(List<Section> sections) {
        this.sections = sections;
    }

    public List<Section> getSections() {
        return sections;
    }
}

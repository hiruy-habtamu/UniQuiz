package com.quizapp.shared.message.academic;

import com.quizapp.shared.message.Message;
import com.quizapp.shared.model.Section;

public class CreateSectionMessage extends Message {
    private static final long serialVersionUID = 1L;

    private final Section section;

    public CreateSectionMessage(Section section) {
        this.section = section;
    }

    public Section getSection() { return section; }
}

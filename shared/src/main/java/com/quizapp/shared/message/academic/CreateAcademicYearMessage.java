package com.quizapp.shared.message.academic;

import com.quizapp.shared.message.Message;

public class CreateAcademicYearMessage extends Message {
    private static final long serialVersionUID = 1L;

    private final int startYear;
    private final boolean active;

    public CreateAcademicYearMessage(int startYear, boolean active) {
        this.startYear = startYear;
        this.active = active;
    }

    public int getStartYear() {
        return startYear;
    }

    public boolean isActive() {
        return active;
    }
}

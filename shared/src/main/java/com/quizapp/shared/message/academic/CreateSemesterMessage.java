package com.quizapp.shared.message.academic;

import com.quizapp.shared.message.Message;
import com.quizapp.shared.model.Semester;

public class CreateSemesterMessage extends Message {
    private static final long serialVersionUID = 1L;

    private final Semester semester;

    public CreateSemesterMessage(Semester semester) {
        this.semester = semester;
    }

    public Semester getSemester() { return semester; }
}

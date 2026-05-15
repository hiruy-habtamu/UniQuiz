package com.quizapp.shared.message.academic;

import com.quizapp.shared.message.Message;
import com.quizapp.shared.model.Semester;

import java.util.List;

public class GetSemestersResponseMessage extends Message {
    private static final long serialVersionUID = 1L;

    private final List<Semester> semesters;

    public GetSemestersResponseMessage(List<Semester> semesters) {
        this.semesters = semesters;
    }

    public List<Semester> getSemesters() {
        return semesters;
    }
}

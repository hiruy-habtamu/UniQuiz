package com.quizapp.shared.message.academic;

import com.quizapp.shared.message.Message;
import com.quizapp.shared.model.AcademicYear;

import java.util.List;

public class GetAcademicYearsResponseMessage extends Message {
    private static final long serialVersionUID = 1L;

    private final List<AcademicYear> academicYears;

    public GetAcademicYearsResponseMessage(List<AcademicYear> academicYears) {
        this.academicYears = academicYears;
    }

    public List<AcademicYear> getAcademicYears() {
        return academicYears;
    }
}

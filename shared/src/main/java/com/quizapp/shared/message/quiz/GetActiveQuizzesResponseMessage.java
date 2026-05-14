package com.quizapp.shared.message.quiz;

import com.quizapp.shared.message.Message;
import com.quizapp.shared.model.Quiz;

import java.util.List;

public class GetActiveQuizzesResponseMessage extends Message {
    private static final long serialVersionUID = 1L;

    private final List<Quiz> quizzes;

    public GetActiveQuizzesResponseMessage(List<Quiz> quizzes) {
        this.quizzes = quizzes;
    }

    public List<Quiz> getQuizzes() { return quizzes; }
}

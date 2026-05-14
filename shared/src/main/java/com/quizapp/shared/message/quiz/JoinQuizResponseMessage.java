package com.quizapp.shared.message.quiz;

import com.quizapp.shared.message.Message;
import com.quizapp.shared.model.Quiz;

import java.util.List;

public class JoinQuizResponseMessage extends Message {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String reason;
    private final Quiz quiz;
    private final List<CreateQuizMessage.QuestionPayload> questions;

    public JoinQuizResponseMessage(boolean success, String reason, Quiz quiz,
                                   List<CreateQuizMessage.QuestionPayload> questions) {
        this.success = success;
        this.reason = reason;
        this.quiz = quiz;
        this.questions = questions;
    }

    public boolean isSuccess() { return success; }
    public String getReason() { return reason; }
    public Quiz getQuiz() { return quiz; }
    public List<CreateQuizMessage.QuestionPayload> getQuestions() { return questions; }
}

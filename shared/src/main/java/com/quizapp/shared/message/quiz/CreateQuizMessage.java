package com.quizapp.shared.message.quiz;

import com.quizapp.shared.message.Message;
import com.quizapp.shared.model.Quiz;

import java.io.Serializable;
import java.util.List;

public class CreateQuizMessage extends Message {
    private static final long serialVersionUID = 1L;

    private final Quiz quiz;
    private final List<QuestionPayload> questions;

    public CreateQuizMessage(Quiz quiz, List<QuestionPayload> questions) {
        this.quiz = quiz;
        this.questions = questions;
    }

    public Quiz getQuiz() { return quiz; }
    public List<QuestionPayload> getQuestions() { return questions; }

    public static class QuestionPayload implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String body;
        private final int position;
        private final List<ChoicePayload> choices;

        public QuestionPayload(String body, int position, List<ChoicePayload> choices) {
            this.body = body;
            this.position = position;
            this.choices = choices;
        }

        public String getBody() { return body; }
        public int getPosition() { return position; }
        public List<ChoicePayload> getChoices() { return choices; }
    }

    public static class ChoicePayload implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String body;
        private final boolean correct;

        public ChoicePayload(String body, boolean correct) {
            this.body = body;
            this.correct = correct;
        }

        public String getBody() { return body; }
        public boolean isCorrect() { return correct; }
    }
}

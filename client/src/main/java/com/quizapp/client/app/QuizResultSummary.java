package com.quizapp.client.app;

public class QuizResultSummary {
    private final String quizTitle;
    private final int totalQuestions;
    private final int answeredQuestions;
    private final int correctAnswers;
    private final int percentage;
    private final boolean passed;
    private final String finishReason;

    public QuizResultSummary(String quizTitle, int totalQuestions, int answeredQuestions,
                             int correctAnswers, int percentage, boolean passed, String finishReason) {
        this.quizTitle = quizTitle;
        this.totalQuestions = totalQuestions;
        this.answeredQuestions = answeredQuestions;
        this.correctAnswers = correctAnswers;
        this.percentage = percentage;
        this.passed = passed;
        this.finishReason = finishReason;
    }

    public String getQuizTitle() { return quizTitle; }
    public int getTotalQuestions() { return totalQuestions; }
    public int getAnsweredQuestions() { return answeredQuestions; }
    public int getCorrectAnswers() { return correctAnswers; }
    public int getPercentage() { return percentage; }
    public boolean isPassed() { return passed; }
    public String getFinishReason() { return finishReason; }
}

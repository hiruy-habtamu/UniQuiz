package com.quizapp.shared.message.student;

import com.quizapp.shared.message.Message;

public class AnswerSubmissionMessage extends Message {
    private static final long serialVersionUID = 1L;

    private final int studentId;
    private final int quizId;
    private final int questionId;
    private final int choiceId;
    private final int violationCount;
    private final boolean forceSubmitted;

    public AnswerSubmissionMessage(int studentId, int quizId, int questionId, int choiceId,
                                   int violationCount, boolean forceSubmitted) {
        this.studentId = studentId;
        this.quizId = quizId;
        this.questionId = questionId;
        this.choiceId = choiceId;
        this.violationCount = violationCount;
        this.forceSubmitted = forceSubmitted;
    }

    public int getStudentId() { return studentId; }
    public int getQuizId() { return quizId; }
    public int getQuestionId() { return questionId; }
    public int getChoiceId() { return choiceId; }
    public int getViolationCount() { return violationCount; }
    public boolean isForceSubmitted() { return forceSubmitted; }
}

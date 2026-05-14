package com.quizapp.server.service;

import com.quizapp.server.dao.AnswerDao;
import com.quizapp.server.dao.ChoiceDao;
import com.quizapp.server.dao.QuestionDao;
import com.quizapp.shared.model.Answer;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoringService {
    private final AnswerDao answerDao;
    private final ChoiceDao choiceDao;
    private final QuestionDao questionDao;

    public ScoringService() {
        this(new AnswerDao(), new ChoiceDao(), new QuestionDao());
    }

    public ScoringService(AnswerDao answerDao, ChoiceDao choiceDao, QuestionDao questionDao) {
        this.answerDao = answerDao;
        this.choiceDao = choiceDao;
        this.questionDao = questionDao;
    }

    public int calculateScore(int studentId, int quizId) throws SQLException {
        List<Answer> answers = answerDao.findByStudentAndQuiz(studentId, quizId);
        int correct = 0;

        for (Answer answer : answers) {
            if (choiceDao.findById(answer.getChoiceId()).map(choice -> choice.isCorrect()).orElse(false)) {
                correct++;
            }
        }

        return correct;
    }

    public int calculateScorePercentage(int studentId, int quizId) throws SQLException {
        int totalQuestions = questionDao.findByQuizId(quizId).size();
        if (totalQuestions == 0) {
            return 0;
        }
        return (calculateScore(studentId, quizId) * 100) / totalQuestions;
    }

    public Map<Integer, Integer> calculateQuizScores(int quizId) throws SQLException {
        List<Answer> answers = answerDao.findByQuizId(quizId);
        Map<Integer, Integer> scores = new HashMap<>();

        for (Answer answer : answers) {
            boolean correct = choiceDao.findById(answer.getChoiceId())
                    .map(choice -> choice.isCorrect())
                    .orElse(false);
            if (correct) {
                scores.merge(answer.getStudentId(), 1, Integer::sum);
            } else {
                scores.putIfAbsent(answer.getStudentId(), 0);
            }
        }

        return scores;
    }
}

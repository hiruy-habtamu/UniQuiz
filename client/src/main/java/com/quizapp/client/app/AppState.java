package com.quizapp.client.app;

import com.quizapp.client.network.MessageDispatcher;
import com.quizapp.shared.message.quiz.CreateQuizMessage;
import com.quizapp.shared.model.Quiz;
import com.quizapp.shared.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class AppState {
    private final MessageDispatcher messageDispatcher;

    private AppRoute currentRoute;
    private User loggedInUser;
    private Quiz currentQuiz;
    private List<CreateQuizMessage.QuestionPayload> currentQuizQuestions;
    private final Map<Integer, Integer> selectedChoiceByQuestionId = new HashMap<>();
    private final Set<Integer> submittedQuestionIds = new HashSet<>();
    private QuizResultSummary lastQuizResult;

    public AppState(MessageDispatcher messageDispatcher) {
        this.messageDispatcher = messageDispatcher;
    }

    public MessageDispatcher getMessageDispatcher() {
        return messageDispatcher;
    }

    public AppRoute getCurrentRoute() {
        return currentRoute;
    }

    public void setCurrentRoute(AppRoute currentRoute) {
        this.currentRoute = currentRoute;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    public Quiz getCurrentQuiz() {
        return currentQuiz;
    }

    public void setCurrentQuiz(Quiz currentQuiz) {
        this.currentQuiz = currentQuiz;
    }

    public List<CreateQuizMessage.QuestionPayload> getCurrentQuizQuestions() {
        return currentQuizQuestions;
    }

    public void setCurrentQuizQuestions(List<CreateQuizMessage.QuestionPayload> currentQuizQuestions) {
        this.currentQuizQuestions = currentQuizQuestions;
    }

    public Map<Integer, Integer> getSelectedChoiceByQuestionId() {
        return selectedChoiceByQuestionId;
    }

    public Set<Integer> getSubmittedQuestionIds() {
        return submittedQuestionIds;
    }

    public QuizResultSummary getLastQuizResult() {
        return lastQuizResult;
    }

    public void setLastQuizResult(QuizResultSummary lastQuizResult) {
        this.lastQuizResult = lastQuizResult;
    }

    public void clearCurrentQuizState() {
        currentQuiz = null;
        currentQuizQuestions = null;
        selectedChoiceByQuestionId.clear();
        submittedQuestionIds.clear();
    }

    public void clearSession() {
        loggedInUser = null;
        clearCurrentQuizState();
        lastQuizResult = null;
    }
}

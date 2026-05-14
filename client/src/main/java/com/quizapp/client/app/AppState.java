package com.quizapp.client.app;

import com.quizapp.client.network.MessageDispatcher;
import com.quizapp.shared.message.quiz.CreateQuizMessage;
import com.quizapp.shared.model.Quiz;
import com.quizapp.shared.model.User;

import java.util.List;

public class AppState {
    private final MessageDispatcher messageDispatcher;

    private AppRoute currentRoute;
    private User loggedInUser;
    private Quiz currentQuiz;
    private List<CreateQuizMessage.QuestionPayload> currentQuizQuestions;

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

    public void clearSession() {
        loggedInUser = null;
        currentQuiz = null;
        currentQuizQuestions = null;
    }
}

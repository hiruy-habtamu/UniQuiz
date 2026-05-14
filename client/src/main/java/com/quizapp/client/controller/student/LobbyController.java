package com.quizapp.client.controller.student;

import com.quizapp.client.Main;
import com.quizapp.client.app.AppRoute;
import com.quizapp.shared.message.quiz.GetActiveQuizzesResponseMessage;
import com.quizapp.shared.message.quiz.JoinQuizResponseMessage;
import com.quizapp.shared.model.Quiz;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class LobbyController {
    @FXML
    private Label welcomeLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private ListView<Quiz> activeQuizList;

    @FXML
    private Button joinButton;

    @FXML
    private void initialize() {
        if (Main.getAppState().getLoggedInUser() != null) {
            welcomeLabel.setText("Welcome, " + Main.getAppState().getLoggedInUser().getFullName());
        }
        activeQuizList.getSelectionModel().selectedItemProperty()
                .addListener((ignored, oldValue, newValue) -> joinButton.setDisable(newValue == null));
        joinButton.setDisable(true);
        refreshActiveQuizzes();
    }

    @FXML
    private void handleRefresh() {
        refreshActiveQuizzes();
    }

    @FXML
    private void handleJoinQuiz() {
        Quiz selectedQuiz = activeQuizList.getSelectionModel().getSelectedItem();
        if (selectedQuiz == null || Main.getAppState().getLoggedInUser() == null) {
            statusLabel.setText("Select a quiz to join.");
            return;
        }

        try {
            JoinQuizResponseMessage response = Main.getAppState().getMessageDispatcher()
                    .joinQuiz(selectedQuiz.getId(), Main.getAppState().getLoggedInUser().getId());
            if (!response.isSuccess() || response.getQuiz() == null || response.getQuestions() == null) {
                statusLabel.setText(response.getReason() == null ? "Unable to join quiz." : response.getReason());
                return;
            }

            Main.getAppState().setCurrentQuiz(response.getQuiz());
            Main.getAppState().setCurrentQuizQuestions(response.getQuestions());
            Main.getAppNavigator().navigateTo(AppRoute.STUDENT_QUIZ);
        } catch (Exception e) {
            statusLabel.setText("Unable to join quiz: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        Main.getAppState().clearSession();
        Main.getAppNavigator().navigateTo(AppRoute.LOGIN);
    }

    private void refreshActiveQuizzes() {
        try {
            GetActiveQuizzesResponseMessage response = Main.getAppState().getMessageDispatcher().getActiveQuizzes();
            activeQuizList.setItems(FXCollections.observableArrayList(response.getQuizzes()));
            if (response.getQuizzes().isEmpty()) {
                statusLabel.setText("No active quizzes right now.");
            } else {
                statusLabel.setText("Select an active quiz to join.");
            }
        } catch (Exception e) {
            statusLabel.setText("Unable to load active quizzes: " + e.getMessage());
        }
    }
}

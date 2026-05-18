package com.quizapp.client.controller.teacher;

import com.quizapp.client.Main;
import com.quizapp.client.app.AppRoute;
import com.quizapp.client.app.QuizResultSummary;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ResultsController {
    @FXML
    private Label quizTitleLabel;

    @FXML
    private Label answeredLabel;

    @FXML
    private Label correctLabel;

    @FXML
    private Label percentageLabel;

    @FXML
    private Label passFailLabel;

    @FXML
    private Label reasonLabel;

    @FXML
    private void initialize() {
        QuizResultSummary summary = Main.getAppState().getLastQuizResult();
        if (summary == null) {
            quizTitleLabel.setText("No quiz result available");
            return;
        }

        quizTitleLabel.setText(summary.getQuizTitle());
        answeredLabel.setText("Answered: " + summary.getAnsweredQuestions() + " / " + summary.getTotalQuestions());
        correctLabel.setText("Correct: " + summary.getCorrectAnswers());
        percentageLabel.setText("Score: " + summary.getPercentage() + "%");
        passFailLabel.setText(summary.isPassed() ? "PASS" : "FAIL");
        reasonLabel.setText("Finished because: " + summary.getFinishReason());
    }

    @FXML
    private void handleBackToLobby() {
        Main.getAppState().setLastQuizResult(null);
        Main.getAppNavigator().navigateTo(AppRoute.STUDENT_LOBBY);
    }
}

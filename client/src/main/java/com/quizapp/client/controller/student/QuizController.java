package com.quizapp.client.controller.student;

import com.quizapp.client.Main;
import com.quizapp.client.app.AppRoute;
import com.quizapp.shared.message.quiz.CreateQuizMessage;
import com.quizapp.shared.message.student.AnswerSubmissionMessage;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class QuizController {
    @FXML
    private Label quizTitleLabel;

    @FXML
    private Label questionCounterLabel;

    @FXML
    private Label questionBodyLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private ListView<CreateQuizMessage.ChoicePayload> choiceList;

    @FXML
    private Button previousButton;

    @FXML
    private Button nextButton;

    @FXML
    private VBox quizContainer;

    private List<CreateQuizMessage.QuestionPayload> questions = List.of();
    private int currentQuestionIndex;

    @FXML
    private void initialize() {
        if (Main.getAppState().getCurrentQuiz() == null || Main.getAppState().getCurrentQuizQuestions() == null) {
            statusLabel.setText("No active quiz loaded.");
            quizContainer.setDisable(true);
            return;
        }

        questions = new ArrayList<>(Main.getAppState().getCurrentQuizQuestions());
        quizTitleLabel.setText(Main.getAppState().getCurrentQuiz().getTitle());
        choiceList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(CreateQuizMessage.ChoicePayload item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getBody());
            }
        });
        renderQuestion();
    }

    @FXML
    private void handleSubmitAnswer() {
        if (Main.getAppState().getLoggedInUser() == null || Main.getAppState().getCurrentQuiz() == null || questions.isEmpty()) {
            statusLabel.setText("Quiz session is not available.");
            return;
        }

        CreateQuizMessage.ChoicePayload selectedChoice = choiceList.getSelectionModel().getSelectedItem();
        if (selectedChoice == null) {
            statusLabel.setText("Select an answer first.");
            return;
        }

        CreateQuizMessage.QuestionPayload question = questions.get(currentQuestionIndex);
        boolean forceSubmitted = currentQuestionIndex == questions.size() - 1;

        try {
            AnswerSubmissionMessage request = new AnswerSubmissionMessage(
                    Main.getAppState().getLoggedInUser().getId(),
                    Main.getAppState().getCurrentQuiz().getId(),
                    question.getId(),
                    selectedChoice.getId(),
                    0,
                    forceSubmitted
            );
            Main.getAppState().getMessageDispatcher().submitAnswer(request);
            statusLabel.setText(forceSubmitted ? "Quiz submitted." : "Answer submitted.");

            if (forceSubmitted) {
                Main.getAppState().setCurrentQuiz(null);
                Main.getAppState().setCurrentQuizQuestions(null);
                Main.getAppNavigator().navigateTo(AppRoute.STUDENT_LOBBY);
                return;
            }

            currentQuestionIndex++;
            renderQuestion();
        } catch (Exception e) {
            statusLabel.setText("Unable to submit answer: " + e.getMessage());
        }
    }

    @FXML
    private void handlePreviousQuestion() {
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--;
            renderQuestion();
        }
    }

    @FXML
    private void handleBackToLobby() {
        Main.getAppState().setCurrentQuiz(null);
        Main.getAppState().setCurrentQuizQuestions(null);
        Main.getAppNavigator().navigateTo(AppRoute.STUDENT_LOBBY);
    }

    private void renderQuestion() {
        if (questions.isEmpty()) {
            statusLabel.setText("This quiz has no questions.");
            quizContainer.setDisable(true);
            return;
        }

        CreateQuizMessage.QuestionPayload question = questions.get(currentQuestionIndex);
        questionCounterLabel.setText("Question " + (currentQuestionIndex + 1) + " of " + questions.size());
        questionBodyLabel.setText(question.getBody());
        choiceList.setItems(FXCollections.observableArrayList(question.getChoices()));
        choiceList.getSelectionModel().clearSelection();
        previousButton.setDisable(currentQuestionIndex == 0);
        nextButton.setText(currentQuestionIndex == questions.size() - 1 ? "Submit & Finish" : "Submit & Next");
    }
}

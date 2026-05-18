package com.quizapp.client.controller.student;

import com.quizapp.client.Main;
import com.quizapp.client.app.AppRoute;
import com.quizapp.client.app.QuizResultSummary;
import com.quizapp.client.util.FullScreenGuard;
import com.quizapp.shared.message.quiz.CreateQuizMessage;
import com.quizapp.shared.message.student.AnswerSubmissionMessage;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private Label timerLabel;

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
    private Timeline timerTimeline;
    private int remainingSeconds;
    private final FullScreenGuard fullScreenGuard = new FullScreenGuard();

    @FXML
    private void initialize() {
        try {
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
            choiceList.getSelectionModel().selectedItemProperty().addListener((ignored, oldValue, newValue) -> {
                if (newValue != null && currentQuestionIndex < questions.size()) {
                    Main.getAppState().getSelectedChoiceByQuestionId().put(currentQuestion().getId(), newValue.getId());
                }
            });
            remainingSeconds = Main.getAppState().getCurrentQuiz().getTimeLimitSecs();
            updateTimerLabel();
            startTimer();
            Platform.runLater(() -> fullScreenGuard.activate(Main.getAppNavigator().getStage(),
                    () -> Platform.runLater(() -> finishQuiz("Fullscreen exited"))));
            renderQuestion();
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Quiz screen init failed: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            quizContainer.setDisable(true);
        }
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

        CreateQuizMessage.QuestionPayload question = currentQuestion();
        if (Main.getAppState().getSubmittedQuestionIds().contains(question.getId())) {
            statusLabel.setText("This question was already submitted.");
            moveForwardAfterSubmission();
            return;
        }
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
            Main.getAppState().getSubmittedQuestionIds().add(question.getId());
            statusLabel.setText(forceSubmitted ? "Quiz submitted." : "Answer submitted.");

            if (forceSubmitted) {
                finishQuiz("Completed");
                return;
            }

            moveForwardAfterSubmission();
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
        stopLocalGuards();
        Main.getAppState().clearCurrentQuizState();
        Main.getAppNavigator().navigateTo(AppRoute.STUDENT_LOBBY);
    }

    private void renderQuestion() {
        if (questions.isEmpty()) {
            statusLabel.setText("This quiz has no questions.");
            quizContainer.setDisable(true);
            return;
        }

        CreateQuizMessage.QuestionPayload question = currentQuestion();
        questionCounterLabel.setText("Question " + (currentQuestionIndex + 1) + " of " + questions.size());
        questionBodyLabel.setText(question.getBody());
        choiceList.setItems(FXCollections.observableArrayList(question.getChoices()));
        restoreSelectedChoice(question);
        previousButton.setDisable(currentQuestionIndex == 0);
        boolean submitted = Main.getAppState().getSubmittedQuestionIds().contains(question.getId());
        nextButton.setText(submitted
                ? (currentQuestionIndex == questions.size() - 1 ? "Finish Quiz" : "Next Question")
                : (currentQuestionIndex == questions.size() - 1 ? "Submit & Finish" : "Submit & Next"));
    }

    private void restoreSelectedChoice(CreateQuizMessage.QuestionPayload question) {
        Integer selectedId = Main.getAppState().getSelectedChoiceByQuestionId().get(question.getId());
        if (selectedId == null) {
            choiceList.getSelectionModel().clearSelection();
            return;
        }
        for (CreateQuizMessage.ChoicePayload choice : question.getChoices()) {
            if (choice.getId() == selectedId) {
                choiceList.getSelectionModel().select(choice);
                return;
            }
        }
        choiceList.getSelectionModel().clearSelection();
    }

    private CreateQuizMessage.QuestionPayload currentQuestion() {
        return questions.get(currentQuestionIndex);
    }

    private void moveForwardAfterSubmission() {
        if (currentQuestionIndex < questions.size() - 1) {
            currentQuestionIndex++;
            renderQuestion();
        } else {
            finishQuiz("Completed");
        }
    }

    private void startTimer() {
        timerTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            remainingSeconds--;
            updateTimerLabel();
            if (remainingSeconds <= 0) {
                finishQuiz("Time ended");
            }
        }));
        timerTimeline.setCycleCount(Timeline.INDEFINITE);
        timerTimeline.play();
    }

    private void updateTimerLabel() {
        int minutes = Math.max(remainingSeconds, 0) / 60;
        int seconds = Math.max(remainingSeconds, 0) % 60;
        timerLabel.setText(String.format("Time Left: %02d:%02d", minutes, seconds));
    }

    private void finishQuiz(String reason) {
        stopLocalGuards();
        QuizResultSummary summary = buildSummary(reason);
        Main.getAppState().setLastQuizResult(summary);
        Main.getAppState().clearCurrentQuizState();
        Main.getAppNavigator().navigateTo(AppRoute.STUDENT_RESULTS);
    }

    private QuizResultSummary buildSummary(String reason) {
        Map<Integer, Integer> selectedAnswers = Main.getAppState().getSelectedChoiceByQuestionId();
        Set<Integer> answeredQuestions = new HashSet<>(selectedAnswers.keySet());
        int correctAnswers = 0;
        for (CreateQuizMessage.QuestionPayload question : questions) {
            Integer selectedChoiceId = selectedAnswers.get(question.getId());
            if (selectedChoiceId == null) {
                continue;
            }
            boolean correct = question.getChoices().stream()
                    .anyMatch(choice -> choice.getId() == selectedChoiceId && choice.isCorrect());
            if (correct) {
                correctAnswers++;
            }
        }

        int totalQuestions = questions.size();
        int percentage = totalQuestions == 0 ? 0 : (correctAnswers * 100) / totalQuestions;
        boolean passed = Main.getAppState().getCurrentQuiz() != null
                && percentage >= Main.getAppState().getCurrentQuiz().getPassingScore();
        String quizTitle = Main.getAppState().getCurrentQuiz() == null ? "Quiz" : Main.getAppState().getCurrentQuiz().getTitle();
        return new QuizResultSummary(quizTitle, totalQuestions, answeredQuestions.size(), correctAnswers, percentage, passed, reason);
    }

    private void stopLocalGuards() {
        if (timerTimeline != null) {
            timerTimeline.stop();
            timerTimeline = null;
        }
        fullScreenGuard.deactivate();
    }
}

package com.quizapp.client.controller.teacher;

import com.quizapp.client.Main;
import com.quizapp.client.app.AppRoute;
import com.quizapp.shared.message.academic.ActionResponseMessage;
import com.quizapp.shared.model.AcademicYear;
import com.quizapp.shared.model.Quiz;
import com.quizapp.shared.model.Semester;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionController {
    @FXML
    private ListView<Quiz> quizListView;

    @FXML
    private Label selectedQuizTitleLabel;

    @FXML
    private Label selectedQuizSemesterLabel;

    @FXML
    private Label selectedQuizStatusLabel;

    @FXML
    private Label selectedQuizTimeLimitLabel;

    @FXML
    private Label selectedQuizPassingScoreLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Button startQuizButton;

    @FXML
    private Button closeQuizButton;

    private final Map<Integer, Semester> semestersById = new HashMap<>();
    private final Map<Integer, AcademicYear> academicYearsById = new HashMap<>();

    @FXML
    private void initialize() {
        configureQuizList();
        loadAcademicYears();
        loadSemesters();
        loadTeacherQuizzes();
        clearSelectionDetails();
        quizListView.getSelectionModel().selectedItemProperty()
                .addListener((ignored, oldValue, newValue) -> updateSelectedQuiz(newValue));
    }

    @FXML
    private void handleRefresh() {
        loadTeacherQuizzes();
    }

    @FXML
    private void handleStartQuiz() {
        updateQuizStatus(true);
    }

    @FXML
    private void handleCloseQuiz() {
        updateQuizStatus(false);
    }

    @FXML
    private void handleBack() {
        Main.getAppNavigator().navigateTo(AppRoute.TEACHER_DASHBOARD);
    }

    private void updateQuizStatus(boolean start) {
        Quiz selectedQuiz = quizListView.getSelectionModel().getSelectedItem();
        if (selectedQuiz == null) {
            statusLabel.setText("Select a quiz first.");
            return;
        }

        try {
            ActionResponseMessage response = start
                    ? Main.getAppState().getMessageDispatcher().startQuiz(selectedQuiz.getId())
                    : Main.getAppState().getMessageDispatcher().closeQuiz(selectedQuiz.getId());
            statusLabel.setText(response.isSuccess()
                    ? (start ? "Quiz started." : "Quiz closed.")
                    : response.getReason());
            if (response.isSuccess()) {
                loadTeacherQuizzes();
                reselectQuiz(selectedQuiz.getId());
            }
        } catch (Exception e) {
            statusLabel.setText("Unable to update quiz status: " + e.getMessage());
        }
    }

    private void loadTeacherQuizzes() {
        if (Main.getAppState().getLoggedInUser() == null) {
            statusLabel.setText("You must be logged in.");
            quizListView.setItems(FXCollections.observableArrayList());
            return;
        }

        try {
            List<Quiz> quizzes = Main.getAppState().getMessageDispatcher()
                    .getTeacherQuizzes(Main.getAppState().getLoggedInUser().getId())
                    .getQuizzes();
            quizListView.setItems(FXCollections.observableArrayList(quizzes));
            statusLabel.setText(quizzes.isEmpty() ? "No quizzes created yet." : "Teacher quizzes loaded.");
        } catch (Exception e) {
            statusLabel.setText("Unable to load teacher quizzes: " + e.getMessage());
        }
    }

    private void loadSemesters() {
        try {
            semestersById.clear();
            Main.getAppState().getMessageDispatcher().getSemesters().getSemesters()
                    .forEach(semester -> semestersById.put(semester.getId(), semester));
        } catch (Exception e) {
            statusLabel.setText("Unable to load semesters: " + e.getMessage());
        }
    }

    private void loadAcademicYears() {
        try {
            academicYearsById.clear();
            Main.getAppState().getMessageDispatcher().getAcademicYears().getAcademicYears()
                    .forEach(year -> academicYearsById.put(year.getId(), year));
        } catch (Exception e) {
            statusLabel.setText("Unable to load academic years: " + e.getMessage());
        }
    }

    private void configureQuizList() {
        quizListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Quiz item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitle() + " - " + describeSemester(item.getSemesterId()) + " - " + item.getStatus());
            }
        });
    }

    private void updateSelectedQuiz(Quiz quiz) {
        if (quiz == null) {
            clearSelectionDetails();
            return;
        }

        selectedQuizTitleLabel.setText(quiz.getTitle());
        selectedQuizSemesterLabel.setText(describeSemester(quiz.getSemesterId()));
        selectedQuizStatusLabel.setText(quiz.getStatus());
        selectedQuizTimeLimitLabel.setText(quiz.getTimeLimitSecs() + " seconds");
        selectedQuizPassingScoreLabel.setText(quiz.getPassingScore() + "%");
        startQuizButton.setDisable(!"DRAFT".equalsIgnoreCase(quiz.getStatus()));
        closeQuizButton.setDisable(!"ACTIVE".equalsIgnoreCase(quiz.getStatus()));
    }

    private void clearSelectionDetails() {
        selectedQuizTitleLabel.setText("No quiz selected");
        selectedQuizSemesterLabel.setText("-");
        selectedQuizStatusLabel.setText("-");
        selectedQuizTimeLimitLabel.setText("-");
        selectedQuizPassingScoreLabel.setText("-");
        startQuizButton.setDisable(true);
        closeQuizButton.setDisable(true);
    }

    private String describeSemester(int semesterId) {
        Semester semester = semestersById.get(semesterId);
        if (semester == null) {
            return "Semester #" + semesterId;
        }
        AcademicYear academicYear = academicYearsById.get(semester.getAcademicYearId());
        String yearLabel = academicYear == null ? "Academic Year #" + semester.getAcademicYearId() : academicYear.getLabel();
        return semester.getName() + " (" + yearLabel + ")";
    }

    private void reselectQuiz(int quizId) {
        quizListView.getItems().stream()
                .filter(quiz -> quiz.getId() == quizId)
                .findFirst()
                .ifPresent(quiz -> quizListView.getSelectionModel().select(quiz));
    }
}

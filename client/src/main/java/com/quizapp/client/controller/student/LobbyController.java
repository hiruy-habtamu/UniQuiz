package com.quizapp.client.controller.student;

import com.quizapp.client.Main;
import com.quizapp.client.app.AppRoute;
import com.quizapp.shared.message.quiz.JoinQuizResponseMessage;
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
import java.util.Map;

public class LobbyController {
    @FXML
    private Label welcomeLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private ListView<Quiz> activeQuizList;

    @FXML
    private Button joinButton;

    private final Map<Integer, Semester> semestersById = new HashMap<>();
    private final Map<Integer, AcademicYear> academicYearsById = new HashMap<>();

    @FXML
    private void initialize() {
        if (Main.getAppState().getLoggedInUser() != null) {
            welcomeLabel.setText("Welcome, " + Main.getAppState().getLoggedInUser().getFullName());
        }
        configureQuizList();
        loadAcademicYears();
        loadSemesters();
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
            int studentId = Main.getAppState().getLoggedInUser() == null ? 0 : Main.getAppState().getLoggedInUser().getId();
            var response = Main.getAppState().getMessageDispatcher().getStudentActiveQuizzes(studentId);
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

    private void configureQuizList() {
        activeQuizList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Quiz item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitle() + " - " + describeSemester(item.getSemesterId()) + " - " + item.getStatus());
            }
        });
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

    private String describeSemester(int semesterId) {
        Semester semester = semestersById.get(semesterId);
        if (semester == null) {
            return "Semester #" + semesterId;
        }
        AcademicYear academicYear = academicYearsById.get(semester.getAcademicYearId());
        String yearLabel = academicYear == null ? "Academic Year #" + semester.getAcademicYearId() : academicYear.getLabel();
        return semester.getName() + " (" + yearLabel + ")";
    }
}

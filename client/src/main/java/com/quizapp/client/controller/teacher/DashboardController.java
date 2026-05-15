package com.quizapp.client.controller.teacher;

import com.quizapp.client.Main;
import com.quizapp.client.app.AppRoute;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DashboardController {
    @FXML
    private Label welcomeLabel;

    @FXML
    private void initialize() {
        if (Main.getAppState().getLoggedInUser() != null) {
            welcomeLabel.setText("Welcome, " + Main.getAppState().getLoggedInUser().getFullName());
        }
    }

    @FXML
    private void handleLogout() {
        Main.getAppState().clearSession();
        Main.getAppNavigator().navigateTo(AppRoute.LOGIN);
    }

    @FXML
    private void handleOpenBatch() {
        Main.getAppNavigator().navigateTo(AppRoute.TEACHER_BATCH);
    }

    @FXML
    private void handleOpenAcademicYear() {
        Main.getAppNavigator().navigateTo(AppRoute.TEACHER_ACADEMIC_YEAR);
    }

    @FXML
    private void handleOpenSemester() {
        Main.getAppNavigator().navigateTo(AppRoute.TEACHER_SEMESTER);
    }

    @FXML
    private void handleOpenSection() {
        Main.getAppNavigator().navigateTo(AppRoute.TEACHER_SECTION);
    }
}

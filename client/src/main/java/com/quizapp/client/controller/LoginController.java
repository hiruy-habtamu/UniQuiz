package com.quizapp.client.controller;

import com.quizapp.client.Main;
import com.quizapp.client.app.AppRoute;
import com.quizapp.client.app.AppState;
import com.quizapp.shared.message.auth.LoginResponseMessage;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    @FXML
    private void handleLogin() {
        AppState appState = Main.getAppState();
        try {
            LoginResponseMessage response = appState.getMessageDispatcher()
                    .login(usernameField.getText(), passwordField.getText());
            if (!response.isSuccess() || response.getUser() == null) {
                statusLabel.setText(response.getReason() == null ? "Login failed." : response.getReason());
                return;
            }

            appState.setLoggedInUser(response.getUser());
            AppRoute route = "TEACHER".equalsIgnoreCase(response.getUser().getRole())
                    ? AppRoute.TEACHER_DASHBOARD
                    : AppRoute.STUDENT_LOBBY;
            Main.getAppNavigator().navigateTo(route);
        } catch (Exception e) {
            statusLabel.setText("Unable to login: " + e.getMessage());
        }
    }

    @FXML
    private void handleGoToRegister() {
        Main.getAppNavigator().navigateTo(AppRoute.REGISTER);
    }
}

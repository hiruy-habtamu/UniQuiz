package com.quizapp.client.controller;

import com.quizapp.client.Main;
import com.quizapp.client.app.AppRoute;
import com.quizapp.client.app.AppState;
import com.quizapp.shared.message.academic.ActionResponseMessage;
import com.quizapp.shared.message.academic.GetBatchesResponseMessage;
import com.quizapp.shared.message.academic.GetSectionsResponseMessage;
import com.quizapp.shared.message.auth.LoginResponseMessage;
import com.quizapp.shared.message.auth.RegisterResponseMessage;
import com.quizapp.shared.model.AcademicYear;
import com.quizapp.shared.model.Batch;
import com.quizapp.shared.model.Section;
import com.quizapp.shared.model.Semester;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField fullNameField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private ComboBox<Batch> batchComboBox;

    @FXML
    private ComboBox<Section> sectionComboBox;

    @FXML
    private Label statusLabel;

    private final Map<Integer, Semester> semestersById = new HashMap<>();
    private final Map<Integer, AcademicYear> academicYearsById = new HashMap<>();

    @FXML
    private void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList("TEACHER", "STUDENT"));
        roleComboBox.setValue("STUDENT");
        roleComboBox.valueProperty().addListener((ignored, oldValue, newValue) -> updateRoleFields(newValue));
        batchComboBox.valueProperty().addListener((ignored, oldValue, newValue) -> loadSectionsForBatch(newValue));
        configureSectionComboBox();
        loadAcademicYears();
        loadSemesters();
        updateRoleFields(roleComboBox.getValue());
        loadBatches();
    }

    @FXML
    private void handleRegister() {
        AppState appState = Main.getAppState();
        String role = roleComboBox.getValue();
        Batch batch = batchComboBox.getValue();
        Section section = sectionComboBox.getValue();
        Integer batchId = "STUDENT".equalsIgnoreCase(role) && batch != null ? batch.getId() : null;

        if ("STUDENT".equalsIgnoreCase(role) && (batch == null || section == null)) {
            statusLabel.setText("Students must select a batch and section.");
            return;
        }

        try {
            RegisterResponseMessage registerResponse = appState.getMessageDispatcher().register(
                    usernameField.getText(), passwordField.getText(), fullNameField.getText(), role, batchId);
            if (!registerResponse.isSuccess()) {
                statusLabel.setText(registerResponse.getReason() == null ? "Registration failed." : registerResponse.getReason());
                return;
            }

            LoginResponseMessage loginResponse = appState.getMessageDispatcher()
                    .login(usernameField.getText(), passwordField.getText());
            if (!loginResponse.isSuccess() || loginResponse.getUser() == null) {
                statusLabel.setText("Registration succeeded, but auto-login failed.");
                return;
            }

            appState.setLoggedInUser(loginResponse.getUser());
            if ("STUDENT".equalsIgnoreCase(role) && section != null) {
                ActionResponseMessage assignment = appState.getMessageDispatcher()
                        .assignStudentSection(loginResponse.getUser().getId(), section.getId());
                if (!assignment.isSuccess()) {
                    statusLabel.setText(assignment.getReason() == null ? "Section assignment failed." : assignment.getReason());
                    return;
                }
            }

            AppRoute route = "TEACHER".equalsIgnoreCase(role) ? AppRoute.TEACHER_DASHBOARD : AppRoute.STUDENT_LOBBY;
            Main.getAppNavigator().navigateTo(route);
        } catch (Exception e) {
            statusLabel.setText("Unable to register: " + e.getMessage());
        }
    }

    @FXML
    private void handleGoToLogin() {
        Main.getAppNavigator().navigateTo(AppRoute.LOGIN);
    }

    private void loadBatches() {
        try {
            GetBatchesResponseMessage response = Main.getAppState().getMessageDispatcher().getBatches();
            List<Batch> batches = response.getBatches();
            batchComboBox.setItems(FXCollections.observableArrayList(batches));
        } catch (Exception e) {
            statusLabel.setText("Unable to load batches: " + e.getMessage());
        }
    }

    private void loadSectionsForBatch(Batch batch) {
        sectionComboBox.getItems().clear();
        if (batch == null || !"STUDENT".equalsIgnoreCase(roleComboBox.getValue())) {
            return;
        }

        try {
            GetSectionsResponseMessage response = Main.getAppState().getMessageDispatcher().getSections(batch.getId());
            sectionComboBox.setItems(FXCollections.observableArrayList(response.getSections()));
        } catch (Exception e) {
            statusLabel.setText("Unable to load sections: " + e.getMessage());
        }
    }

    private void updateRoleFields(String role) {
        boolean student = "STUDENT".equalsIgnoreCase(role);
        batchComboBox.setDisable(!student);
        sectionComboBox.setDisable(!student);
        if (!student) {
            batchComboBox.getSelectionModel().clearSelection();
            sectionComboBox.getItems().clear();
        }
    }

    private void configureSectionComboBox() {
        sectionComboBox.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Section item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : describeSection(item));
            }
        });
        sectionComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Section item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : describeSection(item));
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

    private String describeSection(Section section) {
        Semester semester = semestersById.get(section.getSemesterId());
        if (semester == null) {
            return section.getName();
        }
        AcademicYear academicYear = academicYearsById.get(semester.getAcademicYearId());
        String yearLabel = academicYear == null ? "Academic Year #" + semester.getAcademicYearId() : academicYear.getLabel();
        return section.getName() + " - " + semester.getName() + " (" + yearLabel + ")" + (semester.isActive() ? " - ACTIVE" : " - INACTIVE");
    }
}

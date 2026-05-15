package com.quizapp.client.controller.teacher;

import com.quizapp.client.Main;
import com.quizapp.client.app.AppRoute;
import com.quizapp.shared.message.academic.EntityResponseMessage;
import com.quizapp.shared.model.AcademicYear;
import com.quizapp.shared.model.Semester;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class SemesterController {
    @FXML
    private ComboBox<AcademicYear> academicYearComboBox;

    @FXML
    private TextField nameField;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private CheckBox activeCheckBox;

    @FXML
    private Label statusLabel;

    @FXML
    private void initialize() {
        loadAcademicYears();
    }

    @FXML
    private void handleCreateSemester() {
        if (Main.getAppState().getLoggedInUser() == null) {
            statusLabel.setText("You must be logged in.");
            return;
        }

        AcademicYear selectedYear = academicYearComboBox.getValue();
        if (selectedYear == null) {
            statusLabel.setText("Select an academic year.");
            return;
        }

        try {
            Semester semester = new Semester();
            semester.setAcademicYearId(selectedYear.getId());
            semester.setName(nameField.getText());
            semester.setStartDate(startDatePicker.getValue());
            semester.setEndDate(endDatePicker.getValue());
            semester.setActive(activeCheckBox.isSelected());
            semester.setCreatedBy(Main.getAppState().getLoggedInUser().getId());
            EntityResponseMessage response = Main.getAppState().getMessageDispatcher().createSemester(semester);
            statusLabel.setText(response.isSuccess()
                    ? "Semester created with id " + response.getEntityId()
                    : response.getReason());
        } catch (Exception e) {
            statusLabel.setText("Unable to create semester: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        Main.getAppNavigator().navigateTo(AppRoute.TEACHER_DASHBOARD);
    }

    private void loadAcademicYears() {
        try {
            academicYearComboBox.setItems(FXCollections.observableArrayList(
                    Main.getAppState().getMessageDispatcher().getAcademicYears().getAcademicYears()));
        } catch (Exception e) {
            statusLabel.setText("Unable to load academic years: " + e.getMessage());
        }
    }
}

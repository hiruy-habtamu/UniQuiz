package com.quizapp.client.controller.teacher;

import com.quizapp.client.Main;
import com.quizapp.client.app.AppRoute;
import com.quizapp.shared.message.academic.EntityResponseMessage;
import com.quizapp.shared.model.AcademicYear;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.util.Comparator;
import java.util.List;

public class AcademicYearController {
    @FXML
    private TextField startYearField;

    @FXML
    private CheckBox activeCheckBox;

    @FXML
    private ListView<AcademicYear> academicYearList;

    @FXML
    private Label statusLabel;

    @FXML
    private void initialize() {
        loadAcademicYears();
    }

    @FXML
    private void handleCreateAcademicYear() {
        try {
            int startYear = Integer.parseInt(startYearField.getText().trim());
            EntityResponseMessage response = Main.getAppState().getMessageDispatcher()
                    .createAcademicYear(startYear, activeCheckBox.isSelected());
            statusLabel.setText(response.isSuccess()
                    ? "Academic year created with id " + response.getEntityId()
                    : response.getReason());
            if (response.isSuccess()) {
                startYearField.clear();
                activeCheckBox.setSelected(false);
                loadAcademicYears();
            }
        } catch (Exception e) {
            statusLabel.setText("Unable to create academic year: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        Main.getAppNavigator().navigateTo(AppRoute.TEACHER_DASHBOARD);
    }

    private void loadAcademicYears() {
        try {
            List<AcademicYear> years = Main.getAppState().getMessageDispatcher().getAcademicYears().getAcademicYears();
            years = years.stream()
                    .sorted(Comparator.comparing(AcademicYear::getStartDate).reversed())
                    .toList();
            academicYearList.setItems(FXCollections.observableArrayList(years));
            statusLabel.setText(years.isEmpty() ? "No academic years created yet." : "Academic years loaded.");
        } catch (Exception e) {
            statusLabel.setText("Unable to load academic years: " + e.getMessage());
        }
    }
}

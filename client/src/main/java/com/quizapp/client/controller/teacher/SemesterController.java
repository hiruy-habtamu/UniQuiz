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
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.util.HashMap;
import java.util.Map;

public class SemesterController {
    @FXML
    private ComboBox<AcademicYear> academicYearComboBox;

    @FXML
    private ComboBox<String> semesterNameComboBox;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private CheckBox activeCheckBox;

    @FXML
    private Label statusLabel;

    @FXML
    private ListView<Semester> semesterListView;

    private final Map<Integer, AcademicYear> academicYearsById = new HashMap<>();

    @FXML
    private void initialize() {
        semesterNameComboBox.setItems(FXCollections.observableArrayList("FIRST", "SECOND", "SUMMER"));
        loadAcademicYears();
        configureSemesterList();
        loadSemesters();
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
            semester.setName(semesterNameComboBox.getValue());
            semester.setStartDate(startDatePicker.getValue());
            semester.setEndDate(endDatePicker.getValue());
            semester.setActive(activeCheckBox.isSelected());
            semester.setCreatedBy(Main.getAppState().getLoggedInUser().getId());
            EntityResponseMessage response = Main.getAppState().getMessageDispatcher().createSemester(semester);
            statusLabel.setText(response.isSuccess()
                    ? "Semester created with id " + response.getEntityId()
                    : response.getReason());
            if (response.isSuccess()) {
                semesterNameComboBox.getSelectionModel().clearSelection();
                startDatePicker.setValue(null);
                endDatePicker.setValue(null);
                activeCheckBox.setSelected(false);
                loadSemesters();
            }
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
            var academicYears = FXCollections.observableArrayList(
                    Main.getAppState().getMessageDispatcher().getAcademicYears().getAcademicYears());
            academicYearsById.clear();
            academicYears.forEach(year -> academicYearsById.put(year.getId(), year));
            academicYearComboBox.setItems(academicYears);
        } catch (Exception e) {
            statusLabel.setText("Unable to load academic years: " + e.getMessage());
        }
    }

    private void loadSemesters() {
        try {
            semesterListView.setItems(FXCollections.observableArrayList(
                    Main.getAppState().getMessageDispatcher().getSemesters().getSemesters()));
        } catch (Exception e) {
            statusLabel.setText("Unable to load semesters: " + e.getMessage());
        }
    }

    private void configureSemesterList() {
        semesterListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Semester item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : describeSemester(item));
            }
        });
    }

    private String describeSemester(Semester semester) {
        AcademicYear academicYear = academicYearsById.get(semester.getAcademicYearId());
        String yearLabel = academicYear == null ? "Academic Year #" + semester.getAcademicYearId() : academicYear.getLabel();
        return semester.getName() + " (" + yearLabel + ")";
    }
}

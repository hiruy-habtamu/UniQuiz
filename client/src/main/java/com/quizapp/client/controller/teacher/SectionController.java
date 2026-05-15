package com.quizapp.client.controller.teacher;

import com.quizapp.client.Main;
import com.quizapp.client.app.AppRoute;
import com.quizapp.shared.message.academic.EntityResponseMessage;
import com.quizapp.shared.model.AcademicYear;
import com.quizapp.shared.model.Batch;
import com.quizapp.shared.model.Section;
import com.quizapp.shared.model.Semester;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.util.HashMap;
import java.util.Map;

public class SectionController {
    @FXML
    private TextField sectionNameField;

    @FXML
    private ComboBox<Batch> batchComboBox;

    @FXML
    private ComboBox<Semester> semesterComboBox;

    @FXML
    private Label statusLabel;

    @FXML
    private ListView<Batch> batchListView;

    @FXML
    private ListView<Section> sectionListView;

    private final Map<Integer, Batch> batchesById = new HashMap<>();
    private final Map<Integer, Semester> semestersById = new HashMap<>();
    private final Map<Integer, AcademicYear> academicYearsById = new HashMap<>();

    @FXML
    private void initialize() {
        loadAcademicYears();
        loadBatches();
        loadSemesters();
        configureLists();
        loadSections();
    }

    @FXML
    private void handleCreateSection() {
        if (Main.getAppState().getLoggedInUser() == null) {
            statusLabel.setText("You must be logged in.");
            return;
        }
        Batch batch = batchComboBox.getValue();
        Semester semester = semesterComboBox.getValue();
        if (batch == null) {
            statusLabel.setText("Select a batch.");
            return;
        }
        if (semester == null) {
            statusLabel.setText("Select a semester.");
            return;
        }

        try {
            Section section = new Section();
            section.setName(sectionNameField.getText());
            section.setBatchId(batch.getId());
            section.setSemesterId(semester.getId());
            EntityResponseMessage response = Main.getAppState().getMessageDispatcher().createSection(section);
            statusLabel.setText(response.isSuccess()
                    ? "Section created with id " + response.getEntityId()
                    : response.getReason());
            if (response.isSuccess()) {
                sectionNameField.clear();
                loadSections();
            }
        } catch (Exception e) {
            statusLabel.setText("Unable to create section: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        Main.getAppNavigator().navigateTo(AppRoute.TEACHER_DASHBOARD);
    }

    private void loadBatches() {
        try {
            var batches = FXCollections.observableArrayList(
                    Main.getAppState().getMessageDispatcher().getBatches().getBatches());
            batchesById.clear();
            batches.forEach(batch -> batchesById.put(batch.getId(), batch));
            batchComboBox.setItems(batches);
            batchListView.setItems(batches);
        } catch (Exception e) {
            statusLabel.setText("Unable to load batches: " + e.getMessage());
        }
    }

    private void loadSemesters() {
        try {
            var semesters = FXCollections.observableArrayList(
                    Main.getAppState().getMessageDispatcher().getSemesters().getSemesters());
            semestersById.clear();
            semesters.forEach(semester -> semestersById.put(semester.getId(), semester));
            semesterComboBox.setItems(semesters);
        } catch (Exception e) {
            statusLabel.setText("Unable to load semesters: " + e.getMessage());
        }
    }

    private void loadSections() {
        try {
            sectionListView.setItems(FXCollections.observableArrayList(
                    Main.getAppState().getMessageDispatcher().getAllSections().getSections()));
        } catch (Exception e) {
            statusLabel.setText("Unable to load sections: " + e.getMessage());
        }
    }

    private void configureLists() {
        semesterComboBox.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Semester item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : describeSemester(item));
            }
        });
        semesterComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Semester item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : describeSemester(item));
            }
        });
        sectionListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Section item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : describeSection(item));
            }
        });
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

    private String describeSemester(Semester semester) {
        AcademicYear academicYear = academicYearsById.get(semester.getAcademicYearId());
        String yearLabel = academicYear == null ? "Academic Year #" + semester.getAcademicYearId() : academicYear.getLabel();
        return semester.getName() + " (" + yearLabel + ")";
    }

    private String describeSection(Section section) {
        Batch batch = batchesById.get(section.getBatchId());
        Semester semester = semestersById.get(section.getSemesterId());
        String batchLabel = batch == null ? "Batch #" + section.getBatchId() : batch.getProgram() + " Batch " + batch.getEntryYear();
        String semesterLabel = semester == null ? "Semester #" + section.getSemesterId() : semester.getName();
        return section.getName() + " - " + batchLabel + " - " + semesterLabel;
    }
}

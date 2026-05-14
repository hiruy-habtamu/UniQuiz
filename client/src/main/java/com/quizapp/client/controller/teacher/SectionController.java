package com.quizapp.client.controller.teacher;

import com.quizapp.client.Main;
import com.quizapp.client.app.AppRoute;
import com.quizapp.shared.message.academic.EntityResponseMessage;
import com.quizapp.shared.model.Batch;
import com.quizapp.shared.model.Section;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class SectionController {
    @FXML
    private TextField sectionNameField;

    @FXML
    private ComboBox<Batch> batchComboBox;

    @FXML
    private TextField semesterIdField;

    @FXML
    private Label statusLabel;

    @FXML
    private void initialize() {
        loadBatches();
    }

    @FXML
    private void handleCreateSection() {
        if (Main.getAppState().getLoggedInUser() == null) {
            statusLabel.setText("You must be logged in.");
            return;
        }
        Batch batch = batchComboBox.getValue();
        if (batch == null) {
            statusLabel.setText("Select a batch.");
            return;
        }

        try {
            Section section = new Section();
            section.setName(sectionNameField.getText());
            section.setBatchId(batch.getId());
            section.setSemesterId(Integer.parseInt(semesterIdField.getText()));
            EntityResponseMessage response = Main.getAppState().getMessageDispatcher().createSection(section);
            statusLabel.setText(response.isSuccess()
                    ? "Section created with id " + response.getEntityId()
                    : response.getReason());
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
            batchComboBox.setItems(FXCollections.observableArrayList(
                    Main.getAppState().getMessageDispatcher().getBatches().getBatches()));
        } catch (Exception e) {
            statusLabel.setText("Unable to load batches: " + e.getMessage());
        }
    }
}

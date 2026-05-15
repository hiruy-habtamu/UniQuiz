package com.quizapp.client.controller.teacher;

import com.quizapp.client.Main;
import com.quizapp.client.app.AppRoute;
import com.quizapp.shared.message.academic.EntityResponseMessage;
import com.quizapp.shared.model.Batch;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class BatchController {
    @FXML
    private TextField programField;

    @FXML
    private TextField entryYearField;

    @FXML
    private Label statusLabel;

    @FXML
    private ListView<Batch> batchListView;

    @FXML
    private void initialize() {
        loadBatches();
    }

    @FXML
    private void handleCreateBatch() {
        if (Main.getAppState().getLoggedInUser() == null) {
            statusLabel.setText("You must be logged in.");
            return;
        }

        try {
            Batch batch = new Batch();
            batch.setProgram(programField.getText());
            batch.setEntryYear(Integer.parseInt(entryYearField.getText()));
            batch.setCreatedBy(Main.getAppState().getLoggedInUser().getId());
            EntityResponseMessage response = Main.getAppState().getMessageDispatcher().createBatch(batch);
            statusLabel.setText(response.isSuccess()
                    ? "Batch created with id " + response.getEntityId()
                    : response.getReason());
            if (response.isSuccess()) {
                programField.clear();
                entryYearField.clear();
                loadBatches();
            }
        } catch (Exception e) {
            statusLabel.setText("Unable to create batch: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        Main.getAppNavigator().navigateTo(AppRoute.TEACHER_DASHBOARD);
    }

    private void loadBatches() {
        try {
            batchListView.setItems(FXCollections.observableArrayList(
                    Main.getAppState().getMessageDispatcher().getBatches().getBatches()));
        } catch (Exception e) {
            statusLabel.setText("Unable to load batches: " + e.getMessage());
        }
    }
}

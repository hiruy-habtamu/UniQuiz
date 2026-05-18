package com.quizapp.client.controller.teacher;

import com.quizapp.client.Main;
import com.quizapp.client.app.AppRoute;
import com.quizapp.shared.message.quiz.CreateQuizMessage;
import com.quizapp.shared.message.quiz.CreateQuizResponseMessage;
import com.quizapp.shared.model.AcademicYear;
import com.quizapp.shared.model.Quiz;
import com.quizapp.shared.model.Semester;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizBuilderController {
    @FXML
    private ComboBox<AcademicYear> academicYearComboBox;

    @FXML
    private ComboBox<Semester> semesterComboBox;

    @FXML
    private TextField titleField;

    @FXML
    private Spinner<Integer> timeLimitSpinner;

    @FXML
    private Spinner<Integer> passingScoreSpinner;

    @FXML
    private TextArea questionBodyArea;

    @FXML
    private TextField choiceAField;

    @FXML
    private TextField choiceBField;

    @FXML
    private TextField choiceCField;

    @FXML
    private TextField choiceDField;

    @FXML
    private ComboBox<String> correctChoiceComboBox;

    @FXML
    private ListView<CreateQuizMessage.QuestionPayload> draftedQuestionsList;

    @FXML
    private Label statusLabel;

    private final List<Semester> allSemesters = new ArrayList<>();
    private final List<CreateQuizMessage.QuestionPayload> draftedQuestions = new ArrayList<>();
    private final Map<Integer, AcademicYear> academicYearsById = new HashMap<>();

    @FXML
    private void initialize() {
        timeLimitSpinner.setValueFactory(new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(30, 7200, 600, 30));
        passingScoreSpinner.setValueFactory(new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 60, 5));
        correctChoiceComboBox.setItems(FXCollections.observableArrayList("A", "B", "C", "D"));
        academicYearComboBox.valueProperty().addListener((ignored, oldValue, newValue) -> filterSemesters(newValue));
        configureLists();
        loadAcademicYears();
        loadSemesters();
    }

    @FXML
    private void handleAddQuestion() {
        try {
            CreateQuizMessage.QuestionPayload payload = buildDraftQuestion();
            draftedQuestions.add(payload);
            draftedQuestionsList.setItems(FXCollections.observableArrayList(draftedQuestions));
            clearQuestionEditor();
            statusLabel.setText("Question added to draft.");
        } catch (IllegalArgumentException e) {
            statusLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void handleRemoveSelectedQuestion() {
        CreateQuizMessage.QuestionPayload selected = draftedQuestionsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a drafted question to remove.");
            return;
        }
        draftedQuestions.remove(selected);
        draftedQuestionsList.setItems(FXCollections.observableArrayList(draftedQuestions));
        statusLabel.setText("Draft question removed.");
    }

    @FXML
    private void handleCreateQuiz() {
        if (Main.getAppState().getLoggedInUser() == null) {
            statusLabel.setText("You must be logged in.");
            return;
        }
        Semester semester = semesterComboBox.getValue();
        if (semester == null) {
            statusLabel.setText("Select a semester.");
            return;
        }
        if (draftedQuestions.isEmpty()) {
            statusLabel.setText("Add at least one question before creating the quiz.");
            return;
        }

        try {
            Quiz quiz = new Quiz();
            quiz.setTitle(titleField.getText());
            quiz.setSemesterId(semester.getId());
            quiz.setCreatedBy(Main.getAppState().getLoggedInUser().getId());
            quiz.setTimeLimitSecs(timeLimitSpinner.getValue());
            quiz.setPassingScore(passingScoreSpinner.getValue());

            CreateQuizResponseMessage response = Main.getAppState().getMessageDispatcher()
                    .createQuiz(new CreateQuizMessage(quiz, List.copyOf(draftedQuestions)));
            statusLabel.setText(response.isSuccess()
                    ? "Quiz created with id " + response.getQuizId()
                    : response.getReason());
            if (response.isSuccess()) {
                clearQuizForm();
            }
        } catch (Exception e) {
            statusLabel.setText("Unable to create quiz: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        Main.getAppNavigator().navigateTo(AppRoute.TEACHER_DASHBOARD);
    }

    private void loadAcademicYears() {
        try {
            var years = FXCollections.observableArrayList(
                    Main.getAppState().getMessageDispatcher().getAcademicYears().getAcademicYears());
            academicYearsById.clear();
            years.forEach(year -> academicYearsById.put(year.getId(), year));
            academicYearComboBox.setItems(years);
        } catch (Exception e) {
            statusLabel.setText("Unable to load academic years: " + e.getMessage());
        }
    }

    private void loadSemesters() {
        try {
            allSemesters.clear();
            allSemesters.addAll(Main.getAppState().getMessageDispatcher().getSemesters().getSemesters());
            filterSemesters(academicYearComboBox.getValue());
        } catch (Exception e) {
            statusLabel.setText("Unable to load semesters: " + e.getMessage());
        }
    }

    private void filterSemesters(AcademicYear selectedYear) {
        if (selectedYear == null) {
            semesterComboBox.setItems(FXCollections.observableArrayList());
            return;
        }
        List<Semester> filtered = allSemesters.stream()
                .filter(semester -> semester.getAcademicYearId() == selectedYear.getId() && semester.isActive())
                .toList();
        semesterComboBox.setItems(FXCollections.observableArrayList(filtered));
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
        draftedQuestionsList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(CreateQuizMessage.QuestionPayload item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : "Q" + item.getPosition() + ": " + item.getBody());
            }
        });
    }

    private String describeSemester(Semester semester) {
        AcademicYear academicYear = academicYearsById.get(semester.getAcademicYearId());
        String yearLabel = academicYear == null ? "Academic Year #" + semester.getAcademicYearId() : academicYear.getLabel();
        return semester.getName() + " (" + yearLabel + ")";
    }

    private CreateQuizMessage.QuestionPayload buildDraftQuestion() {
        String body = questionBodyArea.getText() == null ? "" : questionBodyArea.getText().trim();
        if (body.isBlank()) {
            throw new IllegalArgumentException("Question body is required.");
        }

        List<CreateQuizMessage.ChoicePayload> choices = new ArrayList<>();
        addChoiceIfPresent(choices, choiceAField.getText(), "A");
        addChoiceIfPresent(choices, choiceBField.getText(), "B");
        addChoiceIfPresent(choices, choiceCField.getText(), "C");
        addChoiceIfPresent(choices, choiceDField.getText(), "D");

        if (choices.size() < 2) {
            throw new IllegalArgumentException("Enter at least two choices.");
        }
        String correctChoice = correctChoiceComboBox.getValue();
        if (correctChoice == null) {
            throw new IllegalArgumentException("Select the correct choice.");
        }

        int position = draftedQuestions.size() + 1;
        List<CreateQuizMessage.ChoicePayload> finalChoices = new ArrayList<>();
        for (CreateQuizMessage.ChoicePayload choice : choices) {
            boolean correct = choice.getBody().equals(choiceBodyForLabel(correctChoice));
            finalChoices.add(new CreateQuizMessage.ChoicePayload(choice.getBody(), correct));
        }
        long correctCount = finalChoices.stream().filter(CreateQuizMessage.ChoicePayload::isCorrect).count();
        if (correctCount != 1) {
            throw new IllegalArgumentException("Correct choice must point to a filled choice.");
        }
        return new CreateQuizMessage.QuestionPayload(body, position, finalChoices);
    }

    private void addChoiceIfPresent(List<CreateQuizMessage.ChoicePayload> choices, String text, String label) {
        if (text != null && !text.trim().isBlank()) {
            choices.add(new CreateQuizMessage.ChoicePayload(text.trim(), false));
        } else if ("A".equals(label) || "B".equals(label)) {
            // Primary two choices are required; validation happens later for count.
        }
    }

    private String choiceBodyForLabel(String label) {
        return switch (label) {
            case "A" -> choiceAField.getText() == null ? "" : choiceAField.getText().trim();
            case "B" -> choiceBField.getText() == null ? "" : choiceBField.getText().trim();
            case "C" -> choiceCField.getText() == null ? "" : choiceCField.getText().trim();
            case "D" -> choiceDField.getText() == null ? "" : choiceDField.getText().trim();
            default -> "";
        };
    }

    private void clearQuestionEditor() {
        questionBodyArea.clear();
        choiceAField.clear();
        choiceBField.clear();
        choiceCField.clear();
        choiceDField.clear();
        correctChoiceComboBox.getSelectionModel().clearSelection();
    }

    private void clearQuizForm() {
        titleField.clear();
        draftedQuestions.clear();
        draftedQuestionsList.setItems(FXCollections.observableArrayList());
        clearQuestionEditor();
    }
}

package com.quizapp.client.app;

public enum AppRoute {
    LOGIN("/com/quizapp/client/fxml/login.fxml", "UniQuiz Login"),
    REGISTER("/com/quizapp/client/fxml/register.fxml", "UniQuiz Register"),
    TEACHER_DASHBOARD("/com/quizapp/client/fxml/teacher/dashboard.fxml", "UniQuiz Teacher"),
    STUDENT_LOBBY("/com/quizapp/client/fxml/student/lobby.fxml", "UniQuiz Student"),
    STUDENT_QUIZ("/com/quizapp/client/fxml/student/quiz.fxml", "UniQuiz Quiz"),
    TEACHER_ACADEMIC_YEAR("/com/quizapp/client/fxml/teacher/academic-year.fxml", "UniQuiz Academic Year"),
    TEACHER_BATCH("/com/quizapp/client/fxml/teacher/batch.fxml", "UniQuiz Batch"),
    TEACHER_SEMESTER("/com/quizapp/client/fxml/teacher/semester.fxml", "UniQuiz Semester"),
    TEACHER_SECTION("/com/quizapp/client/fxml/teacher/section.fxml", "UniQuiz Section");

    private final String fxmlPath;
    private final String title;

    AppRoute(String fxmlPath, String title) {
        this.fxmlPath = fxmlPath;
        this.title = title;
    }

    public String getFxmlPath() {
        return fxmlPath;
    }

    public String getTitle() {
        return title;
    }
}

package com.quizapp.client.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

public class AppNavigator {
    private static final String THEME_PATH = "/com/quizapp/client/css/theme.css";

    private final Stage stage;
    private final AppState appState;

    public AppNavigator(Stage stage, AppState appState) {
        this.stage = stage;
        this.appState = appState;
    }

    public void navigateTo(AppRoute route) {
        try {
            Scene scene = loadScene(route);
            appState.setCurrentRoute(route);
            stage.setTitle(route.getTitle());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showLoadError(route, e);
            throw new IllegalStateException("Failed to load route: " + route, e);
        }
    }

    private Scene loadScene(AppRoute route) throws IOException {
        URL resource = AppNavigator.class.getResource(route.getFxmlPath());
        if (resource == null) {
            throw new IOException("FXML not found: " + route.getFxmlPath());
        }

        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root, 960, 640);
        URL theme = AppNavigator.class.getResource(THEME_PATH);
        if (theme != null) {
            scene.getStylesheets().add(theme.toExternalForm());
        }
        return scene;
    }

    private void showLoadError(AppRoute route, IOException error) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Route Load Error");
        alert.setHeaderText("Failed to load route: " + route);

        Throwable root = rootCause(error);
        alert.setContentText(root.getClass().getSimpleName() + ": " + root.getMessage());

        TextArea details = new TextArea(stackTrace(error));
        details.setEditable(false);
        details.setWrapText(false);
        details.setPrefRowCount(18);
        details.setPrefColumnCount(90);

        VBox container = new VBox(details);
        container.setSpacing(8);
        alert.getDialogPane().setExpandableContent(container);
        alert.getDialogPane().setExpanded(true);
        alert.showAndWait();
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    private String stackTrace(Throwable throwable) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);
        printWriter.flush();
        return writer.toString();
    }
}

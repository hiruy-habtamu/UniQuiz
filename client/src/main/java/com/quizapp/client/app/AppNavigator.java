package com.quizapp.client.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
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
}

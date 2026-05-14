package com.quizapp.client;

import com.quizapp.client.app.AppNavigator;
import com.quizapp.client.app.AppRoute;
import com.quizapp.client.app.AppState;
import com.quizapp.client.network.MessageDispatcher;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main {
    private static AppState appState;
    private static AppNavigator appNavigator;

    public static void main(String[] args) {
        Application.launch(ClientApplication.class, args);
    }

    public static AppState getAppState() {
        return appState;
    }

    public static AppNavigator getAppNavigator() {
        return appNavigator;
    }

    public static final class ClientApplication extends Application {
        @Override
        public void start(Stage stage) {
            appState = new AppState(new MessageDispatcher());
            appNavigator = new AppNavigator(stage, appState);
            stage.setMinWidth(800);
            stage.setMinHeight(520);
            appNavigator.navigateTo(AppRoute.LOGIN);
        }
    }
}

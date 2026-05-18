package com.quizapp.client.util;

import javafx.beans.value.ChangeListener;
import javafx.stage.Stage;

public class FullScreenGuard {
    private Stage stage;
    private ChangeListener<Boolean> listener;

    public void activate(Stage stage, Runnable onExit) {
        deactivate();
        this.stage = stage;
        this.listener = (ignored, oldValue, newValue) -> {
            if (Boolean.TRUE.equals(oldValue) && Boolean.FALSE.equals(newValue)) {
                onExit.run();
            }
        };
        stage.fullScreenProperty().addListener(listener);
        stage.setFullScreen(true);
    }

    public void deactivate() {
        if (stage != null && listener != null) {
            stage.fullScreenProperty().removeListener(listener);
        }
        listener = null;
        stage = null;
    }
}

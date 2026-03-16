package iti.exam.desktop.ui;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.util.concurrent.Callable;

public final class FxUtils {
    private FxUtils() {
    }

    public static <T> void runAsync(Callable<T> work, java.util.function.Consumer<T> onSuccess, java.util.function.Consumer<Throwable> onError) {
        Task<T> task = new Task<T>() {
            @Override
            protected T call() throws Exception {
                return work.call();
            }
        };

        task.setOnSucceeded(evt -> {
            if (onSuccess != null) {
                onSuccess.accept(task.getValue());
            }
        });
        task.setOnFailed(evt -> {
            Throwable ex = task.getException();
            if (onError != null) {
                onError.accept(ex);
            } else {
                showError("Error", ex == null ? "Unknown error" : ex.getMessage());
            }
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    public static void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}


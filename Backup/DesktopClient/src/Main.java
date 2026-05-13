import iti.exam.desktop.ui.FxApp;
import javafx.application.Application;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        File cacheDir = new File("javafx-cache");
        cacheDir.mkdirs();
        System.setProperty("javafx.cachedir", cacheDir.getAbsolutePath());
        Application.launch(FxApp.class, args);
    }
}

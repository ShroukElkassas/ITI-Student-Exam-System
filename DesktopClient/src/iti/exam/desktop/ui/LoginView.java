package iti.exam.desktop.ui;

import iti.exam.desktop.db.DbConfig;
import iti.exam.desktop.db.DbConnectionFactory;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;

public final class LoginView {
    private final Stage stage;
    private AppSession session;

    private final TextField hostField;
    private final TextField portField;
    private final TextField dbField;
    private final ComboBox<AppRole> roleBox;
    private final TextField userField;
    private final PasswordField passwordField;
    private final Label statusLabel;

    public LoginView(Stage owner, DbConfig baseConfig) {
        this.stage = new Stage();
        this.stage.initOwner(owner);
        this.stage.initModality(Modality.APPLICATION_MODAL);
        this.stage.setTitle("Login");

        this.hostField = new TextField(baseConfig.getHost());
        this.portField = new TextField(baseConfig.getPort());
        this.dbField = new TextField(baseConfig.getDatabase());

        this.roleBox = new ComboBox<AppRole>();
        this.roleBox.getItems().addAll(AppRole.ADMIN, AppRole.INSTRUCTOR, AppRole.STUDENT);
        this.roleBox.getSelectionModel().select(AppRole.ADMIN);

        this.userField = new TextField(AppRole.ADMIN.getLoginName());
        this.userField.setDisable(true);

        this.passwordField = new PasswordField();
        this.passwordField.setText(defaultPasswordFor(AppRole.ADMIN));

        this.statusLabel = new Label();

        roleBox.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                return;
            }
            userField.setText(newV.getLoginName());
            String currentPassword = passwordField.getText();
            String oldDefault = oldV == null ? null : defaultPasswordFor(oldV);
            boolean shouldReplace =
                    currentPassword == null
                            || currentPassword.trim().isEmpty()
                            || (oldDefault != null && oldDefault.equals(currentPassword));
            if (shouldReplace) {
                passwordField.setText(defaultPasswordFor(newV));
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        grid.add(new Label("Host"), 0, 0);
        grid.add(hostField, 1, 0);
        grid.add(new Label("Port"), 0, 1);
        grid.add(portField, 1, 1);
        grid.add(new Label("Database"), 0, 2);
        grid.add(dbField, 1, 2);
        grid.add(new Label("Role"), 0, 3);
        grid.add(roleBox, 1, 3);
        grid.add(new Label("Login"), 0, 4);
        grid.add(userField, 1, 4);
        grid.add(new Label("Password"), 0, 5);
        grid.add(passwordField, 1, 5);

        Button loginBtn = new Button("Login");
        Button cancelBtn = new Button("Cancel");

        loginBtn.setDefaultButton(true);
        cancelBtn.setCancelButton(true);

        loginBtn.setOnAction(e -> tryLogin());
        cancelBtn.setOnAction(e -> stage.close());

        HBox buttons = new HBox(10, loginBtn, cancelBtn);
        grid.add(buttons, 1, 6);
        grid.add(statusLabel, 1, 7);

        GridPane.setHgrow(hostField, Priority.ALWAYS);
        GridPane.setHgrow(portField, Priority.ALWAYS);
        GridPane.setHgrow(dbField, Priority.ALWAYS);
        GridPane.setHgrow(roleBox, Priority.ALWAYS);
        GridPane.setHgrow(userField, Priority.ALWAYS);
        GridPane.setHgrow(passwordField, Priority.ALWAYS);

        stage.setScene(new Scene(grid, 450, 360));
    }

    public AppSession showAndWait() {
        stage.showAndWait();
        return session;
    }

    private void tryLogin() {
        statusLabel.setText("");

        String host = trimOrNull(hostField.getText());
        String port = trimOrNull(portField.getText());
        String db = trimOrNull(dbField.getText());
        AppRole role = roleBox.getValue();
        String login = trimOrNull(userField.getText());
        String password = passwordField.getText();

        if (host == null || port == null || db == null || login == null || password == null || password.trim().isEmpty()) {
            statusLabel.setText("Please fill all fields.");
            return;
        }

        DbConfig config = new DbConfig(host, port, db, login, password);
        DbConnectionFactory cf = new DbConnectionFactory(config);

        try (Connection ignored = cf.openConnection()) {
            this.session = new AppSession(role, new AppContext(config));
            stage.close();
        } catch (Exception ex) {
            statusLabel.setText("Login failed: " + ex.getMessage());
        }
    }

    private static String trimOrNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String defaultPasswordFor(AppRole role) {
        if (role == AppRole.ADMIN) return "Admin@123";
        if (role == AppRole.INSTRUCTOR) return "Instructor@123";
        if (role == AppRole.STUDENT) return "Student@123";
        return "";
    }
}

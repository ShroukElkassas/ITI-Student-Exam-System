package iti.exam.desktop.ui.views;

import iti.exam.desktop.ui.AppContext;
import iti.exam.desktop.ui.AppSession;
import iti.exam.desktop.ui.FxUtils;
import iti.exam.desktop.util.RowUtils;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.List;
import java.util.Map;

public final class StudentHomeView {
    private final AppSession session;
    private final AppContext context;
    private final BorderPane root;

    private final TextField studentIdField;
    private final Label statusLabel;

    private final Label nameValue;
    private final Label emailValue;
    private final Label phoneValue;
    private final Label trackIdValue;
    private final Label trackNameValue;

    public StudentHomeView(AppSession session) {
        this.session = session;
        this.context = session.getContext();
        this.root = new BorderPane();

        this.studentIdField = new TextField();
        Integer existing = session.getStudentId();
        if (existing != null) {
            studentIdField.setText(String.valueOf(existing));
        }

        Button saveBtn = new Button("Save");
        Button loadBtn = new Button("Load Profile");
        saveBtn.setOnAction(e -> saveStudentId());
        loadBtn.setOnAction(e -> loadProfile());

        HBox top = new HBox(10, new Label("StudentID"), studentIdField, saveBtn, loadBtn);
        top.setPadding(new Insets(10));
        HBox.setHgrow(studentIdField, Priority.ALWAYS);

        this.statusLabel = new Label();
        top.getChildren().add(statusLabel);

        this.nameValue = new Label("");
        this.emailValue = new Label("");
        this.phoneValue = new Label("");
        this.trackIdValue = new Label("");
        this.trackNameValue = new Label("");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(12));

        grid.add(new Label("Name"), 0, 0);
        grid.add(nameValue, 1, 0);
        grid.add(new Label("Email"), 0, 1);
        grid.add(emailValue, 1, 1);
        grid.add(new Label("Phone"), 0, 2);
        grid.add(phoneValue, 1, 2);
        grid.add(new Label("TrackID"), 0, 3);
        grid.add(trackIdValue, 1, 3);
        grid.add(new Label("Track"), 0, 4);
        grid.add(trackNameValue, 1, 4);

        GridPane.setHgrow(nameValue, Priority.ALWAYS);
        GridPane.setHgrow(emailValue, Priority.ALWAYS);
        GridPane.setHgrow(phoneValue, Priority.ALWAYS);
        GridPane.setHgrow(trackIdValue, Priority.ALWAYS);
        GridPane.setHgrow(trackNameValue, Priority.ALWAYS);

        root.setTop(top);
        root.setCenter(grid);
    }

    public Node getNode() {
        return root;
    }

    private void saveStudentId() {
        Integer id = RowUtils.toNullableInt(studentIdField.getText());
        if (id == null) {
            statusLabel.setText("StudentID is required.");
            return;
        }
        session.setStudentId(id);
        statusLabel.setText("Saved.");
    }

    private void loadProfile() {
        Integer id = RowUtils.toNullableInt(studentIdField.getText());
        if (id == null) {
            statusLabel.setText("StudentID is required.");
            return;
        }
        session.setStudentId(id);
        statusLabel.setText("Loading...");

        FxUtils.runAsync(
                () -> context.students().selectStudents(id),
                this::setProfile,
                ex -> statusLabel.setText("Load failed: " + ex.getMessage())
        );
    }

    private void setProfile(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            statusLabel.setText("Student not found.");
            nameValue.setText("");
            emailValue.setText("");
            phoneValue.setText("");
            trackIdValue.setText("");
            trackNameValue.setText("");
            return;
        }

        Map<String, Object> row = rows.get(0);
        String name = RowUtils.toNullableString(RowUtils.getIgnoreCase(row, "StudentName"));
        String email = RowUtils.toNullableString(RowUtils.getIgnoreCase(row, "Email"));
        String phone = RowUtils.toNullableString(RowUtils.getIgnoreCase(row, "Phone"));
        Integer trackId = RowUtils.toNullableInt(RowUtils.getIgnoreCase(row, "TrackID"));
        String trackName = RowUtils.toNullableString(RowUtils.getIgnoreCase(row, "TrackName"));

        nameValue.setText(name == null ? "" : name);
        emailValue.setText(email == null ? "" : email);
        phoneValue.setText(phone == null ? "" : phone);
        trackIdValue.setText(trackId == null ? "" : String.valueOf(trackId));
        trackNameValue.setText(trackName == null ? "" : trackName);
        statusLabel.setText("Loaded.");
    }
}


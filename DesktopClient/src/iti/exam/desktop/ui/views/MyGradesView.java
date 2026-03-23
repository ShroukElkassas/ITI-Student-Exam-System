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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.List;
import java.util.Map;

public final class MyGradesView {
    private final AppSession session;
    private final AppContext context;
    private final BorderPane root;
    private final MapTableView table;

    private final TextField studentIdField;
    private final Label statusLabel;

    public MyGradesView(AppSession session) {
        this.session = session;
        this.context = session.getContext();
        this.root = new BorderPane();
        this.table = new MapTableView();

        this.studentIdField = new TextField();
        Integer existing = session.getStudentId();
        if (existing != null) {
            studentIdField.setText(String.valueOf(existing));
        }
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refresh());
        this.statusLabel = new Label();

        HBox top = new HBox(10, new Label("StudentID"), studentIdField, refreshBtn, statusLabel);
        top.setPadding(new Insets(10));
        HBox.setHgrow(studentIdField, Priority.ALWAYS);

        root.setTop(top);
        root.setCenter(table.getNode());
    }

    public Node getNode() {
        return root;
    }

    private void refresh() {
        Integer studentId = RowUtils.toNullableInt(studentIdField.getText());
        if (studentId == null) {
            statusLabel.setText("StudentID is required.");
            table.setRows(null);
            return;
        }
        session.setStudentId(studentId);
        statusLabel.setText("Loading...");

        FxUtils.runAsync(
                () -> context.reports().studentGrades(studentId),
                this::setRows,
                ex -> statusLabel.setText("Load failed: " + ex.getMessage())
        );
    }

    private void setRows(List<Map<String, Object>> rows) {
        table.setRows(rows);
        statusLabel.setText(rows == null ? "Loaded." : ("Loaded: " + rows.size()));
    }
}


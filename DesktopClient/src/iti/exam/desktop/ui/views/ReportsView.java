package iti.exam.desktop.ui.views;

import iti.exam.desktop.ui.AppContext;
import iti.exam.desktop.ui.AppRole;
import iti.exam.desktop.ui.AppSession;
import iti.exam.desktop.ui.FxUtils;
import iti.exam.desktop.util.RowUtils;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.List;
import java.util.Map;

public final class ReportsView {
    private final AppSession session;
    private final AppContext context;
    private final BorderPane root;
    private final MapTableView table;

    public ReportsView(AppSession session) {
        this.session = session;
        this.context = session.getContext();
        this.root = new BorderPane();
        this.table = new MapTableView();

        ComboBox<String> reportBox = new ComboBox<String>();
        reportBox.getItems().addAll("Students By Department", "Student Grades", "Instructor Courses");
        reportBox.getSelectionModel().select(0);

        TextField p1 = new TextField();
        TextField p2 = new TextField();

        Label p1Label = new Label("Department No");
        Label p2Label = new Label("");
        p2.setDisable(true);

        updateLabels(reportBox.getValue(), p1Label, p2Label, p1, p2);

        reportBox.valueProperty().addListener((obs, oldV, newV) -> updateLabels(newV, p1Label, p2Label, p1, p2));

        Button runBtn = new Button("Run");
        runBtn.setOnAction(e -> runReport(reportBox.getValue(), p1, p2));

        HBox topBar = new HBox(10, new Label("Report"), reportBox, runBtn);
        topBar.setPadding(new Insets(10));

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.setPadding(new Insets(10));

        form.add(p1Label, 0, 0);
        form.add(p1, 1, 0);
        form.add(p2Label, 0, 1);
        form.add(p2, 1, 1);

        GridPane.setHgrow(p1, Priority.ALWAYS);
        GridPane.setHgrow(p2, Priority.ALWAYS);

        boolean canRunAll = session.getRole() != AppRole.STUDENT;
        if (!canRunAll) {
            reportBox.getItems().setAll("Student Grades");
            reportBox.getSelectionModel().select(0);
            updateLabels(reportBox.getValue(), p1Label, p2Label, p1, p2);
        }

        root.setTop(topBar);
        root.setCenter(table.getNode());
        root.setBottom(form);
    }

    public Node getNode() {
        return root;
    }

    private void updateLabels(String report, Label p1Label, Label p2Label, TextField p1, TextField p2) {
        if ("Students By Department".equals(report)) {
            p1Label.setText("Department No");
            p2Label.setText("");
            p2.setDisable(true);
            p2.setText("");
        } else if ("Student Grades".equals(report)) {
            p1Label.setText("Student ID");
            p2Label.setText("");
            p2.setDisable(true);
            p2.setText("");
        } else if ("Instructor Courses".equals(report)) {
            p1Label.setText("Instructor ID");
            p2Label.setText("");
            p2.setDisable(true);
            p2.setText("");
        }
    }

    private void runReport(String report, TextField p1, TextField p2) {
        Integer v1 = RowUtils.toNullableInt(p1.getText());
        if (v1 == null) {
            FxUtils.showError("Validation", "Enter a valid number.");
            return;
        }

        FxUtils.runAsync(
                () -> loadReport(report, v1),
                rows -> table.setRows(rows),
                ex -> FxUtils.showError("Report failed", ex.getMessage())
        );
    }

    private List<Map<String, Object>> loadReport(String report, int v1) throws Exception {
        if ("Students By Department".equals(report)) {
            return context.reports().studentsByDepartment(v1);
        }
        if ("Student Grades".equals(report)) {
            return context.reports().studentGrades(v1);
        }
        if ("Instructor Courses".equals(report)) {
            return context.reports().instructorCourses(v1);
        }
        return context.reports().studentGrades(v1);
    }
}


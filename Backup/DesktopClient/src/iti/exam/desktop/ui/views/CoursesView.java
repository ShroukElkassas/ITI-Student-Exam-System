package iti.exam.desktop.ui.views;

import iti.exam.desktop.models.Course;
import iti.exam.desktop.ui.AppContext;
import iti.exam.desktop.ui.AppSession;
import iti.exam.desktop.ui.FxUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.List;

public final class CoursesView {
    private final AppContext context;
    private final BorderPane root;
    private final TableView<CourseRow> table;
    private final ObservableList<CourseRow> items;

    public CoursesView(AppSession session) {
        this.context = session.getContext();
        this.root = new BorderPane();
        this.items = FXCollections.observableArrayList();

        this.table = new TableView<CourseRow>(items);
        this.table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<CourseRow, Integer> idCol = new TableColumn<CourseRow, Integer>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<CourseRow, Integer>("courseId"));
        idCol.setMaxWidth(110);

        TableColumn<CourseRow, String> nameCol = new TableColumn<CourseRow, String>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<CourseRow, String>("courseName"));

        TableColumn<CourseRow, Integer> minCol = new TableColumn<CourseRow, Integer>("Min");
        minCol.setCellValueFactory(new PropertyValueFactory<CourseRow, Integer>("minDegree"));
        minCol.setMaxWidth(120);

        TableColumn<CourseRow, Integer> maxCol = new TableColumn<CourseRow, Integer>("Max");
        maxCol.setCellValueFactory(new PropertyValueFactory<CourseRow, Integer>("maxDegree"));
        maxCol.setMaxWidth(120);

        this.table.getColumns().add(idCol);
        this.table.getColumns().add(nameCol);
        this.table.getColumns().add(minCol);
        this.table.getColumns().add(maxCol);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refresh());
        HBox topBar = new HBox(10, refreshBtn);
        topBar.setPadding(new Insets(10));

        TextField nameField = new TextField();
        TextField minField = new TextField();
        TextField maxField = new TextField();

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.setPadding(new Insets(10));

        form.add(new Label("Course Name"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(new Label("Min Degree"), 0, 1);
        form.add(minField, 1, 1);
        form.add(new Label("Max Degree"), 0, 2);
        form.add(maxField, 1, 2);

        Button addBtn = new Button("Add");
        Button updateBtn = new Button("Update");
        Button deleteBtn = new Button("Delete");

        boolean canWrite = session.getRole().canManageMasterData();
        addBtn.setDisable(!canWrite);
        updateBtn.setDisable(!canWrite);
        deleteBtn.setDisable(!canWrite);

        addBtn.setOnAction(e -> addCourse(nameField, minField, maxField));
        updateBtn.setOnAction(e -> updateCourse(nameField, minField, maxField));
        deleteBtn.setOnAction(e -> deleteCourse());

        HBox buttons = new HBox(10, addBtn, updateBtn, deleteBtn);
        form.add(buttons, 1, 3);

        GridPane.setHgrow(nameField, Priority.ALWAYS);
        GridPane.setHgrow(minField, Priority.ALWAYS);
        GridPane.setHgrow(maxField, Priority.ALWAYS);

        this.table.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                return;
            }
            nameField.setText(newV.getCourseName());
            minField.setText(newV.getMinDegree() == null ? "" : String.valueOf(newV.getMinDegree()));
            maxField.setText(newV.getMaxDegree() == null ? "" : String.valueOf(newV.getMaxDegree()));
        });

        root.setTop(topBar);
        root.setCenter(table);
        root.setBottom(form);

        refresh();
    }

    public Node getNode() {
        return root;
    }

    private void refresh() {
        FxUtils.runAsync(
                () -> context.courses().selectCourses(null, null),
                this::setRows,
                ex -> FxUtils.showError("Load courses failed", ex.getMessage())
        );
    }

    private void setRows(List<Course> courses) {
        items.clear();
        for (Course c : courses) {
            items.add(new CourseRow(c.getCourseId(), c.getCourseName(), c.getMinDegree(), c.getMaxDegree()));
        }
    }

    private void addCourse(TextField nameField, TextField minField, TextField maxField) {
        String name = trimOrNull(nameField.getText());
        Integer min = parseIntOrNull(minField.getText());
        Integer max = parseIntOrNull(maxField.getText());

        if (name == null || min == null || max == null) {
            FxUtils.showError("Validation", "Course name, Min, and Max are required.");
            return;
        }

        FxUtils.runAsync(
                () -> {
                    context.courses().insertCourse(name, min, max);
                    return null;
                },
                ignored -> refresh(),
                ex -> FxUtils.showError("Insert failed", ex.getMessage())
        );
    }

    private void updateCourse(TextField nameField, TextField minField, TextField maxField) {
        CourseRow selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxUtils.showError("Validation", "Select a course to update.");
            return;
        }

        String name = trimOrNull(nameField.getText());
        Integer min = parseIntOrNull(minField.getText());
        Integer max = parseIntOrNull(maxField.getText());

        if (name == null || min == null || max == null) {
            FxUtils.showError("Validation", "Course name, Min, and Max are required.");
            return;
        }

        FxUtils.runAsync(
                () -> {
                    context.courses().updateCourse(selected.getCourseId(), name, min, max);
                    return null;
                },
                ignored -> refresh(),
                ex -> FxUtils.showError("Update failed", ex.getMessage())
        );
    }

    private void deleteCourse() {
        CourseRow selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxUtils.showError("Validation", "Select a course to delete.");
            return;
        }

        FxUtils.runAsync(
                () -> {
                    context.courses().deleteCourse(selected.getCourseId());
                    return null;
                },
                ignored -> refresh(),
                ex -> FxUtils.showError("Delete failed", ex.getMessage())
        );
    }

    private static String trimOrNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static Integer parseIntOrNull(String s) {
        String t = trimOrNull(s);
        if (t == null) {
            return null;
        }
        try {
            return Integer.valueOf(t);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static final class CourseRow {
        private final int courseId;
        private final String courseName;
        private final Integer minDegree;
        private final Integer maxDegree;

        public CourseRow(int courseId, String courseName, Integer minDegree, Integer maxDegree) {
            this.courseId = courseId;
            this.courseName = courseName;
            this.minDegree = minDegree;
            this.maxDegree = maxDegree;
        }

        public int getCourseId() {
            return courseId;
        }

        public String getCourseName() {
            return courseName;
        }

        public Integer getMinDegree() {
            return minDegree;
        }

        public Integer getMaxDegree() {
            return maxDegree;
        }
    }
}

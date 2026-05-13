package iti.exam.desktop.ui.views;

import iti.exam.desktop.ui.AppContext;
import iti.exam.desktop.ui.AppRole;
import iti.exam.desktop.ui.AppSession;
import iti.exam.desktop.ui.FxUtils;
import iti.exam.desktop.util.RowUtils;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class InstructorsView {
    private final AppContext context;
    private final BorderPane root;
    private final TableView<InstructorRow> table;
    private final ObservableList<InstructorRow> items;

    private final TextField nameField;
    private final TextField emailField;
    private final TextField deptField;
    private final TextField courseIdField;

    public InstructorsView(AppSession session) {
        this.context = session.getContext();
        this.root = new BorderPane();
        this.items = FXCollections.observableArrayList();

        this.table = new TableView<InstructorRow>(items);
        this.table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<InstructorRow, Integer> idCol = new TableColumn<InstructorRow, Integer>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<InstructorRow, Integer>("instructorId"));
        idCol.setMaxWidth(110);

        TableColumn<InstructorRow, String> nameCol = new TableColumn<InstructorRow, String>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<InstructorRow, String>("instructorName"));

        TableColumn<InstructorRow, String> emailCol = new TableColumn<InstructorRow, String>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<InstructorRow, String>("email"));

        TableColumn<InstructorRow, Integer> deptCol = new TableColumn<InstructorRow, Integer>("Dept");
        deptCol.setCellValueFactory(new PropertyValueFactory<InstructorRow, Integer>("departmentNo"));
        deptCol.setMaxWidth(120);

        this.table.getColumns().add(idCol);
        this.table.getColumns().add(nameCol);
        this.table.getColumns().add(emailCol);
        this.table.getColumns().add(deptCol);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refresh());
        HBox topBar = new HBox(10, refreshBtn);
        topBar.setPadding(new Insets(10));

        this.nameField = new TextField();
        this.emailField = new TextField();
        this.deptField = new TextField();
        this.courseIdField = new TextField();

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.setPadding(new Insets(10));

        form.add(new Label("Name"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(new Label("Email"), 0, 1);
        form.add(emailField, 1, 1);
        form.add(new Label("Department No"), 0, 2);
        form.add(deptField, 1, 2);
        form.add(new Label("Course ID"), 0, 3);
        form.add(courseIdField, 1, 3);

        Button addBtn = new Button("Add");
        Button updateBtn = new Button("Update");
        Button deleteBtn = new Button("Delete");
        Button assignCourseBtn = new Button("Assign Course");

        boolean canWrite = session.getRole() == AppRole.ADMIN;
        addBtn.setDisable(!canWrite);
        updateBtn.setDisable(!canWrite);
        deleteBtn.setDisable(!canWrite);
        assignCourseBtn.setDisable(!canWrite);

        addBtn.setOnAction(e -> addInstructor());
        updateBtn.setOnAction(e -> updateInstructor());
        deleteBtn.setOnAction(e -> deleteInstructor());
        assignCourseBtn.setOnAction(e -> assignCourse());

        HBox buttons = new HBox(10, addBtn, updateBtn, deleteBtn, assignCourseBtn);
        form.add(buttons, 1, 4);

        GridPane.setHgrow(nameField, Priority.ALWAYS);
        GridPane.setHgrow(emailField, Priority.ALWAYS);
        GridPane.setHgrow(deptField, Priority.ALWAYS);
        GridPane.setHgrow(courseIdField, Priority.ALWAYS);

        this.table.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                return;
            }
            nameField.setText(newV.getInstructorName());
            emailField.setText(newV.getEmail());
            deptField.setText(newV.getDepartmentNo() == null ? "" : String.valueOf(newV.getDepartmentNo()));
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
                () -> context.instructors().selectInstructors(null, null),
                rows -> setRows(rows),
                ex -> FxUtils.showError("Load instructors failed", ex.getMessage())
        );
    }

    private void setRows(List<Map<String, Object>> rows) {
        List<InstructorRow> list = new ArrayList<InstructorRow>();
        for (Map<String, Object> row : rows) {
            int id = RowUtils.toInt(RowUtils.getIgnoreCase(row, "InstructorID"));
            String name = RowUtils.toNullableString(RowUtils.getIgnoreCase(row, "InstructorName"));
            if (name == null) {
                name = RowUtils.toNullableString(RowUtils.getIgnoreCase(row, "Name"));
            }
            String email = RowUtils.toNullableString(RowUtils.getIgnoreCase(row, "Email"));
            Integer dept = RowUtils.toNullableInt(RowUtils.getIgnoreCase(row, "DepartmentNo"));
            list.add(new InstructorRow(id, name, email, dept));
        }
        items.setAll(list);
    }

    private void addInstructor() {
        String name = RowUtils.toNullableString(nameField.getText());
        String email = RowUtils.toNullableString(emailField.getText());
        Integer dept = RowUtils.toNullableInt(deptField.getText());

        if (name == null || dept == null) {
            FxUtils.showError("Validation", "Name and Department No are required.");
            return;
        }

        FxUtils.runAsync(
                () -> {
                    context.instructors().insertInstructor(name, email, dept);
                    return null;
                },
                ignored -> refresh(),
                ex -> FxUtils.showError("Insert failed", ex.getMessage())
        );
    }

    private void updateInstructor() {
        InstructorRow selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxUtils.showError("Validation", "Select an instructor to update.");
            return;
        }

        String name = RowUtils.toNullableString(nameField.getText());
        String email = RowUtils.toNullableString(emailField.getText());
        Integer dept = RowUtils.toNullableInt(deptField.getText());

        if (dept == null) {
            FxUtils.showError("Validation", "Department No is required.");
            return;
        }

        FxUtils.runAsync(
                () -> {
                    context.instructors().updateInstructor(selected.getInstructorId(), name, email, dept);
                    return null;
                },
                ignored -> refresh(),
                ex -> FxUtils.showError("Update failed", ex.getMessage())
        );
    }

    private void deleteInstructor() {
        InstructorRow selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxUtils.showError("Validation", "Select an instructor to delete.");
            return;
        }

        FxUtils.runAsync(
                () -> {
                    context.instructors().deleteInstructor(selected.getInstructorId());
                    return null;
                },
                ignored -> refresh(),
                ex -> FxUtils.showError("Delete failed", ex.getMessage())
        );
    }

    private void assignCourse() {
        InstructorRow selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxUtils.showError("Validation", "Select an instructor.");
            return;
        }
        Integer courseId = RowUtils.toNullableInt(courseIdField.getText());
        if (courseId == null) {
            FxUtils.showError("Validation", "Course ID is required.");
            return;
        }

        FxUtils.runAsync(
                () -> {
                    context.instructors().assignInstructorToCourse(selected.getInstructorId(), courseId);
                    return null;
                },
                ignored -> FxUtils.showError("Done", "Assigned instructor to course."),
                ex -> FxUtils.showError("Assign failed", ex.getMessage())
        );
    }

    public static final class InstructorRow {
        private final int instructorId;
        private final String instructorName;
        private final String email;
        private final Integer departmentNo;

        public InstructorRow(int instructorId, String instructorName, String email, Integer departmentNo) {
            this.instructorId = instructorId;
            this.instructorName = instructorName;
            this.email = email;
            this.departmentNo = departmentNo;
        }

        public int getInstructorId() {
            return instructorId;
        }

        public String getInstructorName() {
            return instructorName;
        }

        public String getEmail() {
            return email;
        }

        public Integer getDepartmentNo() {
            return departmentNo;
        }
    }
}


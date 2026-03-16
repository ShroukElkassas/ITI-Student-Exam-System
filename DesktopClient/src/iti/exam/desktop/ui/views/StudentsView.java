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

public final class StudentsView {
    private final AppContext context;
    private final BorderPane root;
    private final TableView<StudentRow> table;
    private final ObservableList<StudentRow> items;

    private final TextField nameField;
    private final TextField emailField;
    private final TextField phoneField;
    private final TextField trackIdField;

    public StudentsView(AppSession session) {
        this.context = session.getContext();
        this.root = new BorderPane();
        this.items = FXCollections.observableArrayList();

        this.table = new TableView<StudentRow>(items);
        this.table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<StudentRow, Integer> idCol = new TableColumn<StudentRow, Integer>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<StudentRow, Integer>("studentId"));
        idCol.setMaxWidth(110);

        TableColumn<StudentRow, String> nameCol = new TableColumn<StudentRow, String>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<StudentRow, String>("studentName"));

        TableColumn<StudentRow, String> emailCol = new TableColumn<StudentRow, String>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<StudentRow, String>("email"));

        TableColumn<StudentRow, String> phoneCol = new TableColumn<StudentRow, String>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<StudentRow, String>("phone"));

        this.table.getColumns().add(idCol);
        this.table.getColumns().add(nameCol);
        this.table.getColumns().add(emailCol);
        this.table.getColumns().add(phoneCol);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refresh());
        HBox topBar = new HBox(10, refreshBtn);
        topBar.setPadding(new Insets(10));

        this.nameField = new TextField();
        this.emailField = new TextField();
        this.phoneField = new TextField();
        this.trackIdField = new TextField();

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.setPadding(new Insets(10));

        form.add(new Label("Name"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(new Label("Email"), 0, 1);
        form.add(emailField, 1, 1);
        form.add(new Label("Phone"), 0, 2);
        form.add(phoneField, 1, 2);
        form.add(new Label("Track ID"), 0, 3);
        form.add(trackIdField, 1, 3);

        Button addBtn = new Button("Add");
        Button updateBtn = new Button("Update");
        Button deleteBtn = new Button("Delete");
        Button assignTrackBtn = new Button("Assign Track");

        boolean canWrite = session.getRole() == AppRole.ADMIN;
        addBtn.setDisable(!canWrite);
        updateBtn.setDisable(!canWrite);
        deleteBtn.setDisable(!canWrite);
        assignTrackBtn.setDisable(!canWrite);

        addBtn.setOnAction(e -> addStudent());
        updateBtn.setOnAction(e -> updateStudent());
        deleteBtn.setOnAction(e -> deleteStudent());
        assignTrackBtn.setOnAction(e -> assignTrack());

        HBox buttons = new HBox(10, addBtn, updateBtn, deleteBtn, assignTrackBtn);
        form.add(buttons, 1, 4);

        GridPane.setHgrow(nameField, Priority.ALWAYS);
        GridPane.setHgrow(emailField, Priority.ALWAYS);
        GridPane.setHgrow(phoneField, Priority.ALWAYS);
        GridPane.setHgrow(trackIdField, Priority.ALWAYS);

        this.table.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                return;
            }
            nameField.setText(newV.getStudentName());
            emailField.setText(newV.getEmail());
            phoneField.setText(newV.getPhone());
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
                () -> context.students().selectStudents(null),
                rows -> setRows(rows),
                ex -> FxUtils.showError("Load students failed", ex.getMessage())
        );
    }

    private void setRows(List<Map<String, Object>> rows) {
        List<StudentRow> list = new ArrayList<StudentRow>();
        for (Map<String, Object> row : rows) {
            int id = RowUtils.toInt(RowUtils.getIgnoreCase(row, "StudentID"));
            String name = RowUtils.toNullableString(RowUtils.getIgnoreCase(row, "StudentName"));
            String email = RowUtils.toNullableString(RowUtils.getIgnoreCase(row, "Email"));
            String phone = RowUtils.toNullableString(RowUtils.getIgnoreCase(row, "Phone"));
            list.add(new StudentRow(id, name, email, phone));
        }
        items.setAll(list);
    }

    private void addStudent() {
        String name = RowUtils.toNullableString(nameField.getText());
        String email = RowUtils.toNullableString(emailField.getText());
        String phone = RowUtils.toNullableString(phoneField.getText());

        if (name == null) {
            FxUtils.showError("Validation", "Student name is required.");
            return;
        }

        FxUtils.runAsync(
                () -> {
                    context.students().insertStudent(name, email, phone);
                    return null;
                },
                ignored -> refresh(),
                ex -> FxUtils.showError("Insert failed", ex.getMessage())
        );
    }

    private void updateStudent() {
        StudentRow selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxUtils.showError("Validation", "Select a student to update.");
            return;
        }

        String name = RowUtils.toNullableString(nameField.getText());
        String email = RowUtils.toNullableString(emailField.getText());
        String phone = RowUtils.toNullableString(phoneField.getText());

        FxUtils.runAsync(
                () -> {
                    context.students().updateStudent(selected.getStudentId(), name, email, phone);
                    return null;
                },
                ignored -> refresh(),
                ex -> FxUtils.showError("Update failed", ex.getMessage())
        );
    }

    private void deleteStudent() {
        StudentRow selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxUtils.showError("Validation", "Select a student to delete.");
            return;
        }

        FxUtils.runAsync(
                () -> {
                    context.students().deleteStudent(selected.getStudentId());
                    return null;
                },
                ignored -> refresh(),
                ex -> FxUtils.showError("Delete failed", ex.getMessage())
        );
    }

    private void assignTrack() {
        StudentRow selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxUtils.showError("Validation", "Select a student.");
            return;
        }
        Integer trackId = RowUtils.toNullableInt(trackIdField.getText());
        if (trackId == null) {
            FxUtils.showError("Validation", "Track ID is required.");
            return;
        }

        FxUtils.runAsync(
                () -> {
                    context.students().assignStudentToTrack(selected.getStudentId(), trackId);
                    return null;
                },
                ignored -> FxUtils.showError("Done", "Assigned student to track."),
                ex -> FxUtils.showError("Assign failed", ex.getMessage())
        );
    }

    public static final class StudentRow {
        private final int studentId;
        private final String studentName;
        private final String email;
        private final String phone;

        public StudentRow(int studentId, String studentName, String email, String phone) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.email = email;
            this.phone = phone;
        }

        public int getStudentId() {
            return studentId;
        }

        public String getStudentName() {
            return studentName;
        }

        public String getEmail() {
            return email;
        }

        public String getPhone() {
            return phone;
        }
    }
}

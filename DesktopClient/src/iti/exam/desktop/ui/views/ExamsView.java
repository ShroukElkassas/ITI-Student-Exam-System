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

public final class ExamsView {
    private final AppSession session;
    private final AppContext context;
    private final BorderPane root;
    private final TableView<ExamRow> table;
    private final ObservableList<ExamRow> items;

    private final TextField examNameField;
    private final TextField courseIdField;
    private final TextField totalQuestionsField;
    private final TextField genMcqField;
    private final TextField genTfField;

    public ExamsView(AppSession session) {
        this.session = session;
        this.context = session.getContext();
        this.root = new BorderPane();
        this.items = FXCollections.observableArrayList();

        this.table = new TableView<ExamRow>(items);
        this.table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<ExamRow, Integer> idCol = new TableColumn<ExamRow, Integer>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<ExamRow, Integer>("examId"));
        idCol.setMaxWidth(110);

        TableColumn<ExamRow, String> nameCol = new TableColumn<ExamRow, String>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<ExamRow, String>("examName"));

        TableColumn<ExamRow, Integer> courseCol = new TableColumn<ExamRow, Integer>("CourseID");
        courseCol.setCellValueFactory(new PropertyValueFactory<ExamRow, Integer>("courseId"));
        courseCol.setMaxWidth(140);

        TableColumn<ExamRow, Integer> totalCol = new TableColumn<ExamRow, Integer>("TotalQuestions");
        totalCol.setCellValueFactory(new PropertyValueFactory<ExamRow, Integer>("totalQuestions"));
        totalCol.setMaxWidth(160);

        this.table.getColumns().add(idCol);
        this.table.getColumns().add(nameCol);
        this.table.getColumns().add(courseCol);
        this.table.getColumns().add(totalCol);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refresh());
        HBox topBar = new HBox(10, refreshBtn);
        topBar.setPadding(new Insets(10));

        this.examNameField = new TextField();
        this.courseIdField = new TextField();
        this.totalQuestionsField = new TextField();
        this.genMcqField = new TextField();
        this.genTfField = new TextField();

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.setPadding(new Insets(10));

        form.add(new Label("Exam Name"), 0, 0);
        form.add(examNameField, 1, 0);
        form.add(new Label("Course ID"), 0, 1);
        form.add(courseIdField, 1, 1);
        form.add(new Label("Total Questions"), 0, 2);
        form.add(totalQuestionsField, 1, 2);
        form.add(new Label("Generate MCQ"), 0, 3);
        form.add(genMcqField, 1, 3);
        form.add(new Label("Generate TF"), 0, 4);
        form.add(genTfField, 1, 4);

        Button addBtn = new Button("Add");
        Button updateBtn = new Button("Update");
        Button deleteBtn = new Button("Delete");
        Button generateBtn = new Button("Generate Exam");

        boolean canWrite = session.getRole().canManageQuestionsAndExams();
        addBtn.setDisable(!canWrite);
        updateBtn.setDisable(!canWrite);
        deleteBtn.setDisable(!canWrite);
        generateBtn.setDisable(!canWrite);

        addBtn.setOnAction(e -> addExam());
        updateBtn.setOnAction(e -> updateExam());
        deleteBtn.setOnAction(e -> deleteExam());
        generateBtn.setOnAction(e -> generateExam());

        HBox buttons = new HBox(10, addBtn, updateBtn, deleteBtn, generateBtn);
        form.add(buttons, 1, 5);

        GridPane.setHgrow(examNameField, Priority.ALWAYS);
        GridPane.setHgrow(courseIdField, Priority.ALWAYS);
        GridPane.setHgrow(totalQuestionsField, Priority.ALWAYS);
        GridPane.setHgrow(genMcqField, Priority.ALWAYS);
        GridPane.setHgrow(genTfField, Priority.ALWAYS);

        this.table.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                return;
            }
            examNameField.setText(newV.getExamName());
            courseIdField.setText(newV.getCourseId() == null ? "" : String.valueOf(newV.getCourseId()));
            totalQuestionsField.setText(newV.getTotalQuestions() == null ? "" : String.valueOf(newV.getTotalQuestions()));
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
                () -> context.exams().selectExam(null, null),
                this::setRows,
                ex -> FxUtils.showError("Load exams failed", ex.getMessage())
        );
    }

    private void setRows(List<Map<String, Object>> rows) {
        List<ExamRow> list = new ArrayList<ExamRow>();
        for (Map<String, Object> row : rows) {
            int id = RowUtils.toInt(RowUtils.getIgnoreCase(row, "ExamID"));
            String name = RowUtils.toNullableString(RowUtils.getIgnoreCase(row, "ExamName"));
            Integer courseId = RowUtils.toNullableInt(RowUtils.getIgnoreCase(row, "CourseID"));
            Integer total = RowUtils.toNullableInt(RowUtils.getIgnoreCase(row, "TotalQuestions"));
            list.add(new ExamRow(id, name, courseId, total));
        }
        items.setAll(list);
    }

    private void addExam() {
        String name = RowUtils.toNullableString(examNameField.getText());
        Integer courseId = RowUtils.toNullableInt(courseIdField.getText());
        Integer total = RowUtils.toNullableInt(totalQuestionsField.getText());

        if (name == null || courseId == null) {
            FxUtils.showError("Validation", "Exam name and Course ID are required.");
            return;
        }

        FxUtils.runAsync(
                () -> {
                    context.exams().insertExam(name, courseId, total);
                    return null;
                },
                ignored -> refresh(),
                ex -> FxUtils.showError("Insert failed", ex.getMessage())
        );
    }

    private void updateExam() {
        ExamRow selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxUtils.showError("Validation", "Select an exam to update.");
            return;
        }

        String name = RowUtils.toNullableString(examNameField.getText());
        Integer courseId = RowUtils.toNullableInt(courseIdField.getText());
        Integer total = RowUtils.toNullableInt(totalQuestionsField.getText());

        FxUtils.runAsync(
                () -> {
                    context.exams().updateExam(selected.getExamId(), name, courseId, total);
                    return null;
                },
                ignored -> refresh(),
                ex -> FxUtils.showError("Update failed", ex.getMessage())
        );
    }

    private void deleteExam() {
        ExamRow selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxUtils.showError("Validation", "Select an exam to delete.");
            return;
        }

        FxUtils.runAsync(
                () -> {
                    context.exams().deleteExam(selected.getExamId());
                    return null;
                },
                ignored -> refresh(),
                ex -> FxUtils.showError("Delete failed", ex.getMessage())
        );
    }

    private void generateExam() {
        Integer courseId = RowUtils.toNullableInt(courseIdField.getText());
        String name = RowUtils.toNullableString(examNameField.getText());
        Integer mcq = RowUtils.toNullableInt(genMcqField.getText());
        Integer tf = RowUtils.toNullableInt(genTfField.getText());

        if (courseId == null || name == null || mcq == null || tf == null) {
            FxUtils.showError("Validation", "Course ID, Exam Name, MCQ count, and TF count are required.");
            return;
        }

        FxUtils.runAsync(
                () -> {
                    context.exams().generateExam(courseId, name, mcq, tf);
                    return null;
                },
                ignored -> {
                    refresh();
                    FxUtils.showError("Done", "GenerateExam completed.");
                },
                ex -> FxUtils.showError("Generate failed", ex.getMessage())
        );
    }

    public static final class ExamRow {
        private final int examId;
        private final String examName;
        private final Integer courseId;
        private final Integer totalQuestions;

        public ExamRow(int examId, String examName, Integer courseId, Integer totalQuestions) {
            this.examId = examId;
            this.examName = examName;
            this.courseId = courseId;
            this.totalQuestions = totalQuestions;
        }

        public int getExamId() {
            return examId;
        }

        public String getExamName() {
            return examName;
        }

        public Integer getCourseId() {
            return courseId;
        }

        public Integer getTotalQuestions() {
            return totalQuestions;
        }
    }
}


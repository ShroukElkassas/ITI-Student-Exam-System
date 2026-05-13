package iti.exam.desktop.ui.views;

import iti.exam.desktop.ui.AppContext;
import iti.exam.desktop.ui.AppSession;
import iti.exam.desktop.ui.FxUtils;
import iti.exam.desktop.util.RowUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ExamComposerView {
    private final AppContext context;
    private final BorderPane root;

    private final ObservableList<ExamRow> examItems;
    private final TableView<ExamRow> examsTable;

    private final ObservableList<ExamQuestionRow> examQuestionItems;
    private final TableView<ExamQuestionRow> examQuestionsTable;

    private final ObservableList<QuestionPickerRow> questionPickerItems;
    private final TableView<QuestionPickerRow> questionPickerTable;

    private final TextField examsFilterCourseIdField;
    private final Button refreshExamsBtn;

    private final ComboBox<String> pickerTypeBox;
    private final Button refreshPickerBtn;

    private final TextField orderNoField;

    public ExamComposerView(AppSession session) {
        this.context = session.getContext();
        this.root = new BorderPane();

        boolean canWrite = session.getRole().canManageQuestionsAndExams();

        this.examItems = FXCollections.observableArrayList();
        this.examsTable = new TableView<ExamRow>(examItems);
        this.examsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<ExamRow, Integer> eIdCol = new TableColumn<ExamRow, Integer>("ExamID");
        eIdCol.setCellValueFactory(new PropertyValueFactory<ExamRow, Integer>("examId"));
        eIdCol.setMaxWidth(110);

        TableColumn<ExamRow, String> eNameCol = new TableColumn<ExamRow, String>("Name");
        eNameCol.setCellValueFactory(new PropertyValueFactory<ExamRow, String>("examName"));

        TableColumn<ExamRow, Integer> eCourseCol = new TableColumn<ExamRow, Integer>("CourseID");
        eCourseCol.setCellValueFactory(new PropertyValueFactory<ExamRow, Integer>("courseId"));
        eCourseCol.setMaxWidth(120);

        TableColumn<ExamRow, Integer> eTotalCol = new TableColumn<ExamRow, Integer>("TotalQ");
        eTotalCol.setCellValueFactory(new PropertyValueFactory<ExamRow, Integer>("totalQuestions"));
        eTotalCol.setMaxWidth(110);

        this.examsTable.getColumns().add(eIdCol);
        this.examsTable.getColumns().add(eNameCol);
        this.examsTable.getColumns().add(eCourseCol);
        this.examsTable.getColumns().add(eTotalCol);

        this.examQuestionItems = FXCollections.observableArrayList();
        this.examQuestionsTable = new TableView<ExamQuestionRow>(examQuestionItems);
        this.examQuestionsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<ExamQuestionRow, Integer> eqOrderCol = new TableColumn<ExamQuestionRow, Integer>("Order");
        eqOrderCol.setCellValueFactory(new PropertyValueFactory<ExamQuestionRow, Integer>("orderNo"));
        eqOrderCol.setMaxWidth(90);

        TableColumn<ExamQuestionRow, Integer> eqQIdCol = new TableColumn<ExamQuestionRow, Integer>("QuestionID");
        eqQIdCol.setCellValueFactory(new PropertyValueFactory<ExamQuestionRow, Integer>("questionId"));
        eqQIdCol.setMaxWidth(120);

        TableColumn<ExamQuestionRow, String> eqTypeCol = new TableColumn<ExamQuestionRow, String>("Type");
        eqTypeCol.setCellValueFactory(new PropertyValueFactory<ExamQuestionRow, String>("questionType"));
        eqTypeCol.setMaxWidth(100);

        TableColumn<ExamQuestionRow, Integer> eqPointsCol = new TableColumn<ExamQuestionRow, Integer>("Points");
        eqPointsCol.setCellValueFactory(new PropertyValueFactory<ExamQuestionRow, Integer>("points"));
        eqPointsCol.setMaxWidth(110);

        TableColumn<ExamQuestionRow, String> eqTextCol = new TableColumn<ExamQuestionRow, String>("Question");
        eqTextCol.setCellValueFactory(new PropertyValueFactory<ExamQuestionRow, String>("questionText"));

        this.examQuestionsTable.getColumns().add(eqOrderCol);
        this.examQuestionsTable.getColumns().add(eqQIdCol);
        this.examQuestionsTable.getColumns().add(eqTypeCol);
        this.examQuestionsTable.getColumns().add(eqPointsCol);
        this.examQuestionsTable.getColumns().add(eqTextCol);

        this.questionPickerItems = FXCollections.observableArrayList();
        this.questionPickerTable = new TableView<QuestionPickerRow>(questionPickerItems);
        this.questionPickerTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<QuestionPickerRow, Integer> pIdCol = new TableColumn<QuestionPickerRow, Integer>("QuestionID");
        pIdCol.setCellValueFactory(new PropertyValueFactory<QuestionPickerRow, Integer>("questionId"));
        pIdCol.setMaxWidth(120);

        TableColumn<QuestionPickerRow, String> pTypeCol = new TableColumn<QuestionPickerRow, String>("Type");
        pTypeCol.setCellValueFactory(new PropertyValueFactory<QuestionPickerRow, String>("questionType"));
        pTypeCol.setMaxWidth(100);

        TableColumn<QuestionPickerRow, Integer> pPointsCol = new TableColumn<QuestionPickerRow, Integer>("Points");
        pPointsCol.setCellValueFactory(new PropertyValueFactory<QuestionPickerRow, Integer>("points"));
        pPointsCol.setMaxWidth(110);

        TableColumn<QuestionPickerRow, String> pTextCol = new TableColumn<QuestionPickerRow, String>("Question");
        pTextCol.setCellValueFactory(new PropertyValueFactory<QuestionPickerRow, String>("questionText"));

        this.questionPickerTable.getColumns().add(pIdCol);
        this.questionPickerTable.getColumns().add(pTypeCol);
        this.questionPickerTable.getColumns().add(pPointsCol);
        this.questionPickerTable.getColumns().add(pTextCol);

        this.examsFilterCourseIdField = new TextField();
        this.refreshExamsBtn = new Button("Refresh Exams");
        this.refreshExamsBtn.setOnAction(e -> refreshExams());

        HBox top = new HBox(10, new Label("CourseID"), examsFilterCourseIdField, refreshExamsBtn);
        top.setPadding(new Insets(10));

        VBox examsPane = new VBox(10, new Label("Exams"), examsTable);
        examsPane.setPadding(new Insets(10));

        this.orderNoField = new TextField();
        Button updateOrderBtn = new Button("Update Order");
        Button removeBtn = new Button("Remove");
        updateOrderBtn.setDisable(!canWrite);
        removeBtn.setDisable(!canWrite);

        updateOrderBtn.setOnAction(e -> updateOrder());
        removeBtn.setOnAction(e -> removeFromExam());

        GridPane eqForm = new GridPane();
        eqForm.setHgap(10);
        eqForm.setVgap(8);
        eqForm.setPadding(new Insets(10));
        eqForm.add(new Label("OrderNo"), 0, 0);
        eqForm.add(orderNoField, 1, 0);
        HBox eqButtons = new HBox(10, updateOrderBtn, removeBtn);
        eqForm.add(eqButtons, 1, 1);
        GridPane.setHgrow(orderNoField, Priority.ALWAYS);

        VBox examQuestionsPane = new VBox(10, new Label("Exam Questions"), examQuestionsTable, eqForm);
        examQuestionsPane.setPadding(new Insets(10));

        this.pickerTypeBox = new ComboBox<String>();
        this.pickerTypeBox.getItems().addAll("", "MCQ", "TF");
        this.pickerTypeBox.getSelectionModel().select(0);

        this.refreshPickerBtn = new Button("Refresh Bank");
        this.refreshPickerBtn.setOnAction(e -> refreshQuestionBank());

        Button addBtn = new Button("Add To Exam");
        addBtn.setDisable(!canWrite);
        addBtn.setOnAction(e -> addToExam());

        HBox pickerControls = new HBox(10, new Label("Type"), pickerTypeBox, refreshPickerBtn, addBtn);
        pickerControls.setPadding(new Insets(10));

        VBox pickerPane = new VBox(10, new Label("Question Bank"), pickerControls, questionPickerTable);
        pickerPane.setPadding(new Insets(10));

        SplitPane split = new SplitPane();
        split.getItems().add(examsPane);
        split.getItems().add(examQuestionsPane);
        split.getItems().add(pickerPane);
        split.setDividerPositions(0.28, 0.62);

        this.examsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                examQuestionItems.clear();
                questionPickerItems.clear();
                orderNoField.setText("");
                return;
            }
            refreshExamQuestions();
            refreshQuestionBank();
        });

        this.examQuestionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                orderNoField.setText("");
                return;
            }
            orderNoField.setText(newV.getOrderNo() == null ? "" : String.valueOf(newV.getOrderNo()));
        });

        root.setTop(top);
        root.setCenter(split);

        refreshExams();
    }

    public Node getNode() {
        return root;
    }

    private void refreshExams() {
        Integer courseId = RowUtils.toNullableInt(examsFilterCourseIdField.getText());
        FxUtils.runAsync(
                () -> context.exams().selectExam(null, courseId),
                this::setExamRows,
                ex -> FxUtils.showError("Load exams failed", ex.getMessage())
        );
    }

    private void setExamRows(List<Map<String, Object>> rows) {
        List<ExamRow> list = new ArrayList<ExamRow>();
        for (Map<String, Object> row : rows) {
            int id = RowUtils.toInt(RowUtils.getIgnoreCase(row, "ExamID"));
            String name = RowUtils.toNullableString(RowUtils.getIgnoreCase(row, "ExamName"));
            Integer courseId = RowUtils.toNullableInt(RowUtils.getIgnoreCase(row, "CourseID"));
            Integer total = RowUtils.toNullableInt(RowUtils.getIgnoreCase(row, "TotalQuestions"));
            list.add(new ExamRow(id, name, courseId, total));
        }
        examItems.setAll(list);
    }

    private void refreshExamQuestions() {
        ExamRow exam = examsTable.getSelectionModel().getSelectedItem();
        if (exam == null) {
            examQuestionItems.clear();
            return;
        }

        FxUtils.runAsync(
                () -> {
                    List<Map<String, Object>> mappings = context.exams().selectExamQuestion(exam.getExamId(), null);
                    List<Map<String, Object>> questions = context.questions().selectQuestions(null, exam.getCourseId(), null);
                    Map<Integer, QuestionInfo> byId = new HashMap<Integer, QuestionInfo>();
                    for (Map<String, Object> q : questions) {
                        int id = RowUtils.toInt(RowUtils.getIgnoreCase(q, "QuestionID"));
                        String text = RowUtils.toNullableString(RowUtils.getIgnoreCase(q, "QuestionText"));
                        String type = RowUtils.toNullableString(RowUtils.getIgnoreCase(q, "QuestionType"));
                        Integer points = RowUtils.toNullableInt(RowUtils.getIgnoreCase(q, "Points"));
                        byId.put(id, new QuestionInfo(text, type, points));
                    }
                    List<ExamQuestionRow> list = new ArrayList<ExamQuestionRow>();
                    for (Map<String, Object> m : mappings) {
                        int qid = RowUtils.toInt(RowUtils.getIgnoreCase(m, "QuestionID"));
                        Integer order = RowUtils.toNullableInt(RowUtils.getIgnoreCase(m, "OrderNo"));
                        QuestionInfo info = byId.get(qid);
                        String text = info == null ? null : info.text;
                        String type = info == null ? null : info.type;
                        Integer points = info == null ? null : info.points;
                        list.add(new ExamQuestionRow(exam.getExamId(), qid, order, text, type, points));
                    }
                    return list;
                },
                list -> examQuestionItems.setAll(list),
                ex -> FxUtils.showError("Load exam questions failed", ex.getMessage())
        );
    }

    private void refreshQuestionBank() {
        ExamRow exam = examsTable.getSelectionModel().getSelectedItem();
        if (exam == null) {
            questionPickerItems.clear();
            return;
        }

        String type = RowUtils.toNullableString(pickerTypeBox.getValue());
        FxUtils.runAsync(
                () -> context.questions().selectQuestions(null, exam.getCourseId(), type),
                this::setPickerRows,
                ex -> FxUtils.showError("Load question bank failed", ex.getMessage())
        );
    }

    private void setPickerRows(List<Map<String, Object>> rows) {
        List<QuestionPickerRow> list = new ArrayList<QuestionPickerRow>();
        for (Map<String, Object> row : rows) {
            int id = RowUtils.toInt(RowUtils.getIgnoreCase(row, "QuestionID"));
            String text = RowUtils.toNullableString(RowUtils.getIgnoreCase(row, "QuestionText"));
            String type = RowUtils.toNullableString(RowUtils.getIgnoreCase(row, "QuestionType"));
            Integer points = RowUtils.toNullableInt(RowUtils.getIgnoreCase(row, "Points"));
            list.add(new QuestionPickerRow(id, text, type, points));
        }
        questionPickerItems.setAll(list);
    }

    private void addToExam() {
        ExamRow exam = examsTable.getSelectionModel().getSelectedItem();
        QuestionPickerRow q = questionPickerTable.getSelectionModel().getSelectedItem();
        if (exam == null || q == null) {
            FxUtils.showError("Validation", "Select an exam and a question.");
            return;
        }

        Integer orderNo = RowUtils.toNullableInt(orderNoField.getText());
        FxUtils.runAsync(
                () -> {
                    context.exams().insertExamQuestion(exam.getExamId(), q.getQuestionId(), orderNo);
                    return null;
                },
                ignored -> refreshExamQuestions(),
                ex -> FxUtils.showError("Add failed", ex.getMessage())
        );
    }

    private void removeFromExam() {
        ExamRow exam = examsTable.getSelectionModel().getSelectedItem();
        ExamQuestionRow row = examQuestionsTable.getSelectionModel().getSelectedItem();
        if (exam == null || row == null) {
            FxUtils.showError("Validation", "Select an exam question to remove.");
            return;
        }

        FxUtils.runAsync(
                () -> {
                    context.exams().deleteExamQuestion(exam.getExamId(), row.getQuestionId());
                    return null;
                },
                ignored -> refreshExamQuestions(),
                ex -> FxUtils.showError("Remove failed", ex.getMessage())
        );
    }

    private void updateOrder() {
        ExamRow exam = examsTable.getSelectionModel().getSelectedItem();
        ExamQuestionRow row = examQuestionsTable.getSelectionModel().getSelectedItem();
        Integer order = RowUtils.toNullableInt(orderNoField.getText());
        if (exam == null || row == null || order == null) {
            FxUtils.showError("Validation", "Select an exam question and enter OrderNo.");
            return;
        }

        FxUtils.runAsync(
                () -> {
                    context.exams().updateExamQuestion(exam.getExamId(), row.getQuestionId(), order);
                    return null;
                },
                ignored -> refreshExamQuestions(),
                ex -> FxUtils.showError("Update order failed", ex.getMessage())
        );
    }

    private static final class QuestionInfo {
        private final String text;
        private final String type;
        private final Integer points;

        private QuestionInfo(String text, String type, Integer points) {
            this.text = text;
            this.type = type;
            this.points = points;
        }
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

    public static final class ExamQuestionRow {
        private final int examId;
        private final int questionId;
        private final Integer orderNo;
        private final String questionText;
        private final String questionType;
        private final Integer points;

        public ExamQuestionRow(int examId, int questionId, Integer orderNo, String questionText, String questionType, Integer points) {
            this.examId = examId;
            this.questionId = questionId;
            this.orderNo = orderNo;
            this.questionText = questionText;
            this.questionType = questionType;
            this.points = points;
        }

        public int getExamId() {
            return examId;
        }

        public int getQuestionId() {
            return questionId;
        }

        public Integer getOrderNo() {
            return orderNo;
        }

        public String getQuestionText() {
            return questionText;
        }

        public String getQuestionType() {
            return questionType;
        }

        public Integer getPoints() {
            return points;
        }
    }

    public static final class QuestionPickerRow {
        private final int questionId;
        private final String questionText;
        private final String questionType;
        private final Integer points;

        public QuestionPickerRow(int questionId, String questionText, String questionType, Integer points) {
            this.questionId = questionId;
            this.questionText = questionText;
            this.questionType = questionType;
            this.points = points;
        }

        public int getQuestionId() {
            return questionId;
        }

        public String getQuestionText() {
            return questionText;
        }

        public String getQuestionType() {
            return questionType;
        }

        public Integer getPoints() {
            return points;
        }
    }
}


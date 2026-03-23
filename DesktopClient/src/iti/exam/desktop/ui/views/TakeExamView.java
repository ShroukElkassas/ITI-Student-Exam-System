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
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TakeExamView {
    private final AppSession session;
    private final AppContext context;
    private final BorderPane root;

    private final ObservableList<ExamRow> examItems;
    private final TableView<ExamRow> examsTable;

    private final TextField studentIdField;
    private final Label statusLabel;

    private final VBox examContent;
    private final ScrollPane scroll;

    private long startedAtMillis;
    private int currentExamId;
    private final List<QuestionWidget> widgets;

    public TakeExamView(AppSession session) {
        this.session = session;
        this.context = session.getContext();
        this.root = new BorderPane();
        this.widgets = new ArrayList<QuestionWidget>();

        this.examItems = FXCollections.observableArrayList();
        this.examsTable = new TableView<ExamRow>(examItems);
        this.examsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<ExamRow, Integer> idCol = new TableColumn<ExamRow, Integer>("ExamID");
        idCol.setCellValueFactory(new PropertyValueFactory<ExamRow, Integer>("examId"));
        idCol.setMaxWidth(110);

        TableColumn<ExamRow, String> nameCol = new TableColumn<ExamRow, String>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<ExamRow, String>("examName"));

        TableColumn<ExamRow, Integer> courseCol = new TableColumn<ExamRow, Integer>("CourseID");
        courseCol.setCellValueFactory(new PropertyValueFactory<ExamRow, Integer>("courseId"));
        courseCol.setMaxWidth(120);

        this.examsTable.getColumns().add(idCol);
        this.examsTable.getColumns().add(nameCol);
        this.examsTable.getColumns().add(courseCol);

        Integer existing = session.getStudentId();
        this.studentIdField = new TextField(existing == null ? "" : String.valueOf(existing));
        Button refreshExamsBtn = new Button("Refresh Exams");
        refreshExamsBtn.setOnAction(e -> refreshExams());
        Button startBtn = new Button("Start");
        startBtn.setOnAction(e -> startExam());
        Button submitBtn = new Button("Submit");
        submitBtn.setOnAction(e -> submit());
        this.statusLabel = new Label();

        HBox top = new HBox(10, new Label("StudentID"), studentIdField, refreshExamsBtn, startBtn, submitBtn, statusLabel);
        top.setPadding(new Insets(10));
        HBox.setHgrow(studentIdField, Priority.ALWAYS);

        VBox left = new VBox(10, new Label("Available Exams"), examsTable);
        left.setPadding(new Insets(10));

        this.examContent = new VBox(12);
        this.examContent.setPadding(new Insets(12));
        this.scroll = new ScrollPane(examContent);
        this.scroll.setFitToWidth(true);

        SplitPane split = new SplitPane(left, scroll);
        split.setDividerPositions(0.35);

        root.setTop(top);
        root.setCenter(split);

        refreshExams();
    }

    public Node getNode() {
        return root;
    }

    private void refreshExams() {
        statusLabel.setText("Loading...");
        FxUtils.runAsync(
                () -> context.exams().selectExam(null, null),
                rows -> {
                    List<ExamRow> list = new ArrayList<ExamRow>();
                    for (Map<String, Object> row : rows) {
                        int id = RowUtils.toInt(RowUtils.getIgnoreCase(row, "ExamID"));
                        String name = RowUtils.toNullableString(RowUtils.getIgnoreCase(row, "ExamName"));
                        Integer courseId = RowUtils.toNullableInt(RowUtils.getIgnoreCase(row, "CourseID"));
                        list.add(new ExamRow(id, name, courseId));
                    }
                    examItems.setAll(list);
                    statusLabel.setText("Loaded: " + list.size());
                },
                ex -> statusLabel.setText("Load failed: " + ex.getMessage())
        );
    }

    private void startExam() {
        Integer studentId = RowUtils.toNullableInt(studentIdField.getText());
        if (studentId == null) {
            FxUtils.showError("Validation", "StudentID is required.");
            return;
        }
        ExamRow exam = examsTable.getSelectionModel().getSelectedItem();
        if (exam == null) {
            FxUtils.showError("Validation", "Select an exam.");
            return;
        }

        session.setStudentId(studentId);
        this.currentExamId = exam.getExamId();
        this.startedAtMillis = System.currentTimeMillis();
        statusLabel.setText("Loading paper...");

        FxUtils.runAsync(
                () -> context.exams().getExamPaper(currentExamId),
                this::renderExamPaper,
                ex -> statusLabel.setText("Load failed: " + ex.getMessage())
        );
    }

    private void renderExamPaper(List<Map<String, Object>> rows) {
        widgets.clear();
        examContent.getChildren().clear();

        if (rows == null || rows.isEmpty()) {
            statusLabel.setText("No questions found.");
            return;
        }

        Map<Integer, ExamQuestion> byQuestion = new LinkedHashMap<Integer, ExamQuestion>();
        for (Map<String, Object> row : rows) {
            Integer orderNo = RowUtils.toNullableInt(RowUtils.getIgnoreCase(row, "OrderNo"));
            int questionId = RowUtils.toInt(RowUtils.getIgnoreCase(row, "QuestionID"));
            String text = RowUtils.toNullableString(RowUtils.getIgnoreCase(row, "QuestionText"));
            String type = RowUtils.toNullableString(RowUtils.getIgnoreCase(row, "QuestionType"));
            Integer points = RowUtils.toNullableInt(RowUtils.getIgnoreCase(row, "Points"));

            ExamQuestion q = byQuestion.get(questionId);
            if (q == null) {
                q = new ExamQuestion(orderNo == null ? 0 : orderNo, questionId, text, type, points);
                byQuestion.put(questionId, q);
            }

            Integer optionId = RowUtils.toNullableInt(RowUtils.getIgnoreCase(row, "OptionID"));
            if (optionId != null) {
                String optionText = RowUtils.toNullableString(RowUtils.getIgnoreCase(row, "OptionText"));
                Integer optionOrder = RowUtils.toNullableInt(RowUtils.getIgnoreCase(row, "OptionOrder"));
                q.options.add(new ExamOption(optionId, optionText, optionOrder));
            }
        }

        for (ExamQuestion q : byQuestion.values()) {
            QuestionWidget widget = new QuestionWidget(q);
            widgets.add(widget);
            examContent.getChildren().add(widget.getNode());
        }

        statusLabel.setText("Ready: " + widgets.size() + " questions");
    }

    private void submit() {
        Integer studentId = RowUtils.toNullableInt(studentIdField.getText());
        if (studentId == null) {
            FxUtils.showError("Validation", "StudentID is required.");
            return;
        }
        if (currentExamId <= 0) {
            FxUtils.showError("Validation", "Start an exam first.");
            return;
        }
        if (widgets.isEmpty()) {
            FxUtils.showError("Validation", "No questions loaded.");
            return;
        }

        StringBuilder xml = new StringBuilder();
        xml.append("<Answers>");
        for (QuestionWidget w : widgets) {
            Integer chosen = w.getChosenOptionId();
            if (chosen == null) {
                FxUtils.showError("Validation", "Answer all questions before submitting.");
                return;
            }
            xml.append("<Answer><QuestionID>")
                    .append(w.getQuestionId())
                    .append("</QuestionID><ChosenOptionID>")
                    .append(chosen)
                    .append("</ChosenOptionID></Answer>");
        }
        xml.append("</Answers>");

        session.setStudentId(studentId);
        Timestamp startTime = new Timestamp(startedAtMillis);
        Timestamp endTime = new Timestamp(System.currentTimeMillis());
        statusLabel.setText("Submitting...");

        FxUtils.runAsync(
                () -> {
                    context.exams().submitExamAnswers(studentId, currentExamId, startTime, endTime, xml.toString());
                    return null;
                },
                ignored -> statusLabel.setText("Submitted."),
                ex -> statusLabel.setText("Submit failed: " + ex.getMessage())
        );
    }

    private static final class ExamQuestion {
        private final int orderNo;
        private final int questionId;
        private final String text;
        private final String type;
        private final Integer points;
        private final List<ExamOption> options;

        private ExamQuestion(int orderNo, int questionId, String text, String type, Integer points) {
            this.orderNo = orderNo;
            this.questionId = questionId;
            this.text = text;
            this.type = type;
            this.points = points;
            this.options = new ArrayList<ExamOption>();
        }
    }

    private static final class ExamOption {
        private final int optionId;
        private final String text;
        private final Integer order;

        private ExamOption(int optionId, String text, Integer order) {
            this.optionId = optionId;
            this.text = text;
            this.order = order;
        }
    }

    private static final class QuestionWidget {
        private final ExamQuestion question;
        private final ToggleGroup group;
        private final VBox root;

        private QuestionWidget(ExamQuestion question) {
            this.question = question;
            this.group = new ToggleGroup();
            this.root = new VBox(6);

            String header = question.orderNo + ") " + (question.text == null ? "" : question.text);
            if (question.points != null) {
                header = header + " (" + question.points + ")";
            }

            Label title = new Label(header);
            root.getChildren().add(title);

            for (ExamOption opt : question.options) {
                String label = (opt.order == null ? "" : (opt.order + ". ")) + (opt.text == null ? "" : opt.text);
                RadioButton rb = new RadioButton(label);
                rb.setToggleGroup(group);
                rb.setUserData(opt.optionId);
                root.getChildren().add(rb);
            }

            root.setPadding(new Insets(10));
            root.setStyle("-fx-border-color: #d0d0d0; -fx-border-radius: 6; -fx-background-radius: 6;");
        }

        public Node getNode() {
            return root;
        }

        public int getQuestionId() {
            return question.questionId;
        }

        public Integer getChosenOptionId() {
            Toggle t = group.getSelectedToggle();
            if (t == null) {
                return null;
            }
            Object v = t.getUserData();
            if (v instanceof Number) {
                return ((Number) v).intValue();
            }
            return v == null ? null : Integer.parseInt(String.valueOf(v));
        }
    }

    public static final class ExamRow {
        private final int examId;
        private final String examName;
        private final Integer courseId;

        public ExamRow(int examId, String examName, Integer courseId) {
            this.examId = examId;
            this.examName = examName;
            this.courseId = courseId;
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
    }
}


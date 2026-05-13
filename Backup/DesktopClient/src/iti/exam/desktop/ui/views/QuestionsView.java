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
import java.util.List;
import java.util.Map;

public final class QuestionsView {
    private final AppContext context;
    private final BorderPane root;

    private final ObservableList<QuestionRow> questionItems;
    private final TableView<QuestionRow> questionsTable;

    private final ObservableList<OptionRow> optionItems;
    private final TableView<OptionRow> optionsTable;

    private final TextField filterCourseIdField;
    private final ComboBox<String> filterTypeBox;

    private final TextField courseIdField;
    private final ComboBox<String> typeBox;
    private final TextField pointsField;
    private final TextField questionTextField;

    private final TextField optionTextField;
    private final TextField optionOrderField;

    private final Label modelAnswerLabel;

    public QuestionsView(AppSession session) {
        this.context = session.getContext();
        this.root = new BorderPane();

        boolean canWrite = session.getRole().canManageQuestionsAndExams();

        this.questionItems = FXCollections.observableArrayList();
        this.questionsTable = new TableView<QuestionRow>(questionItems);
        this.questionsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<QuestionRow, Integer> qIdCol = new TableColumn<QuestionRow, Integer>("ID");
        qIdCol.setCellValueFactory(new PropertyValueFactory<QuestionRow, Integer>("questionId"));
        qIdCol.setMaxWidth(110);

        TableColumn<QuestionRow, Integer> qCourseCol = new TableColumn<QuestionRow, Integer>("CourseID");
        qCourseCol.setCellValueFactory(new PropertyValueFactory<QuestionRow, Integer>("courseId"));
        qCourseCol.setMaxWidth(120);

        TableColumn<QuestionRow, String> qTypeCol = new TableColumn<QuestionRow, String>("Type");
        qTypeCol.setCellValueFactory(new PropertyValueFactory<QuestionRow, String>("questionType"));
        qTypeCol.setMaxWidth(100);

        TableColumn<QuestionRow, Integer> qPointsCol = new TableColumn<QuestionRow, Integer>("Points");
        qPointsCol.setCellValueFactory(new PropertyValueFactory<QuestionRow, Integer>("points"));
        qPointsCol.setMaxWidth(110);

        TableColumn<QuestionRow, String> qTextCol = new TableColumn<QuestionRow, String>("Question");
        qTextCol.setCellValueFactory(new PropertyValueFactory<QuestionRow, String>("questionText"));

        this.questionsTable.getColumns().add(qIdCol);
        this.questionsTable.getColumns().add(qCourseCol);
        this.questionsTable.getColumns().add(qTypeCol);
        this.questionsTable.getColumns().add(qPointsCol);
        this.questionsTable.getColumns().add(qTextCol);

        this.optionItems = FXCollections.observableArrayList();
        this.optionsTable = new TableView<OptionRow>(optionItems);
        this.optionsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<OptionRow, Integer> oIdCol = new TableColumn<OptionRow, Integer>("OptionID");
        oIdCol.setCellValueFactory(new PropertyValueFactory<OptionRow, Integer>("optionId"));
        oIdCol.setMaxWidth(120);

        TableColumn<OptionRow, Integer> oOrderCol = new TableColumn<OptionRow, Integer>("Order");
        oOrderCol.setCellValueFactory(new PropertyValueFactory<OptionRow, Integer>("optionOrder"));
        oOrderCol.setMaxWidth(110);

        TableColumn<OptionRow, String> oTextCol = new TableColumn<OptionRow, String>("Option");
        oTextCol.setCellValueFactory(new PropertyValueFactory<OptionRow, String>("optionText"));

        this.optionsTable.getColumns().add(oIdCol);
        this.optionsTable.getColumns().add(oOrderCol);
        this.optionsTable.getColumns().add(oTextCol);

        this.filterCourseIdField = new TextField();
        this.filterTypeBox = new ComboBox<String>();
        this.filterTypeBox.getItems().addAll("", "MCQ", "TF");
        this.filterTypeBox.getSelectionModel().select(0);

        Button refreshQuestionsBtn = new Button("Refresh");
        refreshQuestionsBtn.setOnAction(e -> refreshQuestions());

        HBox filters = new HBox(10,
                new Label("CourseID"), filterCourseIdField,
                new Label("Type"), filterTypeBox,
                refreshQuestionsBtn
        );
        filters.setPadding(new Insets(10));

        this.courseIdField = new TextField();
        this.typeBox = new ComboBox<String>();
        this.typeBox.getItems().addAll("MCQ", "TF");
        this.typeBox.getSelectionModel().select(0);
        this.pointsField = new TextField();
        this.questionTextField = new TextField();

        Button addQuestionBtn = new Button("Add");
        Button updateQuestionBtn = new Button("Update");
        Button deleteQuestionBtn = new Button("Delete");
        addQuestionBtn.setDisable(!canWrite);
        updateQuestionBtn.setDisable(!canWrite);
        deleteQuestionBtn.setDisable(!canWrite);

        addQuestionBtn.setOnAction(e -> addQuestion());
        updateQuestionBtn.setOnAction(e -> updateQuestion());
        deleteQuestionBtn.setOnAction(e -> deleteQuestion());

        GridPane qForm = new GridPane();
        qForm.setHgap(10);
        qForm.setVgap(8);
        qForm.setPadding(new Insets(10));

        qForm.add(new Label("CourseID"), 0, 0);
        qForm.add(courseIdField, 1, 0);
        qForm.add(new Label("Type"), 0, 1);
        qForm.add(typeBox, 1, 1);
        qForm.add(new Label("Points"), 0, 2);
        qForm.add(pointsField, 1, 2);
        qForm.add(new Label("Text"), 0, 3);
        qForm.add(questionTextField, 1, 3);

        HBox qButtons = new HBox(10, addQuestionBtn, updateQuestionBtn, deleteQuestionBtn);
        qForm.add(qButtons, 1, 4);

        GridPane.setHgrow(courseIdField, Priority.ALWAYS);
        GridPane.setHgrow(typeBox, Priority.ALWAYS);
        GridPane.setHgrow(pointsField, Priority.ALWAYS);
        GridPane.setHgrow(questionTextField, Priority.ALWAYS);

        this.optionTextField = new TextField();
        this.optionOrderField = new TextField();

        Button refreshOptionsBtn = new Button("Refresh Options");
        Button addOptionBtn = new Button("Add Option");
        Button updateOptionBtn = new Button("Update Option");
        Button deleteOptionBtn = new Button("Delete Option");
        Button setCorrectBtn = new Button("Set Correct");

        refreshOptionsBtn.setOnAction(e -> refreshOptions());
        addOptionBtn.setDisable(!canWrite);
        updateOptionBtn.setDisable(!canWrite);
        deleteOptionBtn.setDisable(!canWrite);
        setCorrectBtn.setDisable(!canWrite);

        addOptionBtn.setOnAction(e -> addOption());
        updateOptionBtn.setOnAction(e -> updateOption());
        deleteOptionBtn.setOnAction(e -> deleteOption());
        setCorrectBtn.setOnAction(e -> setCorrectOption());

        GridPane oForm = new GridPane();
        oForm.setHgap(10);
        oForm.setVgap(8);
        oForm.setPadding(new Insets(10));

        oForm.add(new Label("Option Text"), 0, 0);
        oForm.add(optionTextField, 1, 0);
        oForm.add(new Label("Order"), 0, 1);
        oForm.add(optionOrderField, 1, 1);

        HBox oButtons = new HBox(10, refreshOptionsBtn, addOptionBtn, updateOptionBtn, deleteOptionBtn);
        oForm.add(oButtons, 1, 2);

        this.modelAnswerLabel = new Label("Correct: (not set)");
        HBox correctRow = new HBox(10, modelAnswerLabel, setCorrectBtn);
        correctRow.setPadding(new Insets(10));

        VBox right = new VBox(10);
        right.setPadding(new Insets(10));
        right.getChildren().addAll(new Label("Options"), optionsTable, oForm, correctRow);

        SplitPane split = new SplitPane();
        BorderPane left = new BorderPane();
        left.setCenter(questionsTable);
        left.setBottom(qForm);
        split.getItems().add(left);
        split.getItems().add(right);
        split.setDividerPositions(0.6);

        this.questionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                optionItems.clear();
                modelAnswerLabel.setText("Correct: (not set)");
                return;
            }
            courseIdField.setText(String.valueOf(newV.getCourseId()));
            if (newV.getQuestionType() == null) {
                typeBox.getSelectionModel().select(0);
            } else {
                typeBox.getSelectionModel().select(newV.getQuestionType());
            }
            pointsField.setText(newV.getPoints() == null ? "" : String.valueOf(newV.getPoints()));
            questionTextField.setText(newV.getQuestionText());
            refreshOptions();
        });

        this.optionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                optionTextField.setText("");
                optionOrderField.setText("");
                return;
            }
            optionTextField.setText(newV.getOptionText());
            optionOrderField.setText(newV.getOptionOrder() == null ? "" : String.valueOf(newV.getOptionOrder()));
        });

        root.setTop(filters);
        root.setCenter(split);

        refreshQuestions();
    }

    public Node getNode() {
        return root;
    }

    private void refreshQuestions() {
        Integer courseId = RowUtils.toNullableInt(filterCourseIdField.getText());
        String type = RowUtils.toNullableString(filterTypeBox.getValue());
        FxUtils.runAsync(
                () -> context.questions().selectQuestions(null, courseId, type),
                this::setQuestionsRows,
                ex -> FxUtils.showError("Load questions failed", ex.getMessage())
        );
    }

    private void setQuestionsRows(List<Map<String, Object>> rows) {
        List<QuestionRow> list = new ArrayList<QuestionRow>();
        for (Map<String, Object> row : rows) {
            int id = RowUtils.toInt(RowUtils.getIgnoreCase(row, "QuestionID"));
            int courseId = RowUtils.toInt(RowUtils.getIgnoreCase(row, "CourseID"));
            String text = RowUtils.toNullableString(RowUtils.getIgnoreCase(row, "QuestionText"));
            String type = RowUtils.toNullableString(RowUtils.getIgnoreCase(row, "QuestionType"));
            Integer points = RowUtils.toNullableInt(RowUtils.getIgnoreCase(row, "Points"));
            list.add(new QuestionRow(id, courseId, text, type, points));
        }
        questionItems.setAll(list);
    }

    private void refreshOptions() {
        QuestionRow q = questionsTable.getSelectionModel().getSelectedItem();
        if (q == null) {
            optionItems.clear();
            modelAnswerLabel.setText("Correct: (not set)");
            return;
        }
        int questionId = q.getQuestionId();
        FxUtils.runAsync(
                () -> context.options().selectOptions(null, questionId),
                rows -> {
                    setOptionsRows(rows);
                    refreshModelAnswer(questionId);
                },
                ex -> FxUtils.showError("Load options failed", ex.getMessage())
        );
    }

    private void setOptionsRows(List<Map<String, Object>> rows) {
        List<OptionRow> list = new ArrayList<OptionRow>();
        for (Map<String, Object> row : rows) {
            int optionId = RowUtils.toInt(RowUtils.getIgnoreCase(row, "OptionID"));
            int questionId = RowUtils.toInt(RowUtils.getIgnoreCase(row, "QuestionID"));
            String text = RowUtils.toNullableString(RowUtils.getIgnoreCase(row, "OptionText"));
            Integer order = RowUtils.toNullableInt(RowUtils.getIgnoreCase(row, "OptionOrder"));
            list.add(new OptionRow(optionId, questionId, text, order));
        }
        optionItems.setAll(list);
    }

    private void refreshModelAnswer(int questionId) {
        FxUtils.runAsync(
                () -> context.modelAnswers().selectModelAnswer(null, questionId),
                rows -> setModelAnswerLabel(rows),
                ex -> FxUtils.showError("Load model answer failed", ex.getMessage())
        );
    }

    private void setModelAnswerLabel(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            modelAnswerLabel.setText("Correct: (not set)");
            return;
        }
        Map<String, Object> row = rows.get(0);
        Integer optionId = RowUtils.toNullableInt(RowUtils.getIgnoreCase(row, "OptionID"));
        String optionText = RowUtils.toNullableString(RowUtils.getIgnoreCase(row, "OptionText"));
        modelAnswerLabel.setText("Correct: " + (optionId == null ? "" : optionId) + " - " + (optionText == null ? "" : optionText));
    }

    private void addQuestion() {
        Integer courseId = RowUtils.toNullableInt(courseIdField.getText());
        String type = RowUtils.toNullableString(typeBox.getValue());
        Integer points = RowUtils.toNullableInt(pointsField.getText());
        String text = RowUtils.toNullableString(questionTextField.getText());

        if (courseId == null || type == null || text == null) {
            FxUtils.showError("Validation", "CourseID, Type and Text are required.");
            return;
        }

        FxUtils.runAsync(
                () -> {
                    context.questions().insertQuestion(courseId, text, type, points);
                    return null;
                },
                ignored -> refreshQuestions(),
                ex -> FxUtils.showError("Insert failed", ex.getMessage())
        );
    }

    private void updateQuestion() {
        QuestionRow selected = questionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxUtils.showError("Validation", "Select a question to update.");
            return;
        }

        Integer points = RowUtils.toNullableInt(pointsField.getText());
        String text = RowUtils.toNullableString(questionTextField.getText());
        if (text == null) {
            FxUtils.showError("Validation", "Text is required.");
            return;
        }

        FxUtils.runAsync(
                () -> {
                    context.questions().updateQuestion(selected.getQuestionId(), text, points == null ? 0 : points);
                    return null;
                },
                ignored -> refreshQuestions(),
                ex -> FxUtils.showError("Update failed", ex.getMessage())
        );
    }

    private void deleteQuestion() {
        QuestionRow selected = questionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxUtils.showError("Validation", "Select a question to delete.");
            return;
        }

        FxUtils.runAsync(
                () -> {
                    context.questions().deleteQuestion(selected.getQuestionId());
                    return null;
                },
                ignored -> refreshQuestions(),
                ex -> FxUtils.showError("Delete failed", ex.getMessage())
        );
    }

    private void addOption() {
        QuestionRow q = questionsTable.getSelectionModel().getSelectedItem();
        if (q == null) {
            FxUtils.showError("Validation", "Select a question first.");
            return;
        }
        String text = RowUtils.toNullableString(optionTextField.getText());
        Integer order = RowUtils.toNullableInt(optionOrderField.getText());
        if (text == null) {
            FxUtils.showError("Validation", "Option text is required.");
            return;
        }

        FxUtils.runAsync(
                () -> {
                    context.options().insertOption(q.getQuestionId(), text, order);
                    return null;
                },
                ignored -> refreshOptions(),
                ex -> FxUtils.showError("Insert option failed", ex.getMessage())
        );
    }

    private void updateOption() {
        OptionRow selected = optionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxUtils.showError("Validation", "Select an option to update.");
            return;
        }
        String text = RowUtils.toNullableString(optionTextField.getText());
        Integer order = RowUtils.toNullableInt(optionOrderField.getText());
        if (text == null) {
            FxUtils.showError("Validation", "Option text is required.");
            return;
        }

        FxUtils.runAsync(
                () -> {
                    context.options().updateOption(selected.getOptionId(), text, order);
                    return null;
                },
                ignored -> refreshOptions(),
                ex -> FxUtils.showError("Update option failed", ex.getMessage())
        );
    }

    private void deleteOption() {
        OptionRow selected = optionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxUtils.showError("Validation", "Select an option to delete.");
            return;
        }

        FxUtils.runAsync(
                () -> {
                    context.options().deleteOption(selected.getOptionId());
                    return null;
                },
                ignored -> refreshOptions(),
                ex -> FxUtils.showError("Delete option failed", ex.getMessage())
        );
    }

    private void setCorrectOption() {
        QuestionRow q = questionsTable.getSelectionModel().getSelectedItem();
        OptionRow selected = optionsTable.getSelectionModel().getSelectedItem();
        if (q == null || selected == null) {
            FxUtils.showError("Validation", "Select a question and an option.");
            return;
        }

        FxUtils.runAsync(
                () -> {
                    context.modelAnswers().setModelAnswer(q.getQuestionId(), selected.getOptionId());
                    return null;
                },
                ignored -> refreshModelAnswer(q.getQuestionId()),
                ex -> FxUtils.showError("Set model answer failed", ex.getMessage())
        );
    }

    public static final class QuestionRow {
        private final int questionId;
        private final int courseId;
        private final String questionText;
        private final String questionType;
        private final Integer points;

        public QuestionRow(int questionId, int courseId, String questionText, String questionType, Integer points) {
            this.questionId = questionId;
            this.courseId = courseId;
            this.questionText = questionText;
            this.questionType = questionType;
            this.points = points;
        }

        public int getQuestionId() {
            return questionId;
        }

        public int getCourseId() {
            return courseId;
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

    public static final class OptionRow {
        private final int optionId;
        private final int questionId;
        private final String optionText;
        private final Integer optionOrder;

        public OptionRow(int optionId, int questionId, String optionText, Integer optionOrder) {
            this.optionId = optionId;
            this.questionId = questionId;
            this.optionText = optionText;
            this.optionOrder = optionOrder;
        }

        public int getOptionId() {
            return optionId;
        }

        public int getQuestionId() {
            return questionId;
        }

        public String getOptionText() {
            return optionText;
        }

        public Integer getOptionOrder() {
            return optionOrder;
        }
    }
}

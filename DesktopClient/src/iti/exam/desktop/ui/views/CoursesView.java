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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

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
        this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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

        this.table.getColumns().addAll(idCol, nameCol, minCol, maxCol);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refresh());
        HBox topBar = new HBox(10, refreshBtn);
        topBar.setPadding(new Insets(10));

        root.setTop(topBar);
        root.setCenter(table);

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

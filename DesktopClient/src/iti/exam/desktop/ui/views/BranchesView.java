package iti.exam.desktop.ui.views;

import iti.exam.desktop.models.Branch;
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

import java.sql.SQLException;
import java.util.List;

public final class BranchesView {
    private final AppSession session;
    private final AppContext context;
    private final BorderPane root;
    private final TableView<BranchRow> table;
    private final ObservableList<BranchRow> items;

    private final TextField branchNameField;
    private final TextField locationField;

    public BranchesView(AppSession session) {
        this.session = session;
        this.context = session.getContext();
        this.root = new BorderPane();
        this.items = FXCollections.observableArrayList();

        this.table = new TableView<BranchRow>(items);
        this.table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<BranchRow, Integer> idCol = new TableColumn<BranchRow, Integer>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<BranchRow, Integer>("branchId"));
        idCol.setMaxWidth(110);

        TableColumn<BranchRow, String> nameCol = new TableColumn<BranchRow, String>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<BranchRow, String>("branchName"));

        TableColumn<BranchRow, String> locCol = new TableColumn<BranchRow, String>("Location");
        locCol.setCellValueFactory(new PropertyValueFactory<BranchRow, String>("location"));

        this.table.getColumns().addAll(idCol, nameCol, locCol);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refresh());

        HBox topBar = new HBox(10, refreshBtn);
        topBar.setPadding(new Insets(10));

        this.branchNameField = new TextField();
        this.locationField = new TextField();

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.setPadding(new Insets(10));

        form.add(new Label("Branch Name"), 0, 0);
        form.add(branchNameField, 1, 0);
        form.add(new Label("Location"), 0, 1);
        form.add(locationField, 1, 1);

        Button addBtn = new Button("Add");
        Button updateBtn = new Button("Update");
        Button deleteBtn = new Button("Delete");

        boolean canWrite = session.getRole().canManageMasterData();
        addBtn.setDisable(!canWrite);
        updateBtn.setDisable(!canWrite);
        deleteBtn.setDisable(!canWrite);

        addBtn.setOnAction(e -> addBranch());
        updateBtn.setOnAction(e -> updateBranch());
        deleteBtn.setOnAction(e -> deleteBranch());

        HBox buttons = new HBox(10, addBtn, updateBtn, deleteBtn);
        form.add(buttons, 1, 2);

        GridPane.setHgrow(branchNameField, Priority.ALWAYS);
        GridPane.setHgrow(locationField, Priority.ALWAYS);

        this.table.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                return;
            }
            branchNameField.setText(newV.getBranchName());
            locationField.setText(newV.getLocation());
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
                () -> context.branches().selectBranches(null),
                this::setRows,
                ex -> FxUtils.showError("Load branches failed", ex.getMessage())
        );
    }

    private void setRows(List<Branch> branches) {
        items.clear();
        for (Branch b : branches) {
            items.add(new BranchRow(b.getBranchId(), b.getBranchName(), b.getLocation()));
        }
    }

    private void addBranch() {
        String name = trimOrNull(branchNameField.getText());
        String loc = trimOrNull(locationField.getText());
        if (name == null) {
            FxUtils.showError("Validation", "Branch name is required.");
            return;
        }

        FxUtils.runAsync(
                () -> {
                    int newId = context.branches().insertBranch(name, loc);
                    return newId;
                },
                newId -> refresh(),
                ex -> FxUtils.showError("Insert failed", ex.getMessage())
        );
    }

    private void updateBranch() {
        BranchRow selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxUtils.showError("Validation", "Select a branch to update.");
            return;
        }
        String name = trimOrNull(branchNameField.getText());
        String loc = trimOrNull(locationField.getText());
        if (name == null) {
            FxUtils.showError("Validation", "Branch name is required.");
            return;
        }

        FxUtils.runAsync(
                () -> {
                    context.branches().updateBranch(selected.getBranchId(), name, loc);
                    return null;
                },
                ignored -> refresh(),
                ex -> FxUtils.showError("Update failed", ex.getMessage())
        );
    }

    private void deleteBranch() {
        BranchRow selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxUtils.showError("Validation", "Select a branch to delete.");
            return;
        }

        FxUtils.runAsync(
                () -> {
                    context.branches().deleteBranch(selected.getBranchId());
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

    public static final class BranchRow {
        private final int branchId;
        private final String branchName;
        private final String location;

        public BranchRow(int branchId, String branchName, String location) {
            this.branchId = branchId;
            this.branchName = branchName;
            this.location = location;
        }

        public int getBranchId() {
            return branchId;
        }

        public String getBranchName() {
            return branchName;
        }

        public String getLocation() {
            return location;
        }
    }
}

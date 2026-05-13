package iti.exam.desktop.ui.views;

import iti.exam.desktop.models.Track;
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

public final class TracksView {
    private final AppContext context;
    private final BorderPane root;
    private final TableView<TrackRow> table;
    private final ObservableList<TrackRow> items;

    public TracksView(AppSession session) {
        this.context = session.getContext();
        this.root = new BorderPane();
        this.items = FXCollections.observableArrayList();

        this.table = new TableView<TrackRow>(items);
        this.table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<TrackRow, Integer> idCol = new TableColumn<TrackRow, Integer>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<TrackRow, Integer>("trackId"));
        idCol.setMaxWidth(110);

        TableColumn<TrackRow, String> nameCol = new TableColumn<TrackRow, String>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<TrackRow, String>("trackName"));

        TableColumn<TrackRow, Integer> durationCol = new TableColumn<TrackRow, Integer>("Months");
        durationCol.setCellValueFactory(new PropertyValueFactory<TrackRow, Integer>("durationMonths"));
        durationCol.setMaxWidth(140);

        TableColumn<TrackRow, String> branchCol = new TableColumn<TrackRow, String>("Branch");
        branchCol.setCellValueFactory(new PropertyValueFactory<TrackRow, String>("branchName"));

        this.table.getColumns().add(idCol);
        this.table.getColumns().add(nameCol);
        this.table.getColumns().add(durationCol);
        this.table.getColumns().add(branchCol);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refresh());
        HBox topBar = new HBox(10, refreshBtn);
        topBar.setPadding(new Insets(10));

        TextField nameField = new TextField();
        TextField branchIdField = new TextField();
        TextField monthsField = new TextField();

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.setPadding(new Insets(10));

        form.add(new Label("Track Name"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(new Label("Branch ID"), 0, 1);
        form.add(branchIdField, 1, 1);
        form.add(new Label("Duration (Months)"), 0, 2);
        form.add(monthsField, 1, 2);

        Button addBtn = new Button("Add");
        Button updateBtn = new Button("Update");
        Button deleteBtn = new Button("Delete");

        boolean canWrite = session.getRole().canManageMasterData();
        addBtn.setDisable(!canWrite);
        updateBtn.setDisable(!canWrite);
        deleteBtn.setDisable(!canWrite);

        addBtn.setOnAction(e -> addTrack(nameField, branchIdField, monthsField));
        updateBtn.setOnAction(e -> updateTrack(nameField, branchIdField, monthsField));
        deleteBtn.setOnAction(e -> deleteTrack());

        HBox buttons = new HBox(10, addBtn, updateBtn, deleteBtn);
        form.add(buttons, 1, 3);

        GridPane.setHgrow(nameField, Priority.ALWAYS);
        GridPane.setHgrow(branchIdField, Priority.ALWAYS);
        GridPane.setHgrow(monthsField, Priority.ALWAYS);

        this.table.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                return;
            }
            nameField.setText(newV.getTrackName());
            monthsField.setText(newV.getDurationMonths() == null ? "" : String.valueOf(newV.getDurationMonths()));
            branchIdField.setText(newV.getBranchId() == null ? "" : String.valueOf(newV.getBranchId()));
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
                () -> context.tracks().selectTracks(null, null),
                this::setRows,
                ex -> FxUtils.showError("Load tracks failed", ex.getMessage())
        );
    }

    private void setRows(List<Track> tracks) {
        items.clear();
        for (Track t : tracks) {
            items.add(new TrackRow(t.getTrackId(), t.getTrackName(), t.getDurationMonths(), t.getBranchId(), t.getBranchName()));
        }
    }

    private void addTrack(TextField nameField, TextField branchIdField, TextField monthsField) {
        String name = trimOrNull(nameField.getText());
        Integer branchId = parseIntOrNull(branchIdField.getText());
        Integer months = parseIntOrNull(monthsField.getText());

        if (name == null || branchId == null) {
            FxUtils.showError("Validation", "Track name and Branch ID are required.");
            return;
        }

        FxUtils.runAsync(
                () -> {
                    context.tracks().insertTrack(name, branchId, months);
                    return null;
                },
                ignored -> refresh(),
                ex -> FxUtils.showError("Insert failed", ex.getMessage())
        );
    }

    private void updateTrack(TextField nameField, TextField branchIdField, TextField monthsField) {
        TrackRow selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxUtils.showError("Validation", "Select a track to update.");
            return;
        }

        String name = trimOrNull(nameField.getText());
        Integer branchId = parseIntOrNull(branchIdField.getText());
        Integer months = parseIntOrNull(monthsField.getText());

        if (name == null || branchId == null) {
            FxUtils.showError("Validation", "Track name and Branch ID are required.");
            return;
        }

        FxUtils.runAsync(
                () -> {
                    context.tracks().updateTrack(selected.getTrackId(), name, branchId, months);
                    return null;
                },
                ignored -> refresh(),
                ex -> FxUtils.showError("Update failed", ex.getMessage())
        );
    }

    private void deleteTrack() {
        TrackRow selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxUtils.showError("Validation", "Select a track to delete.");
            return;
        }

        FxUtils.runAsync(
                () -> {
                    context.tracks().deleteTrack(selected.getTrackId());
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

    public static final class TrackRow {
        private final int trackId;
        private final String trackName;
        private final Integer durationMonths;
        private final Integer branchId;
        private final String branchName;

        public TrackRow(int trackId, String trackName, Integer durationMonths, Integer branchId, String branchName) {
            this.trackId = trackId;
            this.trackName = trackName;
            this.durationMonths = durationMonths;
            this.branchId = branchId;
            this.branchName = branchName;
        }

        public int getTrackId() {
            return trackId;
        }

        public String getTrackName() {
            return trackName;
        }

        public Integer getDurationMonths() {
            return durationMonths;
        }

        public Integer getBranchId() {
            return branchId;
        }

        public String getBranchName() {
            return branchName;
        }
    }
}

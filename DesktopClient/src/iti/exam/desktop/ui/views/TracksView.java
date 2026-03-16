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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

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
        this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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

        this.table.getColumns().addAll(idCol, nameCol, durationCol, branchCol);

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
                () -> context.tracks().selectTracks(null, null),
                this::setRows,
                ex -> FxUtils.showError("Load tracks failed", ex.getMessage())
        );
    }

    private void setRows(List<Track> tracks) {
        items.clear();
        for (Track t : tracks) {
            items.add(new TrackRow(t.getTrackId(), t.getTrackName(), t.getDurationMonths(), t.getBranchName()));
        }
    }

    public static final class TrackRow {
        private final int trackId;
        private final String trackName;
        private final Integer durationMonths;
        private final String branchName;

        public TrackRow(int trackId, String trackName, Integer durationMonths, String branchName) {
            this.trackId = trackId;
            this.trackName = trackName;
            this.durationMonths = durationMonths;
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

        public String getBranchName() {
            return branchName;
        }
    }
}

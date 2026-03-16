package iti.exam.desktop.ui.views;

import iti.exam.desktop.models.StoredProcedureInfo;
import iti.exam.desktop.models.StoredProcedureParam;
import iti.exam.desktop.ui.AppContext;
import iti.exam.desktop.ui.AppSession;
import iti.exam.desktop.ui.FxUtils;
import iti.exam.desktop.db.StoredProcedures;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ProceduresView {
    private final AppSession session;
    private final AppContext context;
    private final BorderPane root;
    private final ObservableList<StoredProcedureInfo> procedures;
    private final ObservableList<ParamRow> params;

    public ProceduresView(AppSession session) {
        this.session = session;
        this.context = session.getContext();
        this.root = new BorderPane();
        this.procedures = FXCollections.observableArrayList();
        this.params = FXCollections.observableArrayList();

        FilteredList<StoredProcedureInfo> filtered = new FilteredList<StoredProcedureInfo>(procedures, p -> true);
        ListView<StoredProcedureInfo> list = new ListView<StoredProcedureInfo>(filtered);
        list.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableView<ParamRow> table = new TableView<ParamRow>(params);

        TableColumn<ParamRow, Integer> idCol = new TableColumn<ParamRow, Integer>("Order");
        idCol.setCellValueFactory(new PropertyValueFactory<ParamRow, Integer>("parameterId"));
        idCol.setMaxWidth(120);

        TableColumn<ParamRow, String> nameCol = new TableColumn<ParamRow, String>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<ParamRow, String>("name"));

        TableColumn<ParamRow, String> typeCol = new TableColumn<ParamRow, String>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<ParamRow, String>("sqlTypeName"));
        typeCol.setMaxWidth(180);

        TableColumn<ParamRow, Boolean> outCol = new TableColumn<ParamRow, Boolean>("Out");
        outCol.setCellValueFactory(new PropertyValueFactory<ParamRow, Boolean>("output"));
        outCol.setMaxWidth(100);

        table.getColumns().add(idCol);
        table.getColumns().add(nameCol);
        table.getColumns().add(typeCol);
        table.getColumns().add(outCol);

        TextField filterField = new TextField();
        filterField.setPromptText("Filter procedures...");
        filterField.textProperty().addListener((obs, oldV, newV) -> {
            final String q = newV == null ? "" : newV.trim().toLowerCase(Locale.ROOT);
            filtered.setPredicate(p -> {
                if (q.isEmpty()) {
                    return true;
                }
                return p.getFullName().toLowerCase(Locale.ROOT).contains(q);
            });
        });

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refreshProcedures());

        HBox topBar = new HBox(10, refreshBtn, filterField);
        topBar.setPadding(new Insets(10));

        VBox left = new VBox(6, new Label("Procedures"), list);
        left.setPadding(new Insets(10));
        left.setPrefWidth(360);

        VBox center = new VBox(6, new Label("Parameters"), table);
        center.setPadding(new Insets(10));

        root.setTop(topBar);
        root.setLeft(left);
        root.setCenter(center);

        list.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                params.clear();
                return;
            }
            loadParams(newV);
        });

        refreshProcedures();
    }

    public Node getNode() {
        return root;
    }

    private void refreshProcedures() {
        FxUtils.runAsync(
                () -> {
                    if (session.getRole() == iti.exam.desktop.ui.AppRole.ADMIN) {
                        return context.catalog().listStoredProcedures();
                    }
                    return listKnownProcedures();
                },
                list -> {
                    procedures.setAll(list);
                    params.clear();
                },
                ex -> FxUtils.showError("Load procedures failed", ex.getMessage())
        );
    }

    private static List<StoredProcedureInfo> listKnownProcedures() {
        List<StoredProcedureInfo> result = new ArrayList<StoredProcedureInfo>();
        Field[] fields = StoredProcedures.class.getDeclaredFields();
        for (Field f : fields) {
            int mods = f.getModifiers();
            if (!Modifier.isStatic(mods) || !Modifier.isFinal(mods) || f.getType() != String.class) {
                continue;
            }
            try {
                Object v = f.get(null);
                if (v == null) {
                    continue;
                }
                String name = String.valueOf(v).trim();
                if (name.isEmpty()) {
                    continue;
                }
                result.add(new StoredProcedureInfo("dbo", name));
            } catch (IllegalAccessException ignored) {
            }
        }
        result.sort((a, b) -> a.getFullName().compareToIgnoreCase(b.getFullName()));
        return result;
    }

    private void loadParams(StoredProcedureInfo info) {
        FxUtils.runAsync(
                () -> context.catalog().getProcedureParams(info.getSchemaName(), info.getProcedureName()),
                list -> setParamRows(list),
                ex -> FxUtils.showError("Load parameters failed", ex.getMessage())
        );
    }

    private void setParamRows(List<StoredProcedureParam> list) {
        params.clear();
        for (StoredProcedureParam p : list) {
            params.add(new ParamRow(p.getParameterId(), p.getName(), p.isOutput(), p.getSqlTypeName()));
        }
    }

    public static final class ParamRow {
        private final int parameterId;
        private final String name;
        private final boolean output;
        private final String sqlTypeName;

        public ParamRow(int parameterId, String name, boolean output, String sqlTypeName) {
            this.parameterId = parameterId;
            this.name = name;
            this.output = output;
            this.sqlTypeName = sqlTypeName;
        }

        public int getParameterId() {
            return parameterId;
        }

        public String getName() {
            return name;
        }

        public boolean isOutput() {
            return output;
        }

        public String getSqlTypeName() {
            return sqlTypeName;
        }
    }
}

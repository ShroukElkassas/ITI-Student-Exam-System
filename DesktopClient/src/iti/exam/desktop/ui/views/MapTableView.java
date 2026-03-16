package iti.exam.desktop.ui.views;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MapTableView {
    private final TableView<Map<String, Object>> table;
    private final ObservableList<Map<String, Object>> items;

    public MapTableView() {
        this.items = FXCollections.observableArrayList();
        this.table = new TableView<Map<String, Object>>(items);
    }

    public Node getNode() {
        return table;
    }

    public void setRows(List<Map<String, Object>> rows) {
        if (rows == null) {
            rows = new ArrayList<Map<String, Object>>();
        }

        Set<String> columns = new LinkedHashSet<String>();
        for (Map<String, Object> row : rows) {
            if (row == null) {
                continue;
            }
            columns.addAll(row.keySet());
        }

        table.getColumns().clear();
        for (String key : columns) {
            final String colKey = key;
            TableColumn<Map<String, Object>, String> col = new TableColumn<Map<String, Object>, String>(colKey);
            col.setCellValueFactory(cell -> {
                Map<String, Object> r = cell.getValue();
                Object v = r == null ? null : r.get(colKey);
                return new SimpleStringProperty(v == null ? "" : String.valueOf(v));
            });
            table.getColumns().add(col);
        }

        items.setAll(rows);
    }
}


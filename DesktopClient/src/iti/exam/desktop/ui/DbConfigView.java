package iti.exam.desktop.ui;

import iti.exam.desktop.db.DbConfig;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public final class DbConfigView {
    private final HBox root;

    public DbConfigView(AppContext context) {
        DbConfig config = context.getDbConfig();
        this.root = new HBox(8);
        this.root.setPadding(new Insets(0, 0, 0, 12));
        this.root.getChildren().addAll(
                new Label("DB: " + config.getDatabase()),
                new Label("Host: " + config.getHost() + ":" + config.getPort()),
                new Label("User: " + config.getUser())
        );
    }

    public Node getNode() {
        return root;
    }
}


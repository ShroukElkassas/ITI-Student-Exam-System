package iti.exam.desktop.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public final class FxApp extends Application {
    private DbConfigView dbConfigView;
    private AppSession session;
    private Stage stage;

    @Override
    public void init() {
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        AppContext baseContext = new AppContext();
        this.session = showLogin(baseContext.getDbConfig());
        if (this.session == null) {
            this.stage.close();
            return;
        }

        showMain(this.session);
        this.stage.show();
    }

    private AppSession showLogin(iti.exam.desktop.db.DbConfig baseConfig) {
        LoginView loginView = new LoginView(stage, baseConfig);
        return loginView.showAndWait();
    }

    private void showMain(AppSession session) {
        BorderPane root = new BorderPane();

        HBox header = new HBox(12);
        header.setPadding(new Insets(10));
        Label title = new Label("ITI Student Exam System");
        this.dbConfigView = new DbConfigView(session.getContext());
        Label roleLabel = new Label("Role: " + session.getRole().getDisplayName());
        Button logoutBtn = new Button("Logout");
        logoutBtn.setOnAction(e -> logout());
        header.getChildren().addAll(title, dbConfigView.getNode(), roleLabel, logoutBtn);

        TabPane tabs = new TabPane();
        tabs.getTabs().add(wrap("Branches", new iti.exam.desktop.ui.views.BranchesView(session).getNode()));
        tabs.getTabs().add(wrap("Tracks", new iti.exam.desktop.ui.views.TracksView(session).getNode()));
        tabs.getTabs().add(wrap("Courses", new iti.exam.desktop.ui.views.CoursesView(session).getNode()));

        if (session.getRole() == iti.exam.desktop.ui.AppRole.ADMIN) {
            tabs.getTabs().add(wrap("Students", new iti.exam.desktop.ui.views.StudentsView(session).getNode()));
            tabs.getTabs().add(wrap("Instructors", new iti.exam.desktop.ui.views.InstructorsView(session).getNode()));
        }

        if (session.getRole().canManageQuestionsAndExams()) {
            tabs.getTabs().add(wrap("Question Bank", new iti.exam.desktop.ui.views.QuestionsView(session).getNode()));
            tabs.getTabs().add(wrap("Exam Composer", new iti.exam.desktop.ui.views.ExamComposerView(session).getNode()));
            tabs.getTabs().add(wrap("Exams", new iti.exam.desktop.ui.views.ExamsView(session).getNode()));
        }

        if (session.getRole() == iti.exam.desktop.ui.AppRole.STUDENT) {
            tabs.getTabs().add(wrap("Home", new iti.exam.desktop.ui.views.StudentHomeView(session).getNode()));
            tabs.getTabs().add(wrap("Take Exam", new iti.exam.desktop.ui.views.TakeExamView(session).getNode()));
            tabs.getTabs().add(wrap("My Grades", new iti.exam.desktop.ui.views.MyGradesView(session).getNode()));
        }

        tabs.getTabs().add(wrap("Reports", new iti.exam.desktop.ui.views.ReportsView(session).getNode()));

        if (session.getRole().canManageQuestionsAndExams()) {
            tabs.getTabs().add(wrap("Procedures", new iti.exam.desktop.ui.views.ProceduresView(session).getNode()));
        }

        root.setTop(header);
        root.setCenter(tabs);

        Scene scene = new Scene(root, 1100, 700);
        stage.setTitle("ITI Examination Desktop Client");
        stage.setScene(scene);
    }

    private void logout() {
        AppSession newSession = showLogin(session.getContext().getDbConfig());
        if (newSession == null) {
            stage.close();
            return;
        }
        this.session = newSession;
        showMain(this.session);
    }

    private static Tab wrap(String title, Node content) {
        Tab tab = new Tab(title, content);
        tab.setClosable(false);
        return tab;
    }
}

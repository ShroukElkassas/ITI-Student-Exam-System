package iti.exam.desktop.ui;

public enum AppRole {
    ADMIN("Admin", "ExamAdmin"),
    INSTRUCTOR("Instructor", "InstructorUser"),
    STUDENT("Student", "StudentUser");

    private final String displayName;
    private final String loginName;

    AppRole(String displayName, String loginName) {
        this.displayName = displayName;
        this.loginName = loginName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLoginName() {
        return loginName;
    }

    public boolean canManageMasterData() {
        return this == ADMIN;
    }

    public boolean canManageQuestionsAndExams() {
        return this == ADMIN || this == INSTRUCTOR;
    }

    @Override
    public String toString() {
        return displayName;
    }
}


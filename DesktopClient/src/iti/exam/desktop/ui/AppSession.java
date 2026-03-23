package iti.exam.desktop.ui;

public final class AppSession {
    private final AppRole role;
    private final AppContext context;
    private Integer studentId;

    public AppSession(AppRole role, AppContext context) {
        this.role = role;
        this.context = context;
    }

    public AppRole getRole() {
        return role;
    }

    public AppContext getContext() {
        return context;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }
}

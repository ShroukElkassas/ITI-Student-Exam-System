package iti.exam.desktop.ui;

import iti.exam.desktop.controllers.BranchController;
import iti.exam.desktop.controllers.CourseController;
import iti.exam.desktop.controllers.EnrollmentController;
import iti.exam.desktop.controllers.ExamController;
import iti.exam.desktop.controllers.InstructorController;
import iti.exam.desktop.controllers.ModelAnswerController;
import iti.exam.desktop.controllers.OptionController;
import iti.exam.desktop.controllers.ProcedureCatalogController;
import iti.exam.desktop.controllers.QuestionController;
import iti.exam.desktop.controllers.ReportController;
import iti.exam.desktop.controllers.StudentController;
import iti.exam.desktop.controllers.TrackController;
import iti.exam.desktop.db.DbConfig;
import iti.exam.desktop.db.DbConfigLoader;
import iti.exam.desktop.db.DbConnectionFactory;
import iti.exam.desktop.db.StoredProcExecutor;

public final class AppContext {
    private final DbConfig dbConfig;
    private final DbConnectionFactory connectionFactory;
    private final StoredProcExecutor procExecutor;

    private final BranchController branchController;
    private final TrackController trackController;
    private final CourseController courseController;
    private final InstructorController instructorController;
    private final StudentController studentController;
    private final QuestionController questionController;
    private final OptionController optionController;
    private final ModelAnswerController modelAnswerController;
    private final EnrollmentController enrollmentController;
    private final ExamController examController;
    private final ReportController reportController;
    private final ProcedureCatalogController procedureCatalogController;

    public AppContext() {
        this(DbConfigLoader.fromEnvironment());
    }

    public AppContext(DbConfig dbConfig) {
        this.dbConfig = dbConfig;
        this.connectionFactory = new DbConnectionFactory(dbConfig);
        this.procExecutor = new StoredProcExecutor(connectionFactory);

        this.branchController = new BranchController(procExecutor);
        this.trackController = new TrackController(procExecutor);
        this.courseController = new CourseController(procExecutor);
        this.instructorController = new InstructorController(procExecutor);
        this.studentController = new StudentController(procExecutor);
        this.questionController = new QuestionController(procExecutor);
        this.optionController = new OptionController(procExecutor);
        this.modelAnswerController = new ModelAnswerController(procExecutor);
        this.enrollmentController = new EnrollmentController(procExecutor);
        this.examController = new ExamController(procExecutor);
        this.reportController = new ReportController(procExecutor);
        this.procedureCatalogController = new ProcedureCatalogController(connectionFactory, procExecutor);
    }

    public DbConfig getDbConfig() {
        return dbConfig;
    }

    public DbConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public StoredProcExecutor getProcExecutor() {
        return procExecutor;
    }

    public BranchController branches() {
        return branchController;
    }

    public TrackController tracks() {
        return trackController;
    }

    public CourseController courses() {
        return courseController;
    }

    public InstructorController instructors() {
        return instructorController;
    }

    public StudentController students() {
        return studentController;
    }

    public QuestionController questions() {
        return questionController;
    }

    public OptionController options() {
        return optionController;
    }

    public ModelAnswerController modelAnswers() {
        return modelAnswerController;
    }

    public EnrollmentController enrollment() {
        return enrollmentController;
    }

    public ExamController exams() {
        return examController;
    }

    public ReportController reports() {
        return reportController;
    }

    public ProcedureCatalogController catalog() {
        return procedureCatalogController;
    }
}


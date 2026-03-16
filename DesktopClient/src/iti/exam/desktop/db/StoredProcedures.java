package iti.exam.desktop.db;

public final class StoredProcedures {
    private StoredProcedures() {
    }

    public static final String INSERT_BRANCH = "InsertBranch";
    public static final String UPDATE_BRANCH = "UpdateBranch";
    public static final String DELETE_BRANCH = "DeleteBranch";
    public static final String SELECT_BRANCH = "SelectBranch";

    public static final String INSERT_TRACK = "InsertTrack";
    public static final String UPDATE_TRACK = "UpdateTrack";
    public static final String DELETE_TRACK = "DeleteTrack";
    public static final String SELECT_TRACK_BY_BRANCH = "SelectTrackByBranch";

    public static final String INSERT_COURSE = "InsertCourse";
    public static final String UPDATE_COURSE = "UpdateCourse";
    public static final String DELETE_COURSE = "DeleteCourse";
    public static final String SELECT_COURSE_BY_TRACK = "SelectCourseByTrack";

    public static final String GENERATE_EXAM = "GenerateExam";
    public static final String SUBMIT_EXAM_ANSWERS = "SubmitExamAnswers";
    public static final String CORRECT_EXAM = "CorrectExam";

    public static final String INSERT_INSTRUCTOR = "InsertInstructor";
    public static final String UPDATE_INSTRUCTOR = "UpdateInstructor";
    public static final String DELETE_INSTRUCTOR = "DeleteInstructor";
    public static final String SELECT_INSTRUCTOR = "SelectInstructor";
    public static final String ASSIGN_INSTRUCTOR_TO_COURSE = "AssignInstructorToCourse";
    public static final String INSERT_INSTRUCTOR_COURSE = "InsertInstructorCourse";
    public static final String UPDATE_INSTRUCTOR_COURSE = "UpdateInstructorCourse";
    public static final String DELETE_INSTRUCTOR_COURSE = "DeleteInstructorCourse";
    public static final String SELECT_INSTRUCTOR_COURSE = "SelectInstructorCourse";

    public static final String INSERT_QUESTION = "InsertQuestion";
    public static final String UPDATE_QUESTION = "UpdateQuestion";
    public static final String DELETE_QUESTION = "DeleteQuestion";
    public static final String SELECT_QUESTION = "SelectQuestion";

    public static final String INSERT_OPTION = "InsertOption";
    public static final String UPDATE_OPTION = "UpdateOption";
    public static final String DELETE_OPTION = "DeleteOption";
    public static final String SELECT_OPTION = "SelectOption";

    public static final String INSERT_MODEL_ANSWER = "InsertModelAnswer";
    public static final String UPDATE_MODEL_ANSWER = "UpdateModelAnswer";
    public static final String DELETE_MODEL_ANSWER = "DeleteModelAnswer";
    public static final String SELECT_MODEL_ANSWER = "SelectModelAnswer";
    public static final String SET_MODEL_ANSWER = "SetModelAnswer";

    public static final String INSERT_STUDENT = "InsertStudent";
    public static final String UPDATE_STUDENT = "UpdateStudent";
    public static final String DELETE_STUDENT = "DeleteStudent";
    public static final String SELECT_STUDENT = "SelectStudent";
    public static final String SELECT_STUDENT_WITH_TRACK = "SelectStudentWithTrack";
    public static final String ASSIGN_STUDENT_TO_TRACK = "AssignStudentToTrack";

    public static final String INSERT_STUDENT_TRACK = "InsertStudentTrack";
    public static final String UPDATE_STUDENT_TRACK = "UpdateStudentTrack";
    public static final String DELETE_STUDENT_TRACK = "DeleteStudentTrack";
    public static final String SELECT_STUDENT_TRACK = "SelectStudentTrack";

    public static final String INSERT_TRACK_COURSE = "InsertTrackCourse";
    public static final String UPDATE_TRACK_COURSE = "UpdateTrackCourse";
    public static final String DELETE_TRACK_COURSE = "DeleteTrackCourse";
    public static final String SELECT_TRACK_COURSE = "SelectTrackCourse";

    public static final String INSERT_EXAM = "InsertExam";
    public static final String UPDATE_EXAM = "UpdateExam";
    public static final String DELETE_EXAM = "DeleteExam";
    public static final String SELECT_EXAM = "SelectExam";

    public static final String INSERT_EXAM_QUESTION = "InsertExamQuestion";
    public static final String UPDATE_EXAM_QUESTION = "UpdateExamQuestion";
    public static final String DELETE_EXAM_QUESTION = "DeleteExamQuestion";
    public static final String SELECT_EXAM_QUESTION = "SelectExamQuestion";

    public static final String INSERT_STUDENT_EXAM = "InsertStudentExam";
    public static final String UPDATE_STUDENT_EXAM = "UpdateStudentExam";
    public static final String DELETE_STUDENT_EXAM = "DeleteStudentExam";
    public static final String SELECT_STUDENT_EXAM = "SelectStudentExam";

    public static final String INSERT_STUDENT_ANSWER = "InsertStudentAnswer";
    public static final String UPDATE_STUDENT_ANSWER = "UpdateStudentAnswer";
    public static final String DELETE_STUDENT_ANSWER = "DeleteStudentAnswer";
    public static final String SELECT_STUDENT_ANSWER = "SelectStudentAnswer";

    public static final String REPORT_STUDENTS_BY_DEPARTMENT = "Report_StudentsByDepartment";
    public static final String REPORT_STUDENT_GRADES = "Report_StudentGrades";
    public static final String REPORT_INSTRUCTOR_COURSES = "Report_InstructorCourses";
}

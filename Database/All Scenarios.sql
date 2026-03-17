USE ITI_ExaminationDB;
GO

-- Scenario 1: GenerateExam — valid inputs
EXEC GenerateExam @CourseID = 1, @ExamName = 'SQL Final Exam', @QuestionsCount = 10;

-- Scenario 2: GenerateExam — not enough questions
EXEC GenerateExam @CourseID = 1, @ExamName = 'Stress Test Exam', @QuestionsCount = 100;

-- Scenario 3: SubmitExamAnswers — all questions answered
EXEC SubmitExamAnswers @StudentID = 1, @ExamID = 1, @AnswersJSON = '[{"Q":1,"A":10}, {"Q":2,"A":15}]'; 

-- Scenario 4: SubmitExamAnswers — one question skipped
EXEC SubmitExamAnswers @StudentID = 1, @ExamID = 1, @AnswersJSON = '[{"Q":1,"A":10}]';

-- Scenario 5: CorrectExam — all correct
EXEC CorrectExam @StudentID = 1, @ExamID = 1;

-- Scenario 6: CorrectExam — all wrong
EXEC CorrectExam @StudentID = 2, @ExamID = 1;

-- Scenario 7: Run all 3 reports
EXEC GetStudentGrades @StudentID = 1;
EXEC GetExamDetails @ExamID = 1, @StudentID = 1;
EXEC GetCourseStats @CourseID = 1;

-- Scenario 8: Delete Course with existing exams
--DELETE FROM Course WHERE CourseID = 1;
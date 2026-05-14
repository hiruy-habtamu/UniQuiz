package com.quizapp.server;

import com.quizapp.server.dao.AcademicYearDao;
import com.quizapp.server.dao.AnswerDao;
import com.quizapp.server.dao.BatchDao;
import com.quizapp.server.dao.ChoiceDao;
import com.quizapp.server.dao.EnrollmentDao;
import com.quizapp.server.dao.QuestionDao;
import com.quizapp.server.dao.QuizDao;
import com.quizapp.server.dao.SectionDao;
import com.quizapp.server.dao.SemesterDao;
import com.quizapp.server.dao.TeacherSectionDao;
import com.quizapp.server.dao.UserDao;
import com.quizapp.server.service.AcademicYearService;
import com.quizapp.server.service.AuthService;
import com.quizapp.server.service.BatchService;
import com.quizapp.server.service.EnrollmentService;
import com.quizapp.server.service.QuizService;
import com.quizapp.server.service.ScoringService;
import com.quizapp.server.service.SectionService;
import com.quizapp.server.service.SemesterService;
import com.quizapp.server.service.TeacherSectionService;
import com.quizapp.shared.message.quiz.CreateQuizMessage;
import com.quizapp.shared.model.AcademicYear;
import com.quizapp.shared.model.Answer;
import com.quizapp.shared.model.Batch;
import com.quizapp.shared.model.Choice;
import com.quizapp.shared.model.Enrollment;
import com.quizapp.shared.model.Question;
import com.quizapp.shared.model.Quiz;
import com.quizapp.shared.model.Section;
import com.quizapp.shared.model.Semester;
import com.quizapp.shared.model.TeacherSection;
import com.quizapp.shared.model.User;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ServerSmokeTest {
    public static void main(String[] args) throws Exception {
        testAcademicYearValidation();
        testAuthValidationAndRegistration();
        testAcademicServicesValidation();
        testQuizCreationAndSubmissionRules();
        testEnrollmentAndTeacherAssignments();
        testScoring();
        System.out.println("Server smoke tests passed");
    }

    private static void testAcademicYearValidation() throws Exception {
        FakeAcademicYearDao dao = new FakeAcademicYearDao();
        AcademicYearService service = new AcademicYearService(dao);

        AcademicYear year = new AcademicYear();
        year.setLabel("2025/26");
        year.setStartDate(LocalDate.of(2025, 9, 1));
        year.setEndDate(LocalDate.of(2026, 8, 31));
        year.setActive(true);
        service.createYear(year);
        assertTrue(dao.findActive().isPresent(), "Active academic year should be stored");

        expectIllegalArgument(() -> service.createYear(year), "duplicate academic year should fail");
    }

    private static void testAuthValidationAndRegistration() throws Exception {
        FakeBatchDao batchDao = new FakeBatchDao();
        Batch batch = new Batch();
        batch.setEntryYear(2024);
        batch.setProgram("BSCS");
        batch.setCreatedBy(1);
        batchDao.insert(batch);

        FakeUserDao userDao = new FakeUserDao();
        AuthService service = new AuthService(userDao, batchDao);

        expectIllegalArgument(() -> service.register("alice", "pass", "Alice", "TEACHER", 1),
                "teacher batch assignment should fail");
        expectIllegalArgument(() -> service.register("bob", "pass", "Bob", "STUDENT", null),
                "student without batch should fail");

        int teacherId = service.register("teacher1", "pass", "Teacher One", "TEACHER", null);
        Optional<User> teacher = service.authenticate("teacher1", "pass");
        assertTrue(teacher.isPresent() && teacher.get().getId() == teacherId, "Teacher login should succeed");

        int studentId = service.register("student1", "pass", "Student One", "STUDENT", batch.getId());
        assertTrue(userDao.findById(studentId).isPresent(), "Student registration should persist user");
    }

    private static void testAcademicServicesValidation() throws Exception {
        FakeUserDao userDao = new FakeUserDao();
        User teacher = teacher("teach");
        userDao.insert(teacher);
        User student = student("stud", 1);
        userDao.insert(student);

        FakeBatchDao batchDao = new FakeBatchDao();
        BatchService batchService = new BatchService(batchDao, userDao);

        Batch batch = new Batch();
        batch.setEntryYear(2023);
        batch.setProgram("BSIT");
        batch.setCreatedBy(teacher.getId());
        int batchId = batchService.createBatch(batch);
        assertTrue(batchId > 0, "Batch should be created");
        expectIllegalArgument(() -> batchService.createBatch(batch), "duplicate batch should fail");

        FakeAcademicYearDao yearDao = new FakeAcademicYearDao();
        AcademicYear year = new AcademicYear();
        year.setLabel("2024/25");
        year.setStartDate(LocalDate.of(2024, 9, 1));
        year.setEndDate(LocalDate.of(2025, 8, 31));
        year.setActive(true);
        yearDao.insert(year);

        FakeSemesterDao semesterDao = new FakeSemesterDao();
        SemesterService semesterService = new SemesterService(semesterDao, yearDao, userDao);

        Semester semester = new Semester();
        semester.setAcademicYearId(year.getId());
        semester.setName("FIRST");
        semester.setStartDate(LocalDate.of(2024, 9, 1));
        semester.setEndDate(LocalDate.of(2025, 1, 15));
        semester.setActive(true);
        semester.setCreatedBy(teacher.getId());
        int semesterId = semesterService.createSemester(semester);
        assertTrue(semesterId > 0, "Semester should be created");

        FakeSectionDao sectionDao = new FakeSectionDao();
        SectionService sectionService = new SectionService(sectionDao, batchDao, semesterDao);
        Section section = new Section();
        section.setName("A");
        section.setBatchId(batch.getId());
        section.setSemesterId(semesterId);
        int sectionId = sectionService.createSection(section);
        assertTrue(sectionId > 0, "Section should be created");
        expectIllegalArgument(() -> sectionService.createSection(section), "duplicate section should fail");
    }

    private static void testQuizCreationAndSubmissionRules() throws Exception {
        FakeUserDao userDao = new FakeUserDao();
        User teacher = teacher("creator");
        userDao.insert(teacher);
        User student = student("learner", 2);
        userDao.insert(student);

        FakeSemesterDao semesterDao = new FakeSemesterDao();
        Semester semester = new Semester();
        semester.setAcademicYearId(1);
        semester.setName("FIRST");
        semester.setStartDate(LocalDate.of(2024, 9, 1));
        semester.setEndDate(LocalDate.of(2025, 1, 15));
        semester.setActive(true);
        semester.setCreatedBy(teacher.getId());
        semesterDao.insert(semester);

        FakeQuizDao quizDao = new FakeQuizDao();
        FakeQuestionDao questionDao = new FakeQuestionDao();
        FakeChoiceDao choiceDao = new FakeChoiceDao();
        FakeAnswerDao answerDao = new FakeAnswerDao();
        QuizService quizService = new QuizService(quizDao, questionDao, choiceDao, answerDao, semesterDao, userDao);

        Quiz quiz = new Quiz();
        quiz.setTitle("Java Basics");
        quiz.setSemesterId(semester.getId());
        quiz.setCreatedBy(teacher.getId());
        quiz.setTimeLimitSecs(600);
        quiz.setPassingScore(60);

        List<CreateQuizMessage.ChoicePayload> choices = List.of(
                new CreateQuizMessage.ChoicePayload("A", true),
                new CreateQuizMessage.ChoicePayload("B", false)
        );
        List<CreateQuizMessage.QuestionPayload> questions = List.of(
                new CreateQuizMessage.QuestionPayload("What is Java?", 1, choices)
        );
        int quizId = quizService.createQuizWithQuestions(quiz, questions);
        assertTrue(quizId > 0, "Quiz should be created");
        assertEquals(1, questionDao.findByQuizId(quizId).size(), "Question should be stored");

        Answer answer = new Answer();
        answer.setStudentId(student.getId());
        answer.setQuizId(quizId);
        answer.setQuestionId(questionDao.findByQuizId(quizId).getFirst().getId());
        answer.setChoiceId(choiceDao.findByQuestionId(answer.getQuestionId()).getFirst().getId());
        answer.setViolationCount(0);
        answer.setForceSubmitted(false);
        quizService.submitAnswer(answer);

        expectIllegalArgument(() -> quizService.submitAnswer(answer), "duplicate answer should fail");
    }

    private static void testEnrollmentAndTeacherAssignments() throws Exception {
        FakeUserDao userDao = new FakeUserDao();
        User teacher = teacher("mentor");
        userDao.insert(teacher);
        User student = student("trainee", 10);
        userDao.insert(student);

        FakeSectionDao sectionDao = new FakeSectionDao();
        Section section = new Section();
        section.setName("B");
        section.setBatchId(10);
        section.setSemesterId(20);
        sectionDao.insert(section);

        EnrollmentService enrollmentService = new EnrollmentService(new FakeEnrollmentDao(), userDao, sectionDao);
        enrollmentService.enrollStudent(student.getId(), section.getId());
        expectIllegalArgument(() -> enrollmentService.enrollStudent(student.getId(), section.getId()),
                "duplicate enrollment should fail");

        TeacherSectionService teacherSectionService = new TeacherSectionService(new FakeTeacherSectionDao(), userDao, sectionDao);
        teacherSectionService.assignTeacher(teacher.getId(), section.getId());
        expectIllegalArgument(() -> teacherSectionService.assignTeacher(teacher.getId(), section.getId()),
                "duplicate teacher assignment should fail");
    }

    private static void testScoring() throws Exception {
        FakeQuestionDao questionDao = new FakeQuestionDao();
        FakeChoiceDao choiceDao = new FakeChoiceDao();
        FakeAnswerDao answerDao = new FakeAnswerDao();

        Question q1 = new Question();
        q1.setQuizId(1);
        q1.setBody("Q1");
        q1.setPosition(1);
        questionDao.insert(q1);

        Question q2 = new Question();
        q2.setQuizId(1);
        q2.setBody("Q2");
        q2.setPosition(2);
        questionDao.insert(q2);

        Choice c1 = new Choice();
        c1.setQuestionId(q1.getId());
        c1.setBody("Correct");
        c1.setCorrect(true);
        choiceDao.insert(c1);

        Choice c2 = new Choice();
        c2.setQuestionId(q2.getId());
        c2.setBody("Wrong");
        c2.setCorrect(false);
        choiceDao.insert(c2);

        Answer a1 = new Answer();
        a1.setStudentId(1);
        a1.setQuizId(1);
        a1.setQuestionId(q1.getId());
        a1.setChoiceId(c1.getId());
        answerDao.insert(a1);

        Answer a2 = new Answer();
        a2.setStudentId(1);
        a2.setQuizId(1);
        a2.setQuestionId(q2.getId());
        a2.setChoiceId(c2.getId());
        answerDao.insert(a2);

        ScoringService scoringService = new ScoringService(answerDao, choiceDao, questionDao);
        assertEquals(1, scoringService.calculateScore(1, 1), "Raw score should count only correct answers");
        assertEquals(50, scoringService.calculateScorePercentage(1, 1), "Percentage score should be derived from question count");
    }

    private static void expectIllegalArgument(ThrowingRunnable runnable, String message) throws Exception {
        try {
            runnable.run();
            throw new AssertionError(message);
        } catch (IllegalArgumentException expected) {
        }
    }

    private static void assertTrue(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + " expected=" + expected + " actual=" + actual);
        }
    }

    private static User teacher(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash("hash");
        user.setFullName(username);
        user.setRole("TEACHER");
        return user;
    }

    private static User student(String username, Integer batchId) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash("hash");
        user.setFullName(username);
        user.setRole("STUDENT");
        user.setBatchId(batchId);
        return user;
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static final class FakeAcademicYearDao extends AcademicYearDao {
        private final Map<Integer, AcademicYear> years = new HashMap<>();
        private int nextId = 1;

        @Override
        public Optional<AcademicYear> findByLabel(String label) {
            return years.values().stream().filter(year -> year.getLabel().equals(label)).findFirst();
        }

        @Override
        public Optional<AcademicYear> findActive() {
            return years.values().stream().filter(AcademicYear::isActive).findFirst();
        }

        @Override
        public List<AcademicYear> findAll() {
            return years.values().stream().sorted(Comparator.comparing(AcademicYear::getId)).toList();
        }

        @Override
        public int insert(AcademicYear academicYear) {
            academicYear.setId(nextId++);
            years.put(academicYear.getId(), academicYear);
            return academicYear.getId();
        }

        @Override
        public void deactivateAllExcept(String label) {
            years.values().forEach(year -> year.setActive(year.getLabel().equals(label)));
        }
    }

    private static class FakeUserDao extends UserDao {
        private final Map<Integer, User> users = new HashMap<>();
        private int nextId = 1;

        @Override
        public Optional<User> findById(int id) {
            return Optional.ofNullable(users.get(id));
        }

        @Override
        public Optional<User> findByUsername(String username) {
            return users.values().stream().filter(user -> user.getUsername().equals(username)).findFirst();
        }

        @Override
        public List<User> findAll() {
            return new ArrayList<>(users.values());
        }

        @Override
        public int insert(User user) {
            user.setId(nextId++);
            users.put(user.getId(), user);
            return user.getId();
        }
    }

    private static class FakeBatchDao extends BatchDao {
        private final Map<Integer, Batch> batches = new HashMap<>();
        private int nextId = 1;

        @Override
        public Optional<Batch> findById(int id) {
            return Optional.ofNullable(batches.get(id));
        }

        @Override
        public Optional<Batch> findByEntryYearAndProgram(int entryYear, String program) {
            return batches.values().stream()
                    .filter(batch -> batch.getEntryYear() == entryYear && batch.getProgram().equals(program))
                    .findFirst();
        }

        @Override
        public List<Batch> findAll() {
            return new ArrayList<>(batches.values());
        }

        @Override
        public int insert(Batch batch) {
            batch.setId(nextId++);
            batches.put(batch.getId(), batch);
            return batch.getId();
        }
    }

    private static class FakeSemesterDao extends SemesterDao {
        private final Map<Integer, Semester> semesters = new HashMap<>();
        private int nextId = 1;

        @Override
        public Optional<Semester> findById(int id) {
            return Optional.ofNullable(semesters.get(id));
        }

        @Override
        public Optional<Semester> findActive() {
            return semesters.values().stream().filter(Semester::isActive).findFirst();
        }

        @Override
        public List<Semester> findAll() {
            return new ArrayList<>(semesters.values());
        }

        @Override
        public List<Semester> findByAcademicYearId(int academicYearId) {
            return semesters.values().stream()
                    .filter(semester -> semester.getAcademicYearId() == academicYearId)
                    .toList();
        }

        @Override
        public int insert(Semester semester) {
            semester.setId(nextId++);
            semesters.put(semester.getId(), semester);
            return semester.getId();
        }

        @Override
        public void deactivateAllInAcademicYearExcept(int academicYearId, int semesterId) {
            semesters.values().stream()
                    .filter(semester -> semester.getAcademicYearId() == academicYearId)
                    .forEach(semester -> semester.setActive(semester.getId() == semesterId));
        }
    }

    private static class FakeSectionDao extends SectionDao {
        private final Map<Integer, Section> sections = new HashMap<>();
        private int nextId = 1;

        @Override
        public Optional<Section> findById(int id) {
            return Optional.ofNullable(sections.get(id));
        }

        @Override
        public List<Section> findAll() {
            return new ArrayList<>(sections.values());
        }

        @Override
        public List<Section> findBySemesterId(int semesterId) {
            return sections.values().stream().filter(section -> section.getSemesterId() == semesterId).toList();
        }

        @Override
        public Optional<Section> findByNameBatchAndSemester(String name, int batchId, int semesterId) {
            return sections.values().stream()
                    .filter(section -> section.getName().equals(name)
                            && section.getBatchId() == batchId
                            && section.getSemesterId() == semesterId)
                    .findFirst();
        }

        @Override
        public int insert(Section section) {
            section.setId(nextId++);
            sections.put(section.getId(), section);
            return section.getId();
        }
    }

    private static class FakeQuizDao extends QuizDao {
        private final Map<Integer, Quiz> quizzes = new HashMap<>();
        private int nextId = 1;

        @Override
        public Optional<Quiz> findById(int id) {
            return Optional.ofNullable(quizzes.get(id));
        }

        @Override
        public List<Quiz> findBySemesterId(int semesterId) {
            return quizzes.values().stream().filter(quiz -> quiz.getSemesterId() == semesterId).toList();
        }

        @Override
        public int insert(Quiz quiz) {
            quiz.setId(nextId++);
            quizzes.put(quiz.getId(), quiz);
            return quiz.getId();
        }

        @Override
        public void updateStatus(int quizId, String status) {
            quizzes.get(quizId).setStatus(status);
        }
    }

    private static class FakeQuestionDao extends QuestionDao {
        private final Map<Integer, Question> questions = new HashMap<>();
        private int nextId = 1;

        @Override
        public Optional<Question> findById(int id) {
            return Optional.ofNullable(questions.get(id));
        }

        @Override
        public List<Question> findByQuizId(int quizId) {
            return questions.values().stream()
                    .filter(question -> question.getQuizId() == quizId)
                    .sorted(Comparator.comparing(Question::getPosition))
                    .toList();
        }

        @Override
        public int insert(Question question) {
            question.setId(nextId++);
            questions.put(question.getId(), question);
            return question.getId();
        }
    }

    private static class FakeChoiceDao extends ChoiceDao {
        private final Map<Integer, Choice> choices = new HashMap<>();
        private int nextId = 1;

        @Override
        public Optional<Choice> findById(int id) {
            return Optional.ofNullable(choices.get(id));
        }

        @Override
        public List<Choice> findByQuestionId(int questionId) {
            return choices.values().stream().filter(choice -> choice.getQuestionId() == questionId).toList();
        }

        @Override
        public int insert(Choice choice) {
            choice.setId(nextId++);
            choices.put(choice.getId(), choice);
            return choice.getId();
        }
    }

    private static class FakeAnswerDao extends AnswerDao {
        private final Map<Integer, Answer> answers = new HashMap<>();
        private int nextId = 1;

        @Override
        public Optional<Answer> findById(int id) {
            return Optional.ofNullable(answers.get(id));
        }

        @Override
        public List<Answer> findByStudentAndQuiz(int studentId, int quizId) {
            return answers.values().stream()
                    .filter(answer -> answer.getStudentId() == studentId && answer.getQuizId() == quizId)
                    .toList();
        }

        @Override
        public Optional<Answer> findByStudentQuizAndQuestion(int studentId, int quizId, int questionId) {
            return answers.values().stream()
                    .filter(answer -> answer.getStudentId() == studentId
                            && answer.getQuizId() == quizId
                            && answer.getQuestionId() == questionId)
                    .findFirst();
        }

        @Override
        public List<Answer> findByQuizId(int quizId) {
            return answers.values().stream().filter(answer -> answer.getQuizId() == quizId).toList();
        }

        @Override
        public int insert(Answer answer) {
            answer.setId(nextId++);
            answers.put(answer.getId(), answer);
            return answer.getId();
        }
    }

    private static class FakeEnrollmentDao extends EnrollmentDao {
        private final List<Enrollment> enrollments = new ArrayList<>();

        @Override
        public Optional<Enrollment> findByStudentAndSection(int studentId, int sectionId) {
            return enrollments.stream()
                    .filter(enrollment -> enrollment.getStudentId() == studentId && enrollment.getSectionId() == sectionId)
                    .findFirst();
        }

        @Override
        public List<Enrollment> findByStudentId(int studentId) {
            return enrollments.stream().filter(enrollment -> enrollment.getStudentId() == studentId).toList();
        }

        @Override
        public List<Enrollment> findBySectionId(int sectionId) {
            return enrollments.stream().filter(enrollment -> enrollment.getSectionId() == sectionId).toList();
        }

        @Override
        public void insert(Enrollment enrollment) {
            enrollments.add(enrollment);
        }

        @Override
        public void delete(int studentId, int sectionId) {
            enrollments.removeIf(enrollment -> enrollment.getStudentId() == studentId && enrollment.getSectionId() == sectionId);
        }
    }

    private static class FakeTeacherSectionDao extends TeacherSectionDao {
        private final List<TeacherSection> assignments = new ArrayList<>();

        @Override
        public Optional<TeacherSection> findByTeacherAndSection(int teacherId, int sectionId) {
            return assignments.stream()
                    .filter(assignment -> assignment.getTeacherId() == teacherId && assignment.getSectionId() == sectionId)
                    .findFirst();
        }

        @Override
        public List<TeacherSection> findByTeacherId(int teacherId) {
            return assignments.stream().filter(assignment -> assignment.getTeacherId() == teacherId).toList();
        }

        @Override
        public List<TeacherSection> findBySectionId(int sectionId) {
            return assignments.stream().filter(assignment -> assignment.getSectionId() == sectionId).toList();
        }

        @Override
        public void insert(TeacherSection teacherSection) {
            assignments.add(teacherSection);
        }

        @Override
        public void delete(int teacherId, int sectionId) {
            assignments.removeIf(assignment -> assignment.getTeacherId() == teacherId && assignment.getSectionId() == sectionId);
        }
    }
}

package com.quizapp.server.network;

import com.quizapp.server.presence.OnlineRegistry;
import com.quizapp.server.service.AcademicYearService;
import com.quizapp.server.service.BatchService;
import com.quizapp.server.service.EnrollmentService;
import com.quizapp.server.service.AuthService;
import com.quizapp.server.service.QuizService;
import com.quizapp.server.service.SectionService;
import com.quizapp.server.service.SemesterService;
import com.quizapp.server.service.TeacherSectionService;
import com.quizapp.server.session.QuizSession;
import com.quizapp.server.session.SessionRegistry;
import com.quizapp.shared.message.CommonResponseMessage;
import com.quizapp.shared.message.Message;
import com.quizapp.shared.message.academic.ActionResponseMessage;
import com.quizapp.shared.message.academic.AssignTeacherSectionMessage;
import com.quizapp.shared.message.academic.AssignStudentSectionMessage;
import com.quizapp.shared.message.academic.CreateAcademicYearMessage;
import com.quizapp.shared.message.academic.CreateBatchMessage;
import com.quizapp.shared.message.academic.CreateSectionMessage;
import com.quizapp.shared.message.academic.CreateSemesterMessage;
import com.quizapp.shared.message.academic.EnrollStudentMessage;
import com.quizapp.shared.message.academic.EntityResponseMessage;
import com.quizapp.shared.message.academic.GetAllSectionsMessage;
import com.quizapp.shared.message.academic.GetAllSectionsResponseMessage;
import com.quizapp.shared.message.academic.GetAcademicYearsMessage;
import com.quizapp.shared.message.academic.GetAcademicYearsResponseMessage;
import com.quizapp.shared.message.academic.GetBatchesMessage;
import com.quizapp.shared.message.academic.GetBatchesResponseMessage;
import com.quizapp.shared.message.academic.GetSemestersMessage;
import com.quizapp.shared.message.academic.GetSemestersResponseMessage;
import com.quizapp.shared.message.academic.GetSectionsMessage;
import com.quizapp.shared.message.academic.GetSectionsResponseMessage;
import com.quizapp.shared.message.academic.SetSemesterActiveMessage;
import com.quizapp.shared.message.auth.LoginMessage;
import com.quizapp.shared.message.auth.LoginResponseMessage;
import com.quizapp.shared.message.auth.RegisterMessage;
import com.quizapp.shared.message.auth.RegisterResponseMessage;
import com.quizapp.shared.message.quiz.CloseQuizMessage;
import com.quizapp.shared.message.quiz.CreateQuizMessage;
import com.quizapp.shared.message.quiz.CreateQuizResponseMessage;
import com.quizapp.shared.message.quiz.GetActiveQuizzesMessage;
import com.quizapp.shared.message.quiz.GetActiveQuizzesResponseMessage;
import com.quizapp.shared.message.quiz.GetStudentActiveQuizzesMessage;
import com.quizapp.shared.message.quiz.GetStudentActiveQuizzesResponseMessage;
import com.quizapp.shared.message.quiz.GetTeacherQuizzesMessage;
import com.quizapp.shared.message.quiz.GetTeacherQuizzesResponseMessage;
import com.quizapp.shared.message.quiz.JoinQuizMessage;
import com.quizapp.shared.message.quiz.JoinQuizResponseMessage;
import com.quizapp.shared.message.quiz.StartQuizMessage;
import com.quizapp.shared.message.student.AnswerSubmissionMessage;
import com.quizapp.shared.message.student.AnswerSubmissionResponseMessage;
import com.quizapp.shared.model.Answer;
import com.quizapp.shared.model.User;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final AuthService authService;
    private final QuizService quizService;
    private final AcademicYearService academicYearService;
    private final BatchService batchService;
    private final SemesterService semesterService;
    private final SectionService sectionService;
    private final EnrollmentService enrollmentService;
    private final TeacherSectionService teacherSectionService;
    private final OnlineRegistry onlineRegistry;
    private final SessionRegistry sessionRegistry;

    private Integer authenticatedUserId;

    public ClientHandler(Socket socket, AuthService authService, QuizService quizService,
                         AcademicYearService academicYearService,
                         BatchService batchService, SemesterService semesterService,
                         SectionService sectionService, EnrollmentService enrollmentService,
                         TeacherSectionService teacherSectionService, OnlineRegistry onlineRegistry,
                         SessionRegistry sessionRegistry) {
        this.socket = socket;
        this.authService = authService;
        this.quizService = quizService;
        this.academicYearService = academicYearService;
        this.batchService = batchService;
        this.semesterService = semesterService;
        this.sectionService = sectionService;
        this.enrollmentService = enrollmentService;
        this.teacherSectionService = teacherSectionService;
        this.onlineRegistry = onlineRegistry;
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void run() {
        try (Socket clientSocket = socket;
             ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream())) {
            output.flush();

            while (!clientSocket.isClosed()) {
                Object payload = input.readObject();
                Message response = handle(payload);
                output.writeObject(response);
                output.flush();
            }
        } catch (EOFException ignored) {
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Client handler failed.", e);
        } finally {
            if (authenticatedUserId != null) {
                onlineRegistry.markOffline(authenticatedUserId);
                sessionRegistry.removeStudentSessions(authenticatedUserId);
            }
        }
    }

    private Message handle(Object payload) {
        try {
            if (payload instanceof LoginMessage message) {
                return handleLogin(message);
            }
            if (payload instanceof RegisterMessage message) {
                return handleRegister(message);
            }
            if (payload instanceof GetBatchesMessage) {
                return handleGetBatches();
            }
            if (payload instanceof GetSemestersMessage) {
                return handleGetSemesters();
            }
            if (payload instanceof GetAllSectionsMessage) {
                return handleGetAllSections();
            }
            if (payload instanceof GetAcademicYearsMessage) {
                return handleGetAcademicYears();
            }
            if (payload instanceof GetSectionsMessage message) {
                return handleGetSections(message);
            }
            if (payload instanceof CreateQuizMessage message) {
                return handleCreateQuiz(message);
            }
            if (payload instanceof CreateBatchMessage message) {
                return handleCreateBatch(message);
            }
            if (payload instanceof CreateAcademicYearMessage message) {
                return handleCreateAcademicYear(message);
            }
            if (payload instanceof CreateSemesterMessage message) {
                return handleCreateSemester(message);
            }
            if (payload instanceof CreateSectionMessage message) {
                return handleCreateSection(message);
            }
            if (payload instanceof SetSemesterActiveMessage message) {
                return handleSetSemesterActive(message);
            }
            if (payload instanceof EnrollStudentMessage message) {
                return handleEnrollStudent(message);
            }
            if (payload instanceof AssignStudentSectionMessage message) {
                return handleAssignStudentSection(message);
            }
            if (payload instanceof AssignTeacherSectionMessage message) {
                return handleAssignTeacher(message);
            }
            if (payload instanceof StartQuizMessage message) {
                return handleStartQuiz(message);
            }
            if (payload instanceof CloseQuizMessage message) {
                return handleCloseQuiz(message);
            }
            if (payload instanceof GetActiveQuizzesMessage) {
                return handleGetActiveQuizzes();
            }
            if (payload instanceof GetStudentActiveQuizzesMessage message) {
                return handleGetStudentActiveQuizzes(message);
            }
            if (payload instanceof GetTeacherQuizzesMessage message) {
                return handleGetTeacherQuizzes(message);
            }
            if (payload instanceof JoinQuizMessage message) {
                return handleJoinQuiz(message);
            }
            if (payload instanceof AnswerSubmissionMessage message) {
                return handleAnswerSubmission(message);
            }
            return new CommonResponseMessage(false, "Unsupported message type: " + payload.getClass().getSimpleName());
        } catch (SQLException | IllegalArgumentException e) {
            return new CommonResponseMessage(false, e.getMessage());
        }
    }

    private LoginResponseMessage handleLogin(LoginMessage message) throws SQLException {
        return authService.authenticate(message.getUsername(), message.getPassword())
                .map(this::onSuccessfulLogin)
                .orElseGet(() -> new LoginResponseMessage(false, "Invalid username or password.", null));
    }

    private RegisterResponseMessage handleRegister(RegisterMessage message) throws SQLException {
        authService.register(message.getUsername(), message.getPassword(), message.getFullName(),
                message.getRole(), message.getBatchId());
        return new RegisterResponseMessage(true, null);
    }

    private GetBatchesResponseMessage handleGetBatches() throws SQLException {
        return new GetBatchesResponseMessage(batchService.getAllBatches());
    }

    private GetSemestersResponseMessage handleGetSemesters() throws SQLException {
        return new GetSemestersResponseMessage(semesterService.getAllSemesters());
    }

    private GetAllSectionsResponseMessage handleGetAllSections() throws SQLException {
        return new GetAllSectionsResponseMessage(sectionService.getAllSections());
    }

    private GetAcademicYearsResponseMessage handleGetAcademicYears() throws SQLException {
        return new GetAcademicYearsResponseMessage(academicYearService.getAllYears());
    }

    private GetSectionsResponseMessage handleGetSections(GetSectionsMessage message) throws SQLException {
        return new GetSectionsResponseMessage(sectionService.getSectionsForBatchInActiveSemester(message.getBatchId()));
    }

    private CreateQuizResponseMessage handleCreateQuiz(CreateQuizMessage message) throws SQLException {
        int quizId = quizService.createQuizWithQuestions(message.getQuiz(), message.getQuestions());
        return new CreateQuizResponseMessage(true, null, quizId);
    }

    private EntityResponseMessage handleCreateBatch(CreateBatchMessage message) throws SQLException {
        int batchId = batchService.createBatch(message.getBatch());
        return new EntityResponseMessage(true, null, batchId);
    }

    private EntityResponseMessage handleCreateAcademicYear(CreateAcademicYearMessage message) throws SQLException {
        int academicYearId = academicYearService.createYearFromStart(message.getStartYear(), message.isActive());
        return new EntityResponseMessage(true, null, academicYearId);
    }

    private EntityResponseMessage handleCreateSemester(CreateSemesterMessage message) throws SQLException {
        int semesterId = semesterService.createSemester(message.getSemester());
        return new EntityResponseMessage(true, null, semesterId);
    }

    private EntityResponseMessage handleCreateSection(CreateSectionMessage message) throws SQLException {
        int sectionId = sectionService.createSection(message.getSection());
        return new EntityResponseMessage(true, null, sectionId);
    }

    private ActionResponseMessage handleSetSemesterActive(SetSemesterActiveMessage message) throws SQLException {
        semesterService.setSemesterActive(message.getSemesterId(), message.isActive());
        return new ActionResponseMessage(true, null);
    }

    private ActionResponseMessage handleEnrollStudent(EnrollStudentMessage message) throws SQLException {
        enrollmentService.enrollStudent(message.getStudentId(), message.getSectionId());
        return new ActionResponseMessage(true, null);
    }

    private ActionResponseMessage handleAssignStudentSection(AssignStudentSectionMessage message) throws SQLException {
        enrollmentService.enrollStudent(message.getStudentId(), message.getSectionId());
        return new ActionResponseMessage(true, null);
    }

    private ActionResponseMessage handleAssignTeacher(AssignTeacherSectionMessage message) throws SQLException {
        teacherSectionService.assignTeacher(message.getTeacherId(), message.getSectionId());
        return new ActionResponseMessage(true, null);
    }

    private ActionResponseMessage handleStartQuiz(StartQuizMessage message) throws SQLException {
        quizService.startQuiz(message.getQuizId());
        return new ActionResponseMessage(true, null);
    }

    private ActionResponseMessage handleCloseQuiz(CloseQuizMessage message) throws SQLException {
        quizService.closeQuiz(message.getQuizId());
        return new ActionResponseMessage(true, null);
    }

    private GetActiveQuizzesResponseMessage handleGetActiveQuizzes() throws SQLException {
        return new GetActiveQuizzesResponseMessage(quizService.getActiveQuizzes());
    }

    private GetStudentActiveQuizzesResponseMessage handleGetStudentActiveQuizzes(GetStudentActiveQuizzesMessage message) throws SQLException {
        var section = enrollmentService.getPrimarySectionForStudent(message.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student is not enrolled in a section."));
        return new GetStudentActiveQuizzesResponseMessage(quizService.getActiveQuizzesForSemester(section.getSemesterId()));
    }

    private GetTeacherQuizzesResponseMessage handleGetTeacherQuizzes(GetTeacherQuizzesMessage message) throws SQLException {
        return new GetTeacherQuizzesResponseMessage(quizService.getQuizzesForTeacher(message.getTeacherId()));
    }

    private JoinQuizResponseMessage handleJoinQuiz(JoinQuizMessage message) throws SQLException {
        var quiz = quizService.getQuiz(message.getQuizId())
                .orElse(null);
        if (quiz == null) {
            return new JoinQuizResponseMessage(false, "Quiz not found.", null, null);
        }
        if (!"ACTIVE".equalsIgnoreCase(quiz.getStatus())) {
            return new JoinQuizResponseMessage(false, "Quiz is not active.", null, null);
        }

        var section = enrollmentService.getPrimarySectionForStudent(message.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student is not enrolled in a section."));
        if (!quizService.canStudentJoinQuizInSemester(message.getStudentId(), message.getQuizId(), section.getId())) {
            return new JoinQuizResponseMessage(false, "Quiz is not available for your semester.", null, null);
        }

        sessionRegistry.getOrCreate(message.getQuizId(), message.getStudentId());
        return new JoinQuizResponseMessage(true, null, quiz, quizService.buildQuizPayload(quiz.getId()));
    }

    private AnswerSubmissionResponseMessage handleAnswerSubmission(AnswerSubmissionMessage message) throws SQLException {
        QuizSession session = sessionRegistry.find(message.getQuizId(), message.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student must join the quiz before submitting answers."));
        if (session.isSubmitted() || session.hasAnswered(message.getQuestionId())) {
            throw new IllegalArgumentException("Question has already been submitted in this session.");
        }

        Answer answer = new Answer();
        answer.setStudentId(message.getStudentId());
        answer.setQuizId(message.getQuizId());
        answer.setQuestionId(message.getQuestionId());
        answer.setChoiceId(message.getChoiceId());
        answer.setViolationCount(message.getViolationCount());
        answer.setForceSubmitted(message.isForceSubmitted());
        int answerId = quizService.submitAnswer(answer);
        session.recordAnswer(message.getQuestionId());
        session.setViolationCount(message.getViolationCount());
        if (message.isForceSubmitted()) {
            session.markSubmitted();
        }
        return new AnswerSubmissionResponseMessage(true, null, answerId);
    }

    private LoginResponseMessage onSuccessfulLogin(User user) {
        authenticatedUserId = user.getId();
        onlineRegistry.markOnline(user.getId(), socket);
        return new LoginResponseMessage(true, null, user);
    }
}

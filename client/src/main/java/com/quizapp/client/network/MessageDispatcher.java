package com.quizapp.client.network;

import com.quizapp.shared.message.Message;
import com.quizapp.shared.message.academic.ActionResponseMessage;
import com.quizapp.shared.message.academic.AssignStudentSectionMessage;
import com.quizapp.shared.message.academic.CreateAcademicYearMessage;
import com.quizapp.shared.message.academic.CreateBatchMessage;
import com.quizapp.shared.message.academic.CreateSectionMessage;
import com.quizapp.shared.message.academic.CreateSemesterMessage;
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
import com.quizapp.shared.message.auth.LoginMessage;
import com.quizapp.shared.message.auth.LoginResponseMessage;
import com.quizapp.shared.message.auth.RegisterMessage;
import com.quizapp.shared.message.auth.RegisterResponseMessage;
import com.quizapp.shared.message.quiz.CloseQuizMessage;
import com.quizapp.shared.message.quiz.CreateQuizMessage;
import com.quizapp.shared.message.quiz.CreateQuizResponseMessage;
import com.quizapp.shared.message.quiz.GetActiveQuizzesMessage;
import com.quizapp.shared.message.quiz.GetActiveQuizzesResponseMessage;
import com.quizapp.shared.message.quiz.JoinQuizMessage;
import com.quizapp.shared.message.quiz.JoinQuizResponseMessage;
import com.quizapp.shared.message.quiz.StartQuizMessage;
import com.quizapp.shared.message.student.AnswerSubmissionMessage;
import com.quizapp.shared.message.student.AnswerSubmissionResponseMessage;
import com.quizapp.shared.model.AcademicYear;
import com.quizapp.shared.model.Batch;
import com.quizapp.shared.model.Section;
import com.quizapp.shared.model.Semester;

import java.io.IOException;

public class MessageDispatcher {
    private final ServerConnection connection;

    public MessageDispatcher() {
        this(new ServerConnection());
    }

    public MessageDispatcher(ServerConnection connection) {
        this.connection = connection;
    }

    public LoginResponseMessage login(String username, String password) throws IOException, ClassNotFoundException {
        return expect(LoginResponseMessage.class, connection.send(new LoginMessage(username, password)));
    }

    public RegisterResponseMessage register(String username, String password, String fullName,
                                            String role, Integer batchId) throws IOException, ClassNotFoundException {
        RegisterMessage request = new RegisterMessage(username, password, fullName, role, batchId);
        return expect(RegisterResponseMessage.class, connection.send(request));
    }

    public GetBatchesResponseMessage getBatches() throws IOException, ClassNotFoundException {
        return expect(GetBatchesResponseMessage.class, connection.send(new GetBatchesMessage()));
    }

    public GetSemestersResponseMessage getSemesters() throws IOException, ClassNotFoundException {
        return expect(GetSemestersResponseMessage.class, connection.send(new GetSemestersMessage()));
    }

    public GetAllSectionsResponseMessage getAllSections() throws IOException, ClassNotFoundException {
        return expect(GetAllSectionsResponseMessage.class, connection.send(new GetAllSectionsMessage()));
    }

    public GetSectionsResponseMessage getSections(int batchId) throws IOException, ClassNotFoundException {
        return expect(GetSectionsResponseMessage.class, connection.send(new GetSectionsMessage(batchId)));
    }

    public ActionResponseMessage assignStudentSection(int studentId, int sectionId)
            throws IOException, ClassNotFoundException {
        AssignStudentSectionMessage request = new AssignStudentSectionMessage(studentId, sectionId);
        return expect(ActionResponseMessage.class, connection.send(request));
    }

    public GetAcademicYearsResponseMessage getAcademicYears() throws IOException, ClassNotFoundException {
        return expect(GetAcademicYearsResponseMessage.class, connection.send(new GetAcademicYearsMessage()));
    }

    public EntityResponseMessage createAcademicYear(int startYear, boolean active)
            throws IOException, ClassNotFoundException {
        return expect(EntityResponseMessage.class, connection.send(new CreateAcademicYearMessage(startYear, active)));
    }

    public EntityResponseMessage createBatch(Batch batch) throws IOException, ClassNotFoundException {
        return expect(EntityResponseMessage.class, connection.send(new CreateBatchMessage(batch)));
    }

    public EntityResponseMessage createSemester(Semester semester) throws IOException, ClassNotFoundException {
        return expect(EntityResponseMessage.class, connection.send(new CreateSemesterMessage(semester)));
    }

    public EntityResponseMessage createSection(Section section) throws IOException, ClassNotFoundException {
        return expect(EntityResponseMessage.class, connection.send(new CreateSectionMessage(section)));
    }

    public ActionResponseMessage startQuiz(int quizId) throws IOException, ClassNotFoundException {
        return expect(ActionResponseMessage.class, connection.send(new StartQuizMessage(quizId)));
    }

    public ActionResponseMessage closeQuiz(int quizId) throws IOException, ClassNotFoundException {
        return expect(ActionResponseMessage.class, connection.send(new CloseQuizMessage(quizId)));
    }

    public GetActiveQuizzesResponseMessage getActiveQuizzes() throws IOException, ClassNotFoundException {
        return expect(GetActiveQuizzesResponseMessage.class, connection.send(new GetActiveQuizzesMessage()));
    }

    public JoinQuizResponseMessage joinQuiz(int quizId, int studentId) throws IOException, ClassNotFoundException {
        return expect(JoinQuizResponseMessage.class, connection.send(new JoinQuizMessage(quizId, studentId)));
    }

    public CreateQuizResponseMessage createQuiz(CreateQuizMessage message) throws IOException, ClassNotFoundException {
        return expect(CreateQuizResponseMessage.class, connection.send(message));
    }

    public AnswerSubmissionResponseMessage submitAnswer(AnswerSubmissionMessage message)
            throws IOException, ClassNotFoundException {
        return expect(AnswerSubmissionResponseMessage.class, connection.send(message));
    }

    private <T extends Message> T expect(Class<T> type, Message response) throws IOException {
        if (type.isInstance(response)) {
            return type.cast(response);
        }
        throw new IOException("Unexpected response type: " + response.getClass().getName());
    }
}

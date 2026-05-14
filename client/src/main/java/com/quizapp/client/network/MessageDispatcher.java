package com.quizapp.client.network;

import com.quizapp.shared.message.Message;
import com.quizapp.shared.message.auth.LoginMessage;
import com.quizapp.shared.message.auth.LoginResponseMessage;
import com.quizapp.shared.message.auth.RegisterMessage;
import com.quizapp.shared.message.auth.RegisterResponseMessage;
import com.quizapp.shared.message.quiz.CreateQuizMessage;
import com.quizapp.shared.message.quiz.CreateQuizResponseMessage;
import com.quizapp.shared.message.student.AnswerSubmissionMessage;
import com.quizapp.shared.message.student.AnswerSubmissionResponseMessage;

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

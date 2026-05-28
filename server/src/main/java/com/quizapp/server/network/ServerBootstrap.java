package com.quizapp.server.network;

import com.quizapp.server.presence.OnlineRegistry;
import com.quizapp.server.service.AcademicYearService;
import com.quizapp.server.service.BatchService;
import com.quizapp.server.service.EnrollmentService;
import com.quizapp.server.service.AuthService;
import com.quizapp.server.service.QuizService;
import com.quizapp.server.service.SectionService;
import com.quizapp.server.service.SemesterService;
import com.quizapp.server.session.SessionRegistry;
import com.quizapp.shared.config.AppEnv;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerBootstrap {
    private static final int DEFAULT_PORT = 5050;

    private final int port;
    private final AuthService authService;
    private final QuizService quizService;
    private final AcademicYearService academicYearService;
    private final BatchService batchService;
    private final SemesterService semesterService;
    private final SectionService sectionService;
    private final EnrollmentService enrollmentService;
    private final OnlineRegistry onlineRegistry;
    private final SessionRegistry sessionRegistry;

    public ServerBootstrap() {
        this(AppEnv.getInt("SERVER_PORT", DEFAULT_PORT), new AuthService(), new QuizService(), new AcademicYearService(), new BatchService(), new SemesterService(),
                new SectionService(), new EnrollmentService(),
                new OnlineRegistry(), new SessionRegistry());
    }

    public ServerBootstrap(int port, AuthService authService, QuizService quizService,
                           AcademicYearService academicYearService,
                           BatchService batchService, SemesterService semesterService,
                           SectionService sectionService, EnrollmentService enrollmentService,
                           OnlineRegistry onlineRegistry,
                           SessionRegistry sessionRegistry) {
        this.port = port;
        this.authService = authService;
        this.quizService = quizService;
        this.academicYearService = academicYearService;
        this.batchService = batchService;
        this.semesterService = semesterService;
        this.sectionService = sectionService;
        this.enrollmentService = enrollmentService;
        this.onlineRegistry = onlineRegistry;
        this.sessionRegistry = sessionRegistry;
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket, authService, quizService, academicYearService, batchService,
                        semesterService, sectionService, enrollmentService,
                        onlineRegistry, sessionRegistry);
                Thread thread = new Thread(handler, "client-" + socket.getPort());
                thread.start();
            }
        }
    }
}

package com.quizapp.server.service;

import com.quizapp.server.dao.AnswerDao;
import com.quizapp.server.dao.ChoiceDao;
import com.quizapp.server.dao.QuestionDao;
import com.quizapp.server.dao.QuizDao;
import com.quizapp.server.dao.SemesterDao;
import com.quizapp.server.dao.UserDao;
import com.quizapp.shared.message.quiz.CreateQuizMessage;
import com.quizapp.shared.model.Answer;
import com.quizapp.shared.model.Choice;
import com.quizapp.shared.model.Question;
import com.quizapp.shared.model.Quiz;
import com.quizapp.shared.model.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QuizService {
    private final QuizDao quizDao;
    private final QuestionDao questionDao;
    private final ChoiceDao choiceDao;
    private final AnswerDao answerDao;
    private final SemesterDao semesterDao;
    private final UserDao userDao;

    public QuizService() {
        this(new QuizDao(), new QuestionDao(), new ChoiceDao(), new AnswerDao(), new SemesterDao(), new UserDao());
    }

    public QuizService(QuizDao quizDao, QuestionDao questionDao, ChoiceDao choiceDao,
                       AnswerDao answerDao, SemesterDao semesterDao, UserDao userDao) {
        this.quizDao = quizDao;
        this.questionDao = questionDao;
        this.choiceDao = choiceDao;
        this.answerDao = answerDao;
        this.semesterDao = semesterDao;
        this.userDao = userDao;
    }

    public int createQuiz(Quiz quiz) throws SQLException {
        validateQuiz(quiz);
        if (quiz.getStatus() == null || quiz.getStatus().isBlank()) {
            quiz.setStatus("DRAFT");
        }
        if (quiz.getPassingScore() <= 0) {
            quiz.setPassingScore(60);
        }
        return quizDao.insert(quiz);
    }

    public int createQuizWithQuestions(Quiz quiz, List<CreateQuizMessage.QuestionPayload> questionPayloads)
            throws SQLException {
        if (questionPayloads == null || questionPayloads.isEmpty()) {
            throw new IllegalArgumentException("Quiz must include at least one question.");
        }

        int quizId = createQuiz(quiz);

        for (CreateQuizMessage.QuestionPayload payload : questionPayloads) {
            validateQuestionPayload(payload);
            Question question = new Question();
            question.setQuizId(quizId);
            question.setBody(payload.getBody());
            question.setPosition(payload.getPosition());
            int questionId = addQuestion(question);

            for (CreateQuizMessage.ChoicePayload choicePayload : payload.getChoices()) {
                Choice choice = new Choice();
                choice.setQuestionId(questionId);
                choice.setBody(choicePayload.getBody());
                choice.setCorrect(choicePayload.isCorrect());
                addChoice(choice);
            }
        }

        return quizId;
    }

    public int addQuestion(Question question) throws SQLException {
        if (question == null || question.getBody() == null || question.getBody().isBlank()) {
            throw new IllegalArgumentException("Question body is required.");
        }
        if (question.getPosition() <= 0) {
            throw new IllegalArgumentException("Question position must be positive.");
        }
        if (quizDao.findById(question.getQuizId()).isEmpty()) {
            throw new IllegalArgumentException("Quiz not found.");
        }
        return questionDao.insert(question);
    }

    public int addChoice(Choice choice) throws SQLException {
        if (choice == null || choice.getBody() == null || choice.getBody().isBlank()) {
            throw new IllegalArgumentException("Choice body is required.");
        }
        if (questionDao.findById(choice.getQuestionId()).isEmpty()) {
            throw new IllegalArgumentException("Question not found.");
        }
        return choiceDao.insert(choice);
    }

    public int submitAnswer(Answer answer) throws SQLException {
        validateAnswer(answer);
        return answerDao.insert(answer);
    }

    public Optional<Quiz> getQuiz(int quizId) throws SQLException {
        return quizDao.findById(quizId);
    }

    public List<Quiz> getQuizzesForSemester(int semesterId) throws SQLException {
        return quizDao.findBySemesterId(semesterId);
    }

    public List<Question> getQuestionsForQuiz(int quizId) throws SQLException {
        return questionDao.findByQuizId(quizId);
    }

    public List<Choice> getChoicesForQuestion(int questionId) throws SQLException {
        return choiceDao.findByQuestionId(questionId);
    }

    public List<Answer> getAnswersForStudent(int studentId, int quizId) throws SQLException {
        return answerDao.findByStudentAndQuiz(studentId, quizId);
    }

    public void updateQuizStatus(int quizId, String status) throws SQLException {
        if (!List.of("DRAFT", "ACTIVE", "CLOSED").contains(String.valueOf(status).toUpperCase())) {
            throw new IllegalArgumentException("Quiz status must be DRAFT, ACTIVE, or CLOSED.");
        }
        if (quizDao.findById(quizId).isEmpty()) {
            throw new IllegalArgumentException("Quiz not found.");
        }
        quizDao.updateStatus(quizId, status);
    }

    public void startQuiz(int quizId) throws SQLException {
        updateQuizStatus(quizId, "ACTIVE");
    }

    public void closeQuiz(int quizId) throws SQLException {
        updateQuizStatus(quizId, "CLOSED");
    }

    public List<Quiz> getActiveQuizzes() throws SQLException {
        return quizDao.findByStatus("ACTIVE");
    }

    public List<CreateQuizMessage.QuestionPayload> buildQuizPayload(int quizId) throws SQLException {
        if (quizDao.findById(quizId).isEmpty()) {
            throw new IllegalArgumentException("Quiz not found.");
        }

        List<CreateQuizMessage.QuestionPayload> payload = new ArrayList<>();
        for (Question question : questionDao.findByQuizId(quizId)) {
            List<CreateQuizMessage.ChoicePayload> choices = new ArrayList<>();
            for (Choice choice : choiceDao.findByQuestionId(question.getId())) {
                choices.add(new CreateQuizMessage.ChoicePayload(choice.getBody(), choice.isCorrect()));
            }
            payload.add(new CreateQuizMessage.QuestionPayload(question.getBody(), question.getPosition(), choices));
        }
        return payload;
    }

    private void validateQuiz(Quiz quiz) throws SQLException {
        if (quiz == null) {
            throw new IllegalArgumentException("Quiz is required.");
        }
        if (quiz.getTitle() == null || quiz.getTitle().isBlank()) {
            throw new IllegalArgumentException("Quiz title is required.");
        }
        if (quiz.getTimeLimitSecs() <= 0) {
            throw new IllegalArgumentException("Quiz time limit must be positive.");
        }
        if (quiz.getPassingScore() < 0 || quiz.getPassingScore() > 100) {
            throw new IllegalArgumentException("Passing score must be between 0 and 100.");
        }
        semesterDao.findById(quiz.getSemesterId()).orElseThrow(() -> new IllegalArgumentException("Semester not found."));
        User creator = userDao.findById(quiz.getCreatedBy())
                .orElseThrow(() -> new IllegalArgumentException("Quiz creator not found."));
        if (!"TEACHER".equalsIgnoreCase(creator.getRole())) {
            throw new IllegalArgumentException("Only teachers can create quizzes.");
        }
    }

    private void validateQuestionPayload(CreateQuizMessage.QuestionPayload payload) {
        if (payload == null || payload.getBody() == null || payload.getBody().isBlank()) {
            throw new IllegalArgumentException("Quiz questions must have a body.");
        }
        if (payload.getPosition() <= 0) {
            throw new IllegalArgumentException("Question position must be positive.");
        }
        if (payload.getChoices() == null || payload.getChoices().size() < 2) {
            throw new IllegalArgumentException("Each question must have at least two choices.");
        }
        long correctChoices = payload.getChoices().stream().filter(CreateQuizMessage.ChoicePayload::isCorrect).count();
        if (correctChoices != 1) {
            throw new IllegalArgumentException("Each question must have exactly one correct choice.");
        }
    }

    private void validateAnswer(Answer answer) throws SQLException {
        if (answer == null) {
            throw new IllegalArgumentException("Answer is required.");
        }
        User student = userDao.findById(answer.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found."));
        if (!"STUDENT".equalsIgnoreCase(student.getRole())) {
            throw new IllegalArgumentException("Only students can submit answers.");
        }
        Quiz quiz = quizDao.findById(answer.getQuizId()).orElseThrow(() -> new IllegalArgumentException("Quiz not found."));
        Question question = questionDao.findById(answer.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found."));
        if (question.getQuizId() != quiz.getId()) {
            throw new IllegalArgumentException("Question does not belong to the quiz.");
        }
        Choice choice = choiceDao.findById(answer.getChoiceId())
                .orElseThrow(() -> new IllegalArgumentException("Choice not found."));
        if (choice.getQuestionId() != question.getId()) {
            throw new IllegalArgumentException("Choice does not belong to the question.");
        }
        if (answerDao.findByStudentQuizAndQuestion(answer.getStudentId(), answer.getQuizId(), answer.getQuestionId()).isPresent()) {
            throw new IllegalArgumentException("Student has already answered this question.");
        }
    }
}

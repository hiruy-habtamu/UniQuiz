package com.quizapp.server.service;

import com.quizapp.server.dao.EnrollmentDao;
import com.quizapp.server.dao.SectionDao;
import com.quizapp.server.dao.UserDao;
import com.quizapp.shared.model.Enrollment;
import com.quizapp.shared.model.User;

import java.sql.SQLException;
import java.util.List;

public class EnrollmentService {
    private final EnrollmentDao enrollmentDao;
    private final UserDao userDao;
    private final SectionDao sectionDao;

    public EnrollmentService() {
        this(new EnrollmentDao(), new UserDao(), new SectionDao());
    }

    public EnrollmentService(EnrollmentDao enrollmentDao, UserDao userDao, SectionDao sectionDao) {
        this.enrollmentDao = enrollmentDao;
        this.userDao = userDao;
        this.sectionDao = sectionDao;
    }

    public List<Enrollment> getEnrollmentsForStudent(int studentId) throws SQLException {
        return enrollmentDao.findByStudentId(studentId);
    }

    public List<Enrollment> getEnrollmentsForSection(int sectionId) throws SQLException {
        return enrollmentDao.findBySectionId(sectionId);
    }

    public void enrollStudent(int studentId, int sectionId) throws SQLException {
        User student = userDao.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found."));
        if (!"STUDENT".equalsIgnoreCase(student.getRole())) {
            throw new IllegalArgumentException("Only students can be enrolled in sections.");
        }
        sectionDao.findById(sectionId).orElseThrow(() -> new IllegalArgumentException("Section not found."));
        if (enrollmentDao.findByStudentAndSection(studentId, sectionId).isPresent()) {
            throw new IllegalArgumentException("Student is already enrolled in this section.");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(studentId);
        enrollment.setSectionId(sectionId);
        enrollmentDao.insert(enrollment);
    }

    public void unenrollStudent(int studentId, int sectionId) throws SQLException {
        enrollmentDao.delete(studentId, sectionId);
    }
}

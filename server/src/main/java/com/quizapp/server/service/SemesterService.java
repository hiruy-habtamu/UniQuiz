package com.quizapp.server.service;

import com.quizapp.server.dao.AcademicYearDao;
import com.quizapp.server.dao.SemesterDao;
import com.quizapp.server.dao.UserDao;
import com.quizapp.shared.model.Semester;
import com.quizapp.shared.model.User;

import java.sql.SQLException;
import java.util.Set;
import java.util.List;
import java.util.Optional;

public class SemesterService {
    private static final Set<String> ALLOWED_NAMES = Set.of("FIRST", "SECOND", "SUMMER");

    private final SemesterDao semesterDao;
    private final AcademicYearDao academicYearDao;
    private final UserDao userDao;

    public SemesterService() {
        this(new SemesterDao(), new AcademicYearDao(), new UserDao());
    }

    public SemesterService(SemesterDao semesterDao, AcademicYearDao academicYearDao, UserDao userDao) {
        this.semesterDao = semesterDao;
        this.academicYearDao = academicYearDao;
        this.userDao = userDao;
    }

    public List<Semester> getAllSemesters() throws SQLException {
        return semesterDao.findAll();
    }

    public List<Semester> getSemestersForYear(int academicYearId) throws SQLException {
        return semesterDao.findByAcademicYearId(academicYearId);
    }

    public Optional<Semester> getSemester(int id) throws SQLException {
        return semesterDao.findById(id);
    }

    public Optional<Semester> getActiveSemester() throws SQLException {
        return semesterDao.findActive();
    }

    public int createSemester(Semester semester) throws SQLException {
        validateSemester(semester);
        academicYearDao.findAll().stream()
                .filter(year -> year.getId() == semester.getAcademicYearId())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Academic year not found."));
        User creator = userDao.findById(semester.getCreatedBy())
                .orElseThrow(() -> new IllegalArgumentException("Semester creator not found."));
        if (!"TEACHER".equalsIgnoreCase(creator.getRole())) {
            throw new IllegalArgumentException("Only teachers can create semesters.");
        }
        boolean duplicate = semesterDao.findByAcademicYearId(semester.getAcademicYearId()).stream()
                .anyMatch(existing -> existing.getName().equalsIgnoreCase(semester.getName()));
        if (duplicate) {
            throw new IllegalArgumentException("Semester name already exists for this academic year.");
        }

        int id = semesterDao.insert(semester);
        if (semester.isActive()) {
            semesterDao.deactivateAllInAcademicYearExcept(semester.getAcademicYearId(), id);
        }
        return id;
    }

    public void setSemesterActive(int semesterId, boolean active) throws SQLException {
        Semester semester = semesterDao.findById(semesterId)
                .orElseThrow(() -> new IllegalArgumentException("Semester not found."));
        semesterDao.updateActive(semesterId, active);
        if (active) {
            semesterDao.deactivateAllInAcademicYearExcept(semester.getAcademicYearId(), semesterId);
        }
    }

    private void validateSemester(Semester semester) {
        if (semester == null) {
            throw new IllegalArgumentException("Semester is required.");
        }
        if (!ALLOWED_NAMES.contains(String.valueOf(semester.getName()).toUpperCase())) {
            throw new IllegalArgumentException("Semester name must be FIRST, SECOND, or SUMMER.");
        }
        if (semester.getStartDate() == null || semester.getEndDate() == null) {
            throw new IllegalArgumentException("Semester dates are required.");
        }
        if (!semester.getStartDate().isBefore(semester.getEndDate())) {
            throw new IllegalArgumentException("Semester start date must be before end date.");
        }
    }
}

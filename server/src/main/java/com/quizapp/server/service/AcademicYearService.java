package com.quizapp.server.service;

import com.quizapp.server.dao.AcademicYearDao;
import com.quizapp.shared.model.AcademicYear;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class AcademicYearService {

    private final AcademicYearDao dao;

    public AcademicYearService() {
        this(new AcademicYearDao());
    }

    public AcademicYearService(AcademicYearDao dao) {
        this.dao = dao;
    }

    public void ensureCurrentYearExists() throws SQLException {
        String label = deriveLabel(currentYear());
        if (dao.findByLabel(label).isEmpty()) {
            AcademicYear year = buildAcademicYear(currentYear(), true);
            year.setActive(true);
            dao.insert(year);
            dao.deactivateAllExcept(label);
        }
    }

    public List<AcademicYear> getAllYears() throws SQLException {
        return dao.findAll();
    }

    public Optional<AcademicYear> getActiveYear() throws SQLException {
        return dao.findActive();
    }

    public int createYear(AcademicYear academicYear) throws SQLException {
        validateYear(academicYear);
        if (dao.findByLabel(academicYear.getLabel()).isPresent()) {
            throw new IllegalArgumentException("Academic year label already exists.");
        }

        int id = dao.insert(academicYear);
        if (academicYear.isActive()) {
            dao.deactivateAllExcept(academicYear.getLabel());
        }
        return id;
    }

    public int createYearFromStart(int startYear, boolean active) throws SQLException {
        AcademicYear academicYear = buildAcademicYear(startYear, active);
        return createYear(academicYear);
    }

    private String deriveLabel(int year) {
        return year + "-" + (year + 1);
    }

    private int currentYear() {

        LocalDate now = LocalDate.now();
        return now.getMonthValue() >= 9 ? now.getYear() : now.getYear() - 1;
    }

    private void validateYear(AcademicYear academicYear) {
        if (academicYear == null) {
            throw new IllegalArgumentException("Academic year is required.");
        }
        if (academicYear.getLabel() == null || academicYear.getLabel().isBlank()) {
            throw new IllegalArgumentException("Academic year label is required.");
        }
        if (academicYear.getStartDate() == null || academicYear.getEndDate() == null) {
            throw new IllegalArgumentException("Academic year dates are required.");
        }
        if (!academicYear.getStartDate().isBefore(academicYear.getEndDate())) {
            throw new IllegalArgumentException("Academic year start date must be before end date.");
        }
    }

    private AcademicYear buildAcademicYear(int startYear, boolean active) {
        if (startYear < 1900) {
            throw new IllegalArgumentException("Academic year start year is invalid.");
        }

        AcademicYear year = new AcademicYear();
        year.setLabel(deriveLabel(startYear));
        year.setStartDate(LocalDate.of(startYear, 9, 1));
        year.setEndDate(LocalDate.of(startYear + 1, 8, 31));
        year.setActive(active);
        return year;
    }
}

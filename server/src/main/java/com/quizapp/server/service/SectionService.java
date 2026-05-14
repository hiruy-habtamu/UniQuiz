package com.quizapp.server.service;

import com.quizapp.server.dao.BatchDao;
import com.quizapp.server.dao.SemesterDao;
import com.quizapp.server.dao.SectionDao;
import com.quizapp.shared.model.Section;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class SectionService {
    private final SectionDao sectionDao;
    private final BatchDao batchDao;
    private final SemesterDao semesterDao;

    public SectionService() {
        this(new SectionDao(), new BatchDao(), new SemesterDao());
    }

    public SectionService(SectionDao sectionDao, BatchDao batchDao, SemesterDao semesterDao) {
        this.sectionDao = sectionDao;
        this.batchDao = batchDao;
        this.semesterDao = semesterDao;
    }

    public List<Section> getAllSections() throws SQLException {
        return sectionDao.findAll();
    }

    public List<Section> getSectionsForSemester(int semesterId) throws SQLException {
        return sectionDao.findBySemesterId(semesterId);
    }

    public Optional<Section> getSection(int id) throws SQLException {
        return sectionDao.findById(id);
    }

    public int createSection(Section section) throws SQLException {
        validateSection(section);
        batchDao.findById(section.getBatchId()).orElseThrow(() -> new IllegalArgumentException("Batch not found."));
        semesterDao.findById(section.getSemesterId()).orElseThrow(() -> new IllegalArgumentException("Semester not found."));
        if (sectionDao.findByNameBatchAndSemester(section.getName(), section.getBatchId(), section.getSemesterId()).isPresent()) {
            throw new IllegalArgumentException("Section already exists for this batch and semester.");
        }
        return sectionDao.insert(section);
    }

    private void validateSection(Section section) {
        if (section == null) {
            throw new IllegalArgumentException("Section is required.");
        }
        if (section.getName() == null || section.getName().isBlank()) {
            throw new IllegalArgumentException("Section name is required.");
        }
        if (section.getName().length() > 10) {
            throw new IllegalArgumentException("Section name must be 10 characters or fewer.");
        }
    }
}

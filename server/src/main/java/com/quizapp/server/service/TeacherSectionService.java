package com.quizapp.server.service;

import com.quizapp.server.dao.SectionDao;
import com.quizapp.server.dao.TeacherSectionDao;
import com.quizapp.server.dao.UserDao;
import com.quizapp.shared.model.TeacherSection;
import com.quizapp.shared.model.User;

import java.sql.SQLException;
import java.util.List;

public class TeacherSectionService {
    private final TeacherSectionDao teacherSectionDao;
    private final UserDao userDao;
    private final SectionDao sectionDao;

    public TeacherSectionService() {
        this(new TeacherSectionDao(), new UserDao(), new SectionDao());
    }

    public TeacherSectionService(TeacherSectionDao teacherSectionDao, UserDao userDao, SectionDao sectionDao) {
        this.teacherSectionDao = teacherSectionDao;
        this.userDao = userDao;
        this.sectionDao = sectionDao;
    }

    public List<TeacherSection> getAssignmentsForTeacher(int teacherId) throws SQLException {
        return teacherSectionDao.findByTeacherId(teacherId);
    }

    public List<TeacherSection> getAssignmentsForSection(int sectionId) throws SQLException {
        return teacherSectionDao.findBySectionId(sectionId);
    }

    public void assignTeacher(int teacherId, int sectionId) throws SQLException {
        User teacher = userDao.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found."));
        if (!"TEACHER".equalsIgnoreCase(teacher.getRole())) {
            throw new IllegalArgumentException("Only teachers can be assigned to sections.");
        }
        sectionDao.findById(sectionId).orElseThrow(() -> new IllegalArgumentException("Section not found."));
        if (teacherSectionDao.findByTeacherAndSection(teacherId, sectionId).isPresent()) {
            throw new IllegalArgumentException("Teacher is already assigned to this section.");
        }

        TeacherSection assignment = new TeacherSection();
        assignment.setTeacherId(teacherId);
        assignment.setSectionId(sectionId);
        teacherSectionDao.insert(assignment);
    }

    public void unassignTeacher(int teacherId, int sectionId) throws SQLException {
        teacherSectionDao.delete(teacherId, sectionId);
    }
}

package com.quizapp.shared.model;

import java.io.Serializable;
import java.time.LocalDate;

public class Semester implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int academicYearId;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
    private int createdBy;

    public Semester() {}

    public int getId() { return id; }
    public int getAcademicYearId() { return academicYearId; }
    public String getName() { return name; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public boolean isActive() { return active; }
    public int getCreatedBy() { return createdBy; }

    public void setId(int id) { this.id = id; }
    public void setAcademicYearId(int academicYearId) { this.academicYearId = academicYearId; }
    public void setName(String name) { this.name = name; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public void setActive(boolean active) { this.active = active; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }
}

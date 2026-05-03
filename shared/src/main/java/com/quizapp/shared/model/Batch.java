package com.quizapp.shared.model;

import java.io.Serializable;

public class Batch implements Serializable {
    private static final long serialVersionUID = 1L;

    private int    id;
    private int    entryYear;
    private String program;
    private int    createdBy;

    public Batch() {}

    public int    getId()        { return id;        }
    public int    getEntryYear() { return entryYear; }
    public String getProgram()   { return program;   }
    public int    getCreatedBy() { return createdBy; }

    public void setId(int id)               { this.id = id;               }
    public void setEntryYear(int entryYear) { this.entryYear = entryYear; }
    public void setProgram(String program)  { this.program = program;     }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    // Readable label used in UI
    @Override
    public String toString() {
        return program + " — Batch " + entryYear;
    }
}
package com.quizapp.shared.model;

import java.io.Serializable;
import java.time.LocalDate;

public class AcademicYear implements Serializable {
    private static final long serialVersionUID = 1L;

    private int       id;
    private String    label;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean   isActive;

    public AcademicYear() {}

    public int       getId()        { return id;        }
    public String    getLabel()     { return label;     }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate()   { return endDate;   }
    public boolean   isActive()     { return isActive;  }

    public void setId(int id)                  { this.id = id;               }
    public void setLabel(String label)         { this.label = label;         }
    public void setStartDate(LocalDate d)      { this.startDate = d;         }
    public void setEndDate(LocalDate d)        { this.endDate = d;           }
    public void setActive(boolean isActive)    { this.isActive = isActive;   }

    @Override
    public String toString() {
        return label;
    }
}

import com.quizapp.server.dao.AcademicYearDao;
import com.quizapp.shared.model.AcademicYear;

import java.time.LocalDate;

public class AcademicYearService {

    private final AcademicYearDao dao;

    public AcademicYearService(AcademicYearDao dao) {
        this.dao = dao;
    }

    public void ensureCurrentYearExists() throws Exception {
        String label = deriveLabel();
        if (dao.findByLabel(label).isEmpty()) {
            AcademicYear year = new AcademicYear();
            year.setLabel(label);
            year.setStartDate(LocalDate.of(currentYear(), 9, 1));
            year.setEndDate(LocalDate.of(currentYear() + 1, 8, 31));
            year.setActive(true);
            dao.insert(year);
            dao.deactivateAllExcept(label);
        }
    }

    private String deriveLabel() {
        int year = currentYear();
        return year + "/" + String.valueOf(year + 1).substring(2);
        // return e.g. 2025/26
    }

    private int currentYear() {

        LocalDate now = LocalDate.now();
        return now.getMonthValue() >= 9 ? now.getYear() : now.getYear() - 1;
    }
}
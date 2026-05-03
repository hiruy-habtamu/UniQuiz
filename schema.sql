CREATE DATABASE IF NOT EXISTS quizapp
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE quizapp;


CREATE TABLE academic_years (
    id         INT PRIMARY KEY AUTO_INCREMENT,
    label      VARCHAR(20) NOT NULL UNIQUE,   -- "2025/26"
    start_date DATE        NOT NULL,
    end_date   DATE        NOT NULL,
    is_active  BOOLEAN     NOT NULL DEFAULT FALSE
) ENGINE=InnoDB;

-- ─────────────────────────────────────────────────────
-- Users — created without batch FK first (circular dep break)
-- ─────────────────────────────────────────────────────

CREATE TABLE users (
    id            INT PRIMARY KEY AUTO_INCREMENT,
    username      VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(150) NOT NULL,
    role          ENUM('TEACHER','STUDENT') NOT NULL,
    batch_id      INT NULL,                    -- set for students, NULL for teachers
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ─────────────────────────────────────────────────────
-- Created by teacher from client
-- ─────────────────────────────────────────────────────

-- A batch = one cohort of students, e.g. "BSCS students who entered in 2022"
-- Differentiates 1st year BSCS from 3rd year BSCS in the same semester
CREATE TABLE batches (
    id         INT PRIMARY KEY AUTO_INCREMENT,
    entry_year INT         NOT NULL,          -- 2022, 2023, 2024 etc
    program    VARCHAR(50) NOT NULL,          -- "BSCS", "BSIT", "BSIS"
    created_by INT         NOT NULL,
    UNIQUE KEY uq_batch (entry_year, program),
    FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB;

-- Deferred FK: users.batch_id → batches (could not be inline due to circular dependency)
ALTER TABLE users
    ADD CONSTRAINT fk_users_batch
    FOREIGN KEY (batch_id) REFERENCES batches(id);

CREATE TABLE semesters (
    id               INT PRIMARY KEY AUTO_INCREMENT,
    academic_year_id INT  NOT NULL,
    name             ENUM('FIRST','SECOND','SUMMER') NOT NULL,
    start_date       DATE NOT NULL,
    end_date         DATE NOT NULL,
    is_active        BOOLEAN NOT NULL DEFAULT FALSE,
    created_by       INT  NOT NULL,
    UNIQUE KEY uq_semester (academic_year_id, name),
    FOREIGN KEY (academic_year_id) REFERENCES academic_years(id),
    FOREIGN KEY (created_by)       REFERENCES users(id)
) ENGINE=InnoDB;

-- Section belongs to a batch within a semester
-- e.g. BSCS 2022 batch, Section A, First Sem 2025/26
CREATE TABLE sections (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(10) NOT NULL,          -- "A", "B", "C"
    batch_id    INT         NOT NULL,
    semester_id INT         NOT NULL,
    UNIQUE KEY uq_section (name, batch_id, semester_id),
    FOREIGN KEY (batch_id)    REFERENCES batches(id),
    FOREIGN KEY (semester_id) REFERENCES semesters(id)
) ENGINE=InnoDB;

CREATE TABLE enrollments (
    student_id  INT NOT NULL,
    section_id  INT NOT NULL,
    PRIMARY KEY (student_id, section_id),
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (section_id) REFERENCES sections(id)
) ENGINE=InnoDB;

CREATE TABLE teacher_sections (
    teacher_id  INT NOT NULL,
    section_id  INT NOT NULL,
    PRIMARY KEY (teacher_id, section_id),
    FOREIGN KEY (teacher_id) REFERENCES users(id),
    FOREIGN KEY (section_id) REFERENCES sections(id)
) ENGINE=InnoDB;

-- ─────────────────────────────────────────────────────
-- Quizzes — created by teacher from client
-- ─────────────────────────────────────────────────────

CREATE TABLE quizzes (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    title           VARCHAR(200) NOT NULL,
    semester_id     INT          NOT NULL,
    created_by      INT          NOT NULL,
    time_limit_secs INT          NOT NULL,
    passing_score   INT          NOT NULL DEFAULT 60,
    status          ENUM('DRAFT','ACTIVE','CLOSED') NOT NULL DEFAULT 'DRAFT',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (semester_id) REFERENCES semesters(id),
    FOREIGN KEY (created_by)  REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE questions (
    id       INT PRIMARY KEY AUTO_INCREMENT,
    quiz_id  INT  NOT NULL,
    body     TEXT NOT NULL,
    position INT  NOT NULL,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id)
) ENGINE=InnoDB;

CREATE TABLE choices (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    question_id INT     NOT NULL,
    body        TEXT    NOT NULL,
    is_correct  BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (question_id) REFERENCES questions(id)
) ENGINE=InnoDB;

-- ─────────────────────────────────────────────────────
-- Results — produced at runtime
-- ─────────────────────────────────────────────────────

CREATE TABLE answers (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    student_id      INT     NOT NULL,
    quiz_id         INT     NOT NULL,
    question_id     INT     NOT NULL,
    choice_id       INT     NOT NULL,
    answered_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    violation_count INT     NOT NULL DEFAULT 0,
    force_submitted BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (student_id)  REFERENCES users(id),
    FOREIGN KEY (quiz_id)     REFERENCES quizzes(id),
    FOREIGN KEY (question_id) REFERENCES questions(id),
    FOREIGN KEY (choice_id)   REFERENCES choices(id)
) ENGINE=InnoDB;

-- ============================================================
-- CRM Xclusive Barber — Schema
-- PostgreSQL (Supabase)
-- ============================================================

-- ENUMS
CREATE TYPE role_enum AS ENUM ('ADMIN', 'BARBER', 'CLIENT');
CREATE TYPE appointment_status_enum AS ENUM ('PENDING', 'COMPLETED', 'CANCELLED', 'NO_SHOW');
CREATE TYPE client_tier_enum AS ENUM ('NEW', 'REGULAR', 'VIP');

-- ============================================================
-- TABLES
-- ============================================================

CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          role_enum   NOT NULL,
    active        BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE client_profiles (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT          NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    phone            VARCHAR(20)     NOT NULL UNIQUE,
    notes            TEXT,
    loyalty_points   INT             NOT NULL DEFAULT 0 CHECK (loyalty_points >= 0),
    tier             client_tier_enum NOT NULL DEFAULT 'NEW',
    last_completed_at TIMESTAMP
);

CREATE TABLE barber_profiles (
    id        BIGSERIAL PRIMARY KEY,
    user_id   BIGINT       NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    phone     VARCHAR(20)  NOT NULL,
    specialty VARCHAR(255)
);

CREATE TABLE barber_schedules (
    id               BIGSERIAL PRIMARY KEY,
    barber_id        BIGINT  NOT NULL REFERENCES barber_profiles(id) ON DELETE CASCADE,
    day_of_week      SMALLINT NOT NULL CHECK (day_of_week BETWEEN 0 AND 6),
    start_hour       SMALLINT NOT NULL CHECK (start_hour BETWEEN 0 AND 23),
    end_hour         SMALLINT NOT NULL CHECK (end_hour BETWEEN 0 AND 23),
    break_start_hour SMALLINT         CHECK (break_start_hour BETWEEN 0 AND 23),
    is_available     BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (barber_id, day_of_week)
);

CREATE TABLE services (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(255) NOT NULL UNIQUE,
    description  TEXT,
    points_value INT          NOT NULL DEFAULT 0 CHECK (points_value >= 0),
    active       BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE appointments (
    id               BIGSERIAL PRIMARY KEY,
    client_id        BIGINT                  NOT NULL REFERENCES client_profiles(id) ON DELETE RESTRICT,
    barber_id        BIGINT                  NOT NULL REFERENCES barber_profiles(id) ON DELETE RESTRICT,
    service_id       BIGINT                  NOT NULL REFERENCES services(id)        ON DELETE RESTRICT,
    appointment_date DATE                    NOT NULL,
    start_hour       SMALLINT                NOT NULL CHECK (start_hour BETWEEN 0 AND 23),
    status           appointment_status_enum NOT NULL DEFAULT 'PENDING',
    created_at       TIMESTAMP               NOT NULL DEFAULT NOW(),
    UNIQUE (barber_id, appointment_date, start_hour)
);

CREATE TABLE loyalty_transactions (
    id            BIGSERIAL PRIMARY KEY,
    client_id     BIGINT      NOT NULL REFERENCES client_profiles(id) ON DELETE CASCADE,
    appointment_id BIGINT              REFERENCES appointments(id)    ON DELETE SET NULL,
    points_change INT         NOT NULL,
    reason        VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- ============================================================
-- INDEXES
-- ============================================================

CREATE INDEX idx_appointments_barber_date ON appointments (barber_id, appointment_date);
CREATE INDEX idx_appointments_client      ON appointments (client_id);
CREATE INDEX idx_appointments_date        ON appointments (appointment_date);
CREATE INDEX idx_client_profiles_tier     ON client_profiles (tier);

-- ============================================================
-- SEED DATA
-- password_hash = BCrypt("Admin1234!", strength 10)
-- ============================================================

-- Admin
INSERT INTO users (email, password_hash, role, active)
VALUES ('admin@xclusivebarber.com',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
        'ADMIN', TRUE);

-- Barbers
INSERT INTO users (email, password_hash, role, active)
VALUES ('carlos@xclusivebarber.com',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
        'BARBER', TRUE);

INSERT INTO users (email, password_hash, role, active)
VALUES ('miguel@xclusivebarber.com',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
        'BARBER', TRUE);

INSERT INTO barber_profiles (user_id, phone, specialty)
VALUES ((SELECT id FROM users WHERE email = 'carlos@xclusivebarber.com'),
        '+573001234567', 'Corte clásico y degradado');

INSERT INTO barber_profiles (user_id, phone, specialty)
VALUES ((SELECT id FROM users WHERE email = 'miguel@xclusivebarber.com'),
        '+573009876543', 'Barba y diseño facial');

-- Services
INSERT INTO services (name, description, points_value, active)
VALUES ('Corte clásico',   'Corte tradicional con tijera y máquina',           10, TRUE),
       ('Corte + Barba',   'Corte completo con arreglo y diseño de barba',      20, TRUE),
       ('Afeitado royal',  'Afeitado con navaja, vapor y cremas hidratantes',   15, TRUE);

CREATE TABLE violations (
    id                BIGSERIAL    PRIMARY KEY,
    license_plate     VARCHAR(8)   NOT NULL,
    equipment_id      VARCHAR(50)  NOT NULL,
    measured_speed    INTEGER      NOT NULL,
    considered_speed  INTEGER      NOT NULL,
    speed_limit       INTEGER      NOT NULL,
    excess_percentage NUMERIC(5,2) NOT NULL,
    severity          VARCHAR(20)  NOT NULL,
    capture_timestamp TIMESTAMPTZ  NOT NULL,
    processed_at      TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_violations_license_plate ON violations(license_plate);

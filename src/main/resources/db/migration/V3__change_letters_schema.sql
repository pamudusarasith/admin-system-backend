DROP TABLE IF EXISTS letters CASCADE;

DROP TYPE IF EXISTS mode_of_arrival_enum;
CREATE TYPE mode_of_arrival_enum AS ENUM (
    'REGISTERED_POST',
    'UNREGISTERED_POST',
    'EMAIL',
    'WHATSAPP',
    'HAND_DELIVERED',
    'FAX',
    'OTHER'
    );

DROP TYPE IF EXISTS priority_enum;
CREATE TYPE priority_enum AS ENUM ('NORMAL', 'HIGH', 'URGENT');

DROP TYPE IF EXISTS letter_status_enum;
CREATE TYPE letter_status_enum AS ENUM (
    'NEW',
    'ASSIGNED_TO_DIVISION',
    'PENDING_ACCEPTANCE',
    'ASSIGNED_TO_OFFICER',
    'RETURNED_FROM_OFFICER',
    'RETURNED_FROM_DIVISION',
    'CLOSED'
    );

CREATE TABLE letters
(
    id                   SERIAL PRIMARY KEY,
    reference            VARCHAR(50) UNIQUE             NOT NULL,
    sender_details       JSONB,
    sent_date            DATE,
    received_date        DATE                           NOT NULL,
    mode_of_arrival      mode_of_arrival_enum           NOT NULL,
    subject              VARCHAR(255)                   NOT NULL,
    content              TEXT,
    priority             priority_enum DEFAULT 'NORMAL' NOT NULL,
    status               letter_status_enum             NOT NULL,
    assigned_division_id INT REFERENCES divisions (id),
    assigned_user_id     INT REFERENCES users (id),
    is_accepted_by_user  BOOLEAN       DEFAULT FALSE
);
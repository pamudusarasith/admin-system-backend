DROP TABLE IF EXISTS letters CASCADE;

CREATE TABLE letters
(
    id                   SERIAL PRIMARY KEY,
    reference            VARCHAR(50) UNIQUE             NOT NULL,
    sender_details       JSONB,
    receiver_details     JSONB,
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
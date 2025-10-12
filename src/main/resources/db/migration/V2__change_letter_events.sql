DROP TABLE IF EXISTS letter_events;
DROP TYPE IF EXISTS event_type_enum;

CREATE TYPE event_type_enum AS ENUM (
    'ADD_NOTE',
    'ADD_ATTACHMENT',
    'REMOVE_ATTACHMENT',
    'REPLY',
    'CHANGE_STATUS',
    'CHANGE_PRIORITY',
    'UPDATE_DETAILS'
    );

CREATE TABLE letter_events
(
    id            SERIAL PRIMARY KEY,
    letter_id     INT             NOT NULL REFERENCES letters (id),
    user_id       INT             NOT NULL REFERENCES users (id),
    event_type    event_type_enum NOT NULL,
    event_details JSONB,
    created_at    TIMESTAMPTZ DEFAULT now()
);
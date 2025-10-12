CREATE TYPE mode_of_arrival_enum AS ENUM (
    'REGISTERED_POST',
    'UNREGISTERED_POST',
    'EMAIL',
    'WHATSAPP',
    'HAND_DELIVERED',
    'FAX',
    'OTHER'
    );

CREATE TYPE priority_enum AS ENUM ('NORMAL', 'HIGH', 'URGENT');

CREATE TYPE letter_status_enum AS ENUM (
    'NEW',
    'ASSIGNED_TO_DIVISION',
    'PENDING_ACCEPTANCE',
    'ASSIGNED_TO_OFFICER',
    'RETURNED_FROM_OFFICER',
    'RETURNED_FROM_DIVISION',
    'CLOSED'
    );

CREATE TYPE event_type_enum AS ENUM ('REGISTER', 'FORWARD', 'ASSIGN', 'RETURN', 'REPLY', 'ADD_NOTE', 'ADD_ATTACHMENT', 'SIGN', 'UPDATE_STATUS', 'LINK_LETTER');

CREATE TYPE parent_type_enum AS ENUM (
    'LETTER',
    'LETTER_EVENT'
    );

CREATE TYPE confidentiality_level_enum AS ENUM ('PUBLIC', 'INTERNAL', 'CONFIDENTIAL', 'SECRET');
CREATE TYPE cabinet_paper_status_enum AS ENUM ('DRAFT', 'SUBMITTED', 'APPROVED_FOR_CABINET', 'CONSIDERED', 'REJECTED', 'ARCHIVED');
CREATE TYPE decision_type_enum AS ENUM ('APPROVED', 'APPROVED_WITH_AMENDMENTS', 'REJECTED', 'DEFERRED');
CREATE TYPE related_item_type_enum AS ENUM ('LETTER', 'CABINET_PAPER');

CREATE OR REPLACE FUNCTION update_updated_at_column()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TABLE roles
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(50) UNIQUE NOT NULL,
    description TEXT
);

CREATE TABLE permissions
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100) UNIQUE NOT NULL,
    description TEXT
);

CREATE TABLE role_permissions
(
    role_id       INT NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    permission_id INT NOT NULL REFERENCES permissions (id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE divisions
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100) UNIQUE NOT NULL,
    description TEXT
);

CREATE TABLE users
(
    id                     SERIAL PRIMARY KEY,
    username               VARCHAR(50) UNIQUE NOT NULL,
    email                  VARCHAR(100),
    password               VARCHAR(255)       NOT NULL,
    full_name              VARCHAR(100) DEFAULT 'New User',
    phone_number           VARCHAR(20),
    role_id                INT                NOT NULL REFERENCES roles (id),
    division_id            INT                NOT NULL REFERENCES divisions (id),
    is_active              BOOLEAN      DEFAULT TRUE,
    account_setup_required BOOLEAN      DEFAULT TRUE,
    created_at             TIMESTAMPTZ  DEFAULT now(),
    updated_at             TIMESTAMPTZ  DEFAULT now(),
    deleted_at             TIMESTAMPTZ
);

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
    is_accepted_by_user  BOOLEAN       DEFAULT FALSE,
    created_at           TIMESTAMPTZ   DEFAULT now(),
    updated_at           TIMESTAMPTZ   DEFAULT now(),
    deleted_at           TIMESTAMPTZ,
    CONSTRAINT chk_letter_dates CHECK (sent_date IS NULL OR sent_date <= received_date),
    CONSTRAINT chk_assigned_division CHECK (
        (status != 'ASSIGNED_TO_DIVISION') OR
        (status = 'ASSIGNED_TO_DIVISION' AND assigned_division_id IS NOT NULL)
        ),
    CONSTRAINT chk_assigned_officer CHECK (
        (status != 'ASSIGNED_TO_OFFICER') OR
        (status = 'ASSIGNED_TO_OFFICER' AND assigned_user_id IS NOT NULL)
        )
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

CREATE TABLE attachments
(
    id          SERIAL PRIMARY KEY,
    parent_type parent_type_enum NOT NULL,
    parent_id   INT              NOT NULL,
    file_name   VARCHAR(255)     NOT NULL,
    file_path   VARCHAR(255)     NOT NULL,
    file_type   VARCHAR(50),
    file_size   BIGINT CHECK (file_size > 0),
    created_at  TIMESTAMPTZ DEFAULT now(),
    updated_at  TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE cabinet_paper_categories
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE cabinet_papers
(
    id                    SERIAL PRIMARY KEY,
    reference_id          VARCHAR(50) UNIQUE        NOT NULL,
    title                 VARCHAR(255)              NOT NULL,
    summary               TEXT,
    category_id           INT REFERENCES cabinet_paper_categories (id),
    confidentiality_level confidentiality_level_enum DEFAULT 'INTERNAL',
    status                cabinet_paper_status_enum NOT NULL,
    submitted_by_user_id  INT                       NOT NULL REFERENCES users (id),
    submission_date       DATE,
    created_at            TIMESTAMPTZ                DEFAULT now(),
    updated_at            TIMESTAMPTZ                DEFAULT now(),
    deleted_at            TIMESTAMPTZ
);

CREATE TABLE cabinet_decisions
(
    id                  SERIAL PRIMARY KEY,
    paper_id            INT UNIQUE         NOT NULL REFERENCES cabinet_papers (id),
    decision_text       TEXT               NOT NULL,
    decision_type       decision_type_enum NOT NULL,
    decision_date       DATE               NOT NULL,
    recorded_by_user_id INT                NOT NULL REFERENCES users (id),
    created_at          TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE refresh_tokens
(
    id                   SERIAL PRIMARY KEY,
    jti                  VARCHAR(36) UNIQUE NOT NULL,
    user_id              INT                NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    expires_at           TIMESTAMPTZ        NOT NULL,
    replaced_by_token_id INT                REFERENCES refresh_tokens (id) ON DELETE SET NULL,
    revoked              BOOLEAN     DEFAULT FALSE,
    created_at           TIMESTAMPTZ DEFAULT now(),
    updated_at           TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE notifications
(
    id                SERIAL PRIMARY KEY,
    user_id           INT  NOT NULL REFERENCES users (id),
    message           TEXT NOT NULL,
    related_item_type related_item_type_enum,
    related_item_id   INT,
    is_read           BOOLEAN     DEFAULT FALSE,
    created_at        TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE audit_log
(
    id              BIGSERIAL PRIMARY KEY,
    user_id         INT REFERENCES users (id),
    action_type     VARCHAR(255) NOT NULL,
    target_resource VARCHAR(100),
    target_id       INT,
    old_values      JSONB,
    new_values      JSONB,
    details         JSONB,
    ip_address      VARCHAR(45),
    user_agent      TEXT,
    created_at      TIMESTAMPTZ DEFAULT now()
);

CREATE TRIGGER trigger_users_updated_at
    BEFORE UPDATE
    ON users
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_letters_updated_at
    BEFORE UPDATE
    ON letters
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_cabinet_papers_updated_at
    BEFORE UPDATE
    ON cabinet_papers
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_refresh_tokens_updated_at
    BEFORE UPDATE
    ON refresh_tokens
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_attachments_updated_at
    BEFORE UPDATE
    ON attachments
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE INDEX idx_letters_status ON letters (status);
CREATE INDEX idx_letters_assigned_division ON letters (assigned_division_id);
CREATE INDEX idx_letters_assigned_user ON letters (assigned_user_id);
CREATE INDEX idx_letters_received_date ON letters (received_date);
CREATE INDEX idx_letters_sent_date ON letters (sent_date);
CREATE INDEX idx_letter_events_letter_id ON letter_events (letter_id);
CREATE INDEX idx_cabinet_papers_status ON cabinet_papers (status);
CREATE INDEX idx_cabinet_papers_category ON cabinet_papers (category_id);
CREATE INDEX idx_users_role ON users (role_id);
CREATE INDEX idx_users_division ON users (division_id);
CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_email ON users (email);

CREATE INDEX idx_refresh_tokens_jti ON refresh_tokens (jti);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);

CREATE INDEX idx_notifications_user_read ON notifications (user_id, is_read);
CREATE INDEX idx_audit_log_created_at ON audit_log (created_at);
CREATE INDEX idx_audit_log_user_id ON audit_log (user_id);
CREATE INDEX idx_attachments_parent ON attachments (parent_type, parent_id);

CREATE INDEX idx_users_deleted_at ON users (deleted_at);
CREATE INDEX idx_letters_deleted_at ON letters (deleted_at);
CREATE INDEX idx_cabinet_papers_deleted_at ON cabinet_papers (deleted_at);

CREATE INDEX idx_letters_sender_details ON letters USING GIN (sender_details);
CREATE INDEX idx_letters_receiver_details ON letters USING GIN (receiver_details);
CREATE INDEX idx_letter_events_details ON letter_events USING GIN (event_details);

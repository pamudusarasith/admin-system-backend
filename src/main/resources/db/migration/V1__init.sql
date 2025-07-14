-- =================================================================
-- Administrative Division Management System Database Schema (PostgreSQL)
-- =================================================================

-- -------------------------------------
-- Custom Type Definitions (ENUMs)
-- -------------------------------------

CREATE TYPE mode_of_arrival_enum AS ENUM('mail', 'email', 'whatsapp', 'by_hand', 'internal', 'other');
CREATE TYPE priority_enum AS ENUM('Normal', 'Urgent', 'High');
CREATE TYPE letter_status_enum AS ENUM('Awaiting Forwarding', 'Awaiting Assignment', 'In Progress', 'Replied', 'Closed', 'Archived', 'Returned');
CREATE TYPE event_type_enum AS ENUM('REGISTER', 'FORWARD', 'ASSIGN', 'RETURN', 'REPLY', 'ADD_NOTE', 'ADD_ATTACHMENT', 'SIGN', 'UPDATE_STATUS', 'LINK_LETTER');
CREATE TYPE parent_type_enum AS ENUM('letter', 'letter_event');
CREATE TYPE confidentiality_level_enum AS ENUM('Public', 'Internal', 'Confidential', 'Secret');
CREATE TYPE cabinet_paper_status_enum AS ENUM('Draft', 'Submitted', 'Approved for Cabinet', 'Considered', 'Rejected', 'Archived');
CREATE TYPE decision_type_enum AS ENUM('Approved', 'Approved with Amendments', 'Rejected', 'Deferred');
CREATE TYPE related_item_type_enum AS ENUM('letter', 'cabinet_paper');


-- -------------------------------------
-- Trigger Function for updated_at
-- -------------------------------------

-- This function will be triggered to automatically update the 'updated_at' column.
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
   NEW.updated_at = now();
   RETURN NEW;
END;
$$ language 'plpgsql';


-- -------------------------------------
-- Core User and System Tables
-- -------------------------------------

CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL, -- e.g., 'Postal Officer', 'Subject Officer', 'Admin'
    description TEXT
);

CREATE TABLE permissions (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL, -- e.g., 'letter:create', 'user:manage', 'report:view'
    description TEXT
);

CREATE TABLE role_permissions (
    role_id INT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id INT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE divisions (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT
);

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100),
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) DEFAULT 'New User',
    phone_number VARCHAR(20),
    role_id INT NOT NULL REFERENCES roles(id),
    division_id INT NOT NULL REFERENCES divisions(id),
    is_active BOOLEAN DEFAULT TRUE,
    account_setup_required BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);


-- -------------------------------------
-- Letter Management System Tables
-- -------------------------------------

CREATE TABLE letters (
    id SERIAL PRIMARY KEY,
    reference_id VARCHAR(50) UNIQUE NOT NULL, -- System-generated unique ID (e.g., L-2025-1234)
    external_reference_no VARCHAR(100),
    subject VARCHAR(255) NOT NULL,
    sender_details JSONB, -- Using JSONB for structured details like name, address, contact
    mode_of_arrival mode_of_arrival_enum NOT NULL,
    priority priority_enum DEFAULT 'Normal',
    status letter_status_enum NOT NULL,
    handler_user_id INT REFERENCES users(id),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE letter_events (
    id SERIAL PRIMARY KEY,
    letter_id INT NOT NULL REFERENCES letters(id),
    user_id INT NOT NULL REFERENCES users(id),
    event_type event_type_enum NOT NULL,
    event_details JSONB, -- Using JSONB for structured data (e.g., { "from_user_id": 5, "to_user_id": 10, "note": "Please handle." })
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE attachments (
    id SERIAL PRIMARY KEY,
    parent_id INT NOT NULL,
    parent_type parent_type_enum NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_type VARCHAR(50),
    created_at TIMESTAMPTZ DEFAULT now()
);


-- -------------------------------------
-- Cabinet Decision Management Tables
-- -------------------------------------

CREATE TABLE cabinet_paper_categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE cabinet_papers (
    id SERIAL PRIMARY KEY,
    reference_id VARCHAR(50) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    summary TEXT,
    category_id INT REFERENCES cabinet_paper_categories(id),
    confidentiality_level confidentiality_level_enum DEFAULT 'Internal',
    status cabinet_paper_status_enum NOT NULL,
    submitted_by_user_id INT NOT NULL REFERENCES users(id),
    submission_date DATE,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE cabinet_decisions (
    id SERIAL PRIMARY KEY,
    paper_id INT UNIQUE NOT NULL REFERENCES cabinet_papers(id),
    decision_text TEXT NOT NULL,
    decision_type decision_type_enum NOT NULL,
    decision_date DATE NOT NULL,
    recorded_by_user_id INT NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ DEFAULT now()
);


-- -------------------------------------
-- Supporting and Audit Tables
-- -------------------------------------

CREATE TABLE notifications (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id),
    message TEXT NOT NULL,
    related_item_type related_item_type_enum,
    related_item_id INT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    action_type VARCHAR(255) NOT NULL,
    target_resource VARCHAR(100),
    target_id INT,
    details JSONB,
    ip_address VARCHAR(45),
    created_at TIMESTAMPTZ DEFAULT now()
);


-- =================================================================
-- Triggers for automatic 'updated_at' timestamp updates
-- =================================================================

CREATE TRIGGER trigger_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_letters_updated_at
BEFORE UPDATE ON letters
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_cabinet_papers_updated_at
BEFORE UPDATE ON cabinet_papers
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- =================================================================
-- Indexes for Performance
-- =================================================================

CREATE INDEX idx_letters_status ON letters(status);
CREATE INDEX idx_letters_handler ON letters(handler_user_id);
CREATE INDEX idx_letter_events_letter_id ON letter_events(letter_id);
CREATE INDEX idx_cabinet_papers_status ON cabinet_papers(status);
CREATE INDEX idx_cabinet_papers_category ON cabinet_papers(category_id);
CREATE INDEX idx_users_role ON users(role_id);
CREATE INDEX idx_users_division ON users(division_id);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);

-- GIN index for efficient searching within JSONB columns
CREATE INDEX idx_letters_sender_details ON letters USING GIN (sender_details);
CREATE INDEX idx_letter_events_details ON letter_events USING GIN (event_details);

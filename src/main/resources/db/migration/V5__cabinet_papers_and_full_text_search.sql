-- Enable the PGroonga extension for advanced full-text search
CREATE EXTENSION IF NOT EXISTS pgroonga;

DROP TABLE IF EXISTS cabinet_decisions;
DROP TABLE IF EXISTS cabinet_papers;
DROP TYPE IF EXISTS confidentiality_level_enum;

ALTER TYPE parent_type_enum ADD VALUE 'CABINET_PAPER';

CREATE TABLE cabinet_papers
(
    id                   SERIAL PRIMARY KEY,
    reference_id         VARCHAR(50) UNIQUE        NOT NULL,
    subject              VARCHAR(255)              NOT NULL,
    summary              TEXT,
    category_id          INT REFERENCES cabinet_paper_categories (id),
    status               cabinet_paper_status_enum NOT NULL,
    submitted_by_user_id INT                       NOT NULL REFERENCES users (id),
    created_at           TIMESTAMPTZ DEFAULT now(),
    updated_at           TIMESTAMPTZ DEFAULT now(),
    deleted_at           TIMESTAMPTZ
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

-- Add full-text search capability to cabinet paper attachments
CREATE TABLE cabinet_paper_attachment_contents
(
    attachment_id INT PRIMARY KEY REFERENCES attachments (id) ON DELETE CASCADE,
    text          TEXT
);

CREATE INDEX idx_pgroonga_cabinet_paper_attachment_contents ON cabinet_paper_attachment_contents USING pgroonga (text);

-- Add full-text search capability to letter attachments
CREATE TABLE letter_attachment_contents
(
    attachment_id INT PRIMARY KEY REFERENCES attachments (id) ON DELETE CASCADE,
    text          TEXT
);

CREATE INDEX idx_pgroonga_letter_attachment_contents ON letter_attachment_contents USING pgroonga (text);

-- Create a function to perform full-text search using PGroonga
CREATE OR REPLACE FUNCTION pgroonga_match(target_text TEXT, query TEXT)
    RETURNS BOOLEAN AS
$$
BEGIN
    RETURN target_text &@~ query;
END;
$$ LANGUAGE plpgsql IMMUTABLE;
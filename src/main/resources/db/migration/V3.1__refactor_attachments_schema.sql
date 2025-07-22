DROP TABLE IF EXISTS attachments CASCADE;
DROP TYPE IF EXISTS parent_type_enum;

CREATE TYPE parent_type_enum AS ENUM (
    'LETTER',
    'LETTER_EVENT'
);

CREATE TABLE attachments (
    id SERIAL PRIMARY KEY,
    parent_type parent_type_enum NOT NULL,
    parent_id INT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_type VARCHAR(50),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

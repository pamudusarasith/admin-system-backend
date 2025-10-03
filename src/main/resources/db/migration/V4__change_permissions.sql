DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS permissions;

CREATE TABLE permission_categories
(
    id        SERIAL PRIMARY KEY,
    name      VARCHAR(100) UNIQUE NOT NULL,
    parent_id INTEGER REFERENCES permission_categories (id)
);



CREATE TABLE permissions
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100) UNIQUE NOT NULL,
    label       VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    category_id INTEGER REFERENCES permission_categories (id)
);

CREATE TABLE role_permissions
(
    role_id       INT NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    permission_id INT NOT NULL REFERENCES permissions (id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);


INSERT INTO permissions(name, description)
VALUES ('user:read', 'Permission to read users data'),
       ('user:create', 'Permission to create new users'),
       ('user:update', 'Permission to update users data'),
       ('user:delete', 'Permission to delete users data');

INSERT INTO roles(name, description)
VALUES ('admin', 'Administrator role with all permissions'),
       ('user', 'Regular user role with limited permissions');

INSERT INTO role_permissions(role_id, permission_id)
VALUES ((SELECT id FROM roles WHERE name = 'admin'), (SELECT id FROM permissions WHERE name = 'user:read')),
       ((SELECT id FROM roles WHERE name = 'admin'), (SELECT id FROM permissions WHERE name = 'user:create')),
       ((SELECT id FROM roles WHERE name = 'admin'), (SELECT id FROM permissions WHERE name = 'user:update')),
       ((SELECT id FROM roles WHERE name = 'admin'), (SELECT id FROM permissions WHERE name = 'user:delete')),
       ((SELECT id FROM roles WHERE name = 'user'), (SELECT id FROM permissions WHERE name = 'user:read'));

INSERT INTO divisions(name, description)
VALUES ('IT Division', 'Information Technology Division responsible for system management'),
       ('HR Division', 'Human Resources Division responsible for employee management'),
       ('Finance Division', 'Finance Division responsible for financial operations');

INSERT INTO users(username, password, role_id, division_id)
VALUES ('admin', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK',
        (SELECT id FROM roles WHERE name = 'admin'), (SELECT id FROM divisions WHERE name = 'IT Division'));
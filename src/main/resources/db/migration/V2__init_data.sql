INSERT INTO permission(name, description)
VALUES ('user:read', 'Permission to read users data'),
       ('user:create', 'Permission to create new users'),
       ('user:update', 'Permission to update users data'),
       ('user:delete', 'Permission to delete users data');

INSERT INTO role(name, description)
VALUES ('admin', 'Administrator role with all permissions'),
       ('user', 'Regular user role with limited permissions');

INSERT INTO role_permission(role_id, permission_id)
VALUES ((SELECT id FROM role WHERE name = 'admin'), (SELECT id FROM permission WHERE name = 'user:read')),
       ((SELECT id FROM role WHERE name = 'admin'), (SELECT id FROM permission WHERE name = 'user:create')),
       ((SELECT id FROM role WHERE name = 'admin'), (SELECT id FROM permission WHERE name = 'user:update')),
       ((SELECT id FROM role WHERE name = 'admin'), (SELECT id FROM permission WHERE name = 'user:delete')),
       ((SELECT id FROM role WHERE name = 'user'), (SELECT id FROM permission WHERE name = 'user:read'));

INSERT INTO "user"(username, password, email, role_id)
VALUES ('admin', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK', 'admin@test.com',
        (SELECT id FROM role WHERE name = 'admin'));
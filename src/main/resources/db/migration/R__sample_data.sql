-- =================================================================
-- Sample Data for Admin System Database
-- =================================================================

-- Truncate tables in correct order (respecting foreign key dependencies)
TRUNCATE TABLE users RESTART IDENTITY CASCADE;
TRUNCATE TABLE role_permissions RESTART IDENTITY CASCADE;
TRUNCATE TABLE roles RESTART IDENTITY CASCADE;
TRUNCATE TABLE permissions RESTART IDENTITY CASCADE;
TRUNCATE TABLE divisions RESTART IDENTITY CASCADE;
TRUNCATE TABLE permission_categories RESTART IDENTITY CASCADE;

-- =================================================================
-- Insert Permission Categories
-- =================================================================
INSERT INTO permission_categories (id, name, parent_id)
VALUES (1, 'User Management', NULL),
       (2, 'Letter Management', NULL),
       (3, 'Letter Reading', 2),
       (4, 'Letter Creation', 2),
       (5, 'Letter Updating', 2),
       (6, 'Letter Assignment', 2),
       (7, 'Letter Priority', 2),
       (8, 'Letter Attachments', 2),
       (9, 'Letter Completion', 2),
       (10, 'Letter Reopen', 2),
       (11, 'Letter Notes', 2),
       (12, 'Division Management', NULL),
       (13, 'General', 11);

-- =================================================================
-- Insert Permissions
-- =================================================================
INSERT INTO permissions (name, label, description, category_id)
VALUES ('user:read', 'Read Users', 'Permission to read user information', 1),
       ('user:create', 'Create Users', 'Permission to create new users', 1),
       ('user:update', 'Update Users', 'Permission to update existing users', 1),
       ('user:delete', 'Delete Users', 'Permission to delete users', 1),

       ('letter:all:read', 'Read All Letters', 'Permission to read every letter', 3),
       ('letter:unassigned:read', 'Read Unassigned Letters', 'Permission to read unassigned letters', 3),
       ('letter:division:read', 'Read Division Letters', 'Permission to read letters in own division', 3),
       ('letter:own:read', 'Read Own Letters', 'Permission to read letters assigned to self', 3),

       ('letter:create', 'Create Letters', 'Permission to create new letters', 4),

       ('letter:all:update', 'Update All Letters', 'Permission to update every letter', 5),
       ('letter:unassigned:update', 'Update Unassigned Letters', 'Permission to update unassigned letters', 5),
       ('letter:division:update', 'Update Division Letters', 'Permission to update letters in own division', 5),
       ('letter:own:update', 'Update Own Letters', 'Permission to update letters assigned to self', 5),

       ('letter:assign:division', 'Assign Letters to Divisions', 'Permission to assign letters to divisions', 6),
       ('letter:assign:user', 'Assign Letters to Users', 'Permission to assign letters to specific users', 6),

       ('letter:all:update:priority', 'Set Priority for All Letters', 'Permission to change priority on every letter',
        7),
       ('letter:unassigned:update:priority', 'Set Priority for Unassigned Letters',
        'Permission to change priority on unassigned letters', 7),
       ('letter:division:update:priority', 'Set Priority for Division Letters',
        'Permission to change priority on letters in own division', 7),
       ('letter:own:update:priority', 'Set Priority for Own Letters',
        'Permission to change priority on letters assigned to self', 7),

       ('letter:all:add:attachments', 'Add Attachments to All Letters', 'Permission to add attachments to every letter',
        8),
       ('letter:unassigned:add:attachments', 'Add Attachments to Unassigned Letters',
        'Permission to add attachments to unassigned letters', 8),
       ('letter:division:add:attachments', 'Add Attachments to Division Letters',
        'Permission to add attachments to letters in own division', 8),
       ('letter:own:add:attachments', 'Add Attachments to Own Letters',
        'Permission to add attachments to letters assigned to self', 8),

       ('letter:all:markcomplete', 'Complete All Letters', 'Permission to mark every letter as complete', 9),
       ('letter:unassigned:markcomplete', 'Complete Unassigned Letters',
        'Permission to mark unassigned letters as complete', 9),
       ('letter:division:markcomplete', 'Complete Division Letters',
        'Permission to mark letters in own division as complete', 9),
       ('letter:own:markcomplete', 'Complete Own Letters', 'Permission to mark letters assigned to self as complete',
        9),
       ('letter:all:reopen', 'Reopen All Letters', 'Permission to reopen every letter', 10),
       ('letter:unassigned:reopen', 'Reopen Unassigned Letters', 'Permission to reopen unassigned letters', 10),
       ('letter:division:reopen', 'Reopen Division Letters', 'Permission to reopen letters in own division', 10),
       ('letter:own:reopen', 'Reopen Own Letters', 'Permission to reopen letters assigned to self', 10),

       ('letter:all:add:note', 'Add Notes to All Letters', 'Permission to add notes to every letter', 11),
       ('letter:unassigned:add:note', 'Add Notes to Unassigned Letters',
        'Permission to add notes to unassigned letters', 11),
       ('letter:division:add:note', 'Add Notes to Division Letters',
        'Permission to add notes to letters in own division', 11),
       ('letter:own:add:note', 'Add Notes to Own Letters', 'Permission to add notes to letters assigned to self', 11),

       ('division:read', 'Read Divisions', 'Permission to read division information', 12),
       ('division:create', 'Create Divisions', 'Permission to create new divisions', 12),
       ('division:update', 'Update Divisions', 'Permission to update existing divisions', 12),
       ('division:delete', 'Delete Divisions', 'Permission to delete divisions', 12);

-- =================================================================
-- Insert Roles
-- =================================================================
INSERT INTO roles (name, description)
VALUES ('ADMIN', 'Administrator with all system permissions'),
       ('POSTAL_OFFICER', 'Postal officer responsible for letter intake and routing'),
       ('DIVISION_HEAD', 'Head of a division with management permissions'),
       ('SUBJECT_OFFICER', 'Subject matter expert handling specific letters'),
       ('CLERK', 'General clerk with basic permissions'),
       ('READ_ONLY', 'Read-only access for viewing purposes');

-- =================================================================
-- Insert Role Permissions
-- =================================================================
-- Admin - All permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.name = 'ADMIN';

-- Postal Officer
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.name = 'POSTAL_OFFICER'
  AND p.name IN ('letter:unassigned:read', 'letter:unassigned:update', 'letter:create', 'letter:assign:division');

-- =================================================================
-- Insert Divisions
-- =================================================================
INSERT INTO divisions (name, description)
VALUES ('IT Division', 'Information Technology Division responsible for system management and technical operations'),
       ('HR Division', 'Human Resources Division responsible for employee management and personnel matters'),
       ('Finance Division', 'Finance Division responsible for financial operations and budget management'),
       ('Legal Division', 'Legal Division handling legal matters and compliance'),
       ('Administration Division', 'General Administration Division for administrative support'),
       ('Policy Division', 'Policy Development Division for strategic planning and policy formulation'),
       ('Public Relations Division', 'Public Relations Division for external communications'),
       ('Research Division', 'Research and Development Division for analytical work'),
       ('Audit Division', 'Internal Audit Division for compliance and oversight'),
       ('Security Division', 'Security Division for safety and security matters');

-- =================================================================
-- Insert Users (with BCrypt hashed passwords)
-- =================================================================
-- Note: All passwords are hashed version of "password"
INSERT INTO users (username, email, password, full_name, phone_number, role_id, division_id, is_active,
                   account_setup_required)
VALUES

-- Administrators
('admin', 'admin@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK', 'System Administrator',
 '+94771234568',
 (SELECT id FROM roles WHERE name = 'ADMIN'),
 (SELECT id FROM divisions WHERE name = 'IT Division'),
 true, false),

('hr.admin', 'hradmin@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK', 'HR Administrator',
 '+94771234569',
 (SELECT id FROM roles WHERE name = 'ADMIN'),
 (SELECT id FROM divisions WHERE name = 'HR Division'),
 true, false),

-- Postal Officers
('postal.officer1', 'postal1@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK',
 'Kumari Silva', '+94771234570',
 (SELECT id FROM roles WHERE name = 'POSTAL_OFFICER'),
 (SELECT id FROM divisions WHERE name = 'Administration Division'),
 true, false),

('postal.officer2', 'postal2@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK',
 'Nimal Perera', '+94771234571',
 (SELECT id FROM roles WHERE name = 'POSTAL_OFFICER'),
 (SELECT id FROM divisions WHERE name = 'Administration Division'),
 true, false),

-- Division Heads
('it.head', 'ithead@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK', 'Dr. Kamal Rajapaksa',
 '+94771234572',
 (SELECT id FROM roles WHERE name = 'DIVISION_HEAD'),
 (SELECT id FROM divisions WHERE name = 'IT Division'),
 true, false),

('finance.head', 'finhead@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK',
 'Mrs. Sunitha Fernando', '+94771234573',
 (SELECT id FROM roles WHERE name = 'DIVISION_HEAD'),
 (SELECT id FROM divisions WHERE name = 'Finance Division'),
 true, false),

('legal.head', 'legalhead@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK',
 'Mr. Chaminda Wickramasinghe', '+94771234574',
 (SELECT id FROM roles WHERE name = 'DIVISION_HEAD'),
 (SELECT id FROM divisions WHERE name = 'Legal Division'),
 true, false),

('policy.head', 'policyhead@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK',
 'Prof. Manjula Weerasinghe', '+94771234575',
 (SELECT id FROM roles WHERE name = 'DIVISION_HEAD'),
 (SELECT id FROM divisions WHERE name = 'Policy Division'),
 true, false),

-- Subject Officers
('finance.officer1', 'finoff1@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK',
 'Ruwan Jayawardena', '+94771234576',
 (SELECT id FROM roles WHERE name = 'SUBJECT_OFFICER'),
 (SELECT id FROM divisions WHERE name = 'Finance Division'),
 true, false),

('legal.officer1', 'legaloff1@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK',
 'Priyanka Rathnayake', '+94771234577',
 (SELECT id FROM roles WHERE name = 'SUBJECT_OFFICER'),
 (SELECT id FROM divisions WHERE name = 'Legal Division'),
 true, false),

('policy.officer1', 'policyoff1@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK',
 'Sandun Amarasinghe', '+94771234578',
 (SELECT id FROM roles WHERE name = 'SUBJECT_OFFICER'),
 (SELECT id FROM divisions WHERE name = 'Policy Division'),
 true, false),

('research.officer1', 'resoff1@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK',
 'Dr. Nilmini Gunawardena', '+94771234579',
 (SELECT id FROM roles WHERE name = 'SUBJECT_OFFICER'),
 (SELECT id FROM divisions WHERE name = 'Research Division'),
 true, false),

('it.officer1', 'itoff1@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK',
 'Tharindu Wijesinghe', '+94771234580',
 (SELECT id FROM roles WHERE name = 'SUBJECT_OFFICER'),
 (SELECT id FROM divisions WHERE name = 'IT Division'),
 true, false),

-- Clerks
('admin.clerk1', 'clerk1@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK', 'Saman Kumara',
 '+94771234581',
 (SELECT id FROM roles WHERE name = 'CLERK'),
 (SELECT id FROM divisions WHERE name = 'Administration Division'),
 true, false),

('admin.clerk2', 'clerk2@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK', 'Chamila Perera',
 '+94771234582',
 (SELECT id FROM roles WHERE name = 'CLERK'),
 (SELECT id FROM divisions WHERE name = 'Administration Division'),
 true, false),

('hr.clerk1', 'hrclerk1@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK',
 'Anura Dissanayake', '+94771234583',
 (SELECT id FROM roles WHERE name = 'CLERK'),
 (SELECT id FROM divisions WHERE name = 'HR Division'),
 true, false),

-- Read-only users
('auditor', 'auditor@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK', 'Internal Auditor',
 '+94771234584',
 (SELECT id FROM roles WHERE name = 'READ_ONLY'),
 (SELECT id FROM divisions WHERE name = 'Audit Division'),
 true, false),

('guest.user', 'guest@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK', 'Guest User',
 '+94771234585',
 (SELECT id FROM roles WHERE name = 'READ_ONLY'),
 (SELECT id FROM divisions WHERE name = 'Administration Division'),
 true, true),

-- Inactive user for testing
('inactive.user', 'inactive@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK',
 'Inactive Test User', '+94771234586',
 (SELECT id FROM roles WHERE name = 'CLERK'),
 (SELECT id FROM divisions WHERE name = 'Administration Division'),
 false, false);
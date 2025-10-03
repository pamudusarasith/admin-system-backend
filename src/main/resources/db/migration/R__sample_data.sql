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
INSERT INTO permission_categories (id,name, parent_id) VALUES
(1,'User Management', NULL),
(2,'Letter Management', NULL),
(3,'Cabinet Paper Management', NULL),
(4,'Division Management', NULL),
(5,'User Read', 1),
(6,'User Create', 1),
(7,'User Update', 1),
(8,'User Delete', 1),
(9,'Letter Read', 2),
(10,'Letter Create', 2),
(11,'Letter Delete', 2),
(12,'Letter Assign', 2),
(13,'Cabinet Read', 3),
(14,'Cabinet Create', 3),
(15,'Cabinet Update', 3),
(16,'Cabinet Delete', 3),
(17,'Admin System', NULL),
(18,'Report View', NULL),
(19,'Audit View', NULL);
-- =================================================================
-- Insert Permissions
-- =================================================================
INSERT INTO permissions (name,label,description,category_id) VALUES
('user:read','User Read', 'Permission to read user data',1),
('user:create','User Create', 'Permission to create new users',1),
('user:update','User Update', 'Permission to update user data',1),
('user:delete','User Delete', 'Permission to delete users',1),
('letter:read:all','Letter Read All', 'Permission to read all letters',9),
('letter:read:unassigned','Letter Read Unassigned', 'Permission to read unassigned letters',9),
('letter:read:division','Letter Read Division', 'Permission to read letters assigned to own division',9),
('letter:read:own','Letter Read Own', 'Permission to read letters assigned to self',9),
('letter:create','Letter Create','Permission to create new letters',2),
('letter:update', 'Letter Update', 'Permission to update letters',2),
('letter:update:all','Letter Update All', 'Permission to update all letters',2),
('letter:update:unassigned','Letter Update Unassigned', 'Permission to update unassigned letters',2),
('letter:update:division','Letter Update Division', 'Permission to update letters assigned to own division',2),
('letter:update:own', 'Letter Update Own','Permission to update letters assigned to self',2),
('letter:delete', 'Letter Delete','Permission to delete letters',2),
('letter:assign', 'Letter Assign', 'Permission to assign letters to divisions/officers',2),
('cabinet:read', 'Cabinet Paper Read','Permission to read cabinet papers',3),
('cabinet:create', 'Cabinet Paper Create','Permission to create cabinet papers',3),
('cabinet:update','Cabinet Paper Update', 'Permission to update cabinet papers',3),
('cabinet:delete', 'Cabinet Paper Delete','Permission to delete cabinet papers',3),
('admin:system', 'Admin System','Full system administration permissions',NULL),
('report:view', 'Report View','Permission to view reports',NULL),
('audit:view', 'Audit View','Permission to view audit logs',NULL);

-- =================================================================
-- Insert Roles
-- =================================================================
INSERT INTO roles (name, description) VALUES ('ADMIN', 'Administrator with all system permissions'),
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
FROM roles r CROSS JOIN permissions p
WHERE r.name = 'ADMIN';

-- Postal Officer
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r CROSS JOIN permissions p 
WHERE r.name = 'POSTAL_OFFICER' 
AND p.name IN ('letter:read', 'letter:create', 'letter:update', 'letter:assign', 'user:read');

-- Division Head
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r CROSS JOIN permissions p 
WHERE r.name = 'DIVISION_HEAD' 
AND p.name IN ('letter:read', 'letter:update', 'letter:assign', 'cabinet:read', 'cabinet:create', 'cabinet:update', 'user:read', 'report:view');

-- Subject Officer
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r CROSS JOIN permissions p 
WHERE r.name = 'SUBJECT_OFFICER' 
AND p.name IN ('letter:read', 'letter:update', 'cabinet:read', 'user:read');

-- Clerk
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r CROSS JOIN permissions p 
WHERE r.name = 'CLERK' 
AND p.name IN ('letter:read', 'letter:create', 'user:read');

-- Read Only
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r CROSS JOIN permissions p 
WHERE r.name = 'READ_ONLY' 
AND p.name IN ('letter:read', 'cabinet:read', 'user:read', 'report:view');

-- =================================================================
-- Insert Divisions
-- =================================================================
INSERT INTO divisions (name, description) VALUES
('IT Division', 'Information Technology Division responsible for system management and technical operations'),
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
INSERT INTO users (username, email, password, full_name, phone_number, role_id, division_id, is_active, account_setup_required) VALUES

-- Administrators
('admin', 'admin@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK', 'System Administrator', '+94771234568', 
 (SELECT id FROM roles WHERE name = 'ADMIN'), 
 (SELECT id FROM divisions WHERE name = 'IT Division'), 
 true, false),

('hr.admin', 'hradmin@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK', 'HR Administrator', '+94771234569', 
 (SELECT id FROM roles WHERE name = 'ADMIN'), 
 (SELECT id FROM divisions WHERE name = 'HR Division'), 
 true, false),

-- Postal Officers
('postal.officer1', 'postal1@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK', 'Kumari Silva', '+94771234570', 
 (SELECT id FROM roles WHERE name = 'POSTAL_OFFICER'), 
 (SELECT id FROM divisions WHERE name = 'Administration Division'), 
 true, false),

('postal.officer2', 'postal2@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK', 'Nimal Perera', '+94771234571', 
 (SELECT id FROM roles WHERE name = 'POSTAL_OFFICER'), 
 (SELECT id FROM divisions WHERE name = 'Administration Division'), 
 true, false),

-- Division Heads
('it.head', 'ithead@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK', 'Dr. Kamal Rajapaksa', '+94771234572', 
 (SELECT id FROM roles WHERE name = 'DIVISION_HEAD'), 
 (SELECT id FROM divisions WHERE name = 'IT Division'), 
 true, false),

('finance.head', 'finhead@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK', 'Mrs. Sunitha Fernando', '+94771234573', 
 (SELECT id FROM roles WHERE name = 'DIVISION_HEAD'), 
 (SELECT id FROM divisions WHERE name = 'Finance Division'), 
 true, false),

('legal.head', 'legalhead@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK', 'Mr. Chaminda Wickramasinghe', '+94771234574', 
 (SELECT id FROM roles WHERE name = 'DIVISION_HEAD'), 
 (SELECT id FROM divisions WHERE name = 'Legal Division'), 
 true, false),

('policy.head', 'policyhead@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK', 'Prof. Manjula Weerasinghe', '+94771234575', 
 (SELECT id FROM roles WHERE name = 'DIVISION_HEAD'), 
 (SELECT id FROM divisions WHERE name = 'Policy Division'), 
 true, false),

-- Subject Officers
('finance.officer1', 'finoff1@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK', 'Ruwan Jayawardena', '+94771234576', 
 (SELECT id FROM roles WHERE name = 'SUBJECT_OFFICER'), 
 (SELECT id FROM divisions WHERE name = 'Finance Division'), 
 true, false),

('legal.officer1', 'legaloff1@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK', 'Priyanka Rathnayake', '+94771234577', 
 (SELECT id FROM roles WHERE name = 'SUBJECT_OFFICER'), 
 (SELECT id FROM divisions WHERE name = 'Legal Division'), 
 true, false),

('policy.officer1', 'policyoff1@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK', 'Sandun Amarasinghe', '+94771234578', 
 (SELECT id FROM roles WHERE name = 'SUBJECT_OFFICER'), 
 (SELECT id FROM divisions WHERE name = 'Policy Division'), 
 true, false),

('research.officer1', 'resoff1@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK', 'Dr. Nilmini Gunawardena', '+94771234579', 
 (SELECT id FROM roles WHERE name = 'SUBJECT_OFFICER'), 
 (SELECT id FROM divisions WHERE name = 'Research Division'), 
 true, false),

('it.officer1', 'itoff1@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK', 'Tharindu Wijesinghe', '+94771234580', 
 (SELECT id FROM roles WHERE name = 'SUBJECT_OFFICER'), 
 (SELECT id FROM divisions WHERE name = 'IT Division'), 
 true, false),

-- Clerks
('admin.clerk1', 'clerk1@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK', 'Saman Kumara', '+94771234581', 
 (SELECT id FROM roles WHERE name = 'CLERK'), 
 (SELECT id FROM divisions WHERE name = 'Administration Division'), 
 true, false),

('admin.clerk2', 'clerk2@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK', 'Chamila Perera', '+94771234582', 
 (SELECT id FROM roles WHERE name = 'CLERK'), 
 (SELECT id FROM divisions WHERE name = 'Administration Division'), 
 true, false),

('hr.clerk1', 'hrclerk1@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK', 'Anura Dissanayake', '+94771234583', 
 (SELECT id FROM roles WHERE name = 'CLERK'), 
 (SELECT id FROM divisions WHERE name = 'HR Division'), 
 true, false),

-- Read-only users
('auditor', 'auditor@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK', 'Internal Auditor', '+94771234584', 
 (SELECT id FROM roles WHERE name = 'READ_ONLY'), 
 (SELECT id FROM divisions WHERE name = 'Audit Division'), 
 true, false),

('guest.user', 'guest@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK', 'Guest User', '+94771234585', 
 (SELECT id FROM roles WHERE name = 'READ_ONLY'), 
 (SELECT id FROM divisions WHERE name = 'Administration Division'), 
 true, true),

-- Inactive user for testing
('inactive.user', 'inactive@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK', 'Inactive Test User', '+94771234586', 
 (SELECT id FROM roles WHERE name = 'CLERK'), 
 (SELECT id FROM divisions WHERE name = 'Administration Division'), 
 false, false);
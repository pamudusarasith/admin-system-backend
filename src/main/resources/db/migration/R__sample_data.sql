-- =================================================================
-- Sample Data for Admin System Database
-- =================================================================

-- Truncate tables in correct order (respecting foreign key dependencies)
TRUNCATE TABLE users RESTART IDENTITY CASCADE;
TRUNCATE TABLE role_permissions RESTART IDENTITY CASCADE;
TRUNCATE TABLE roles RESTART IDENTITY CASCADE;
TRUNCATE TABLE permissions RESTART IDENTITY CASCADE;
TRUNCATE TABLE divisions RESTART IDENTITY CASCADE;

-- =================================================================
-- Insert Permissions
-- =================================================================
INSERT INTO permissions (name, description) VALUES
('user:read', 'Permission to read user data'),
('user:create', 'Permission to create new users'),
('user:update', 'Permission to update user data'),
('user:delete', 'Permission to delete users'),
('letter:read', 'Permission to read letters'),
('letter:create', 'Permission to create new letters'),
('letter:update', 'Permission to update letters'),
('letter:delete', 'Permission to delete letters'),
('letter:assign', 'Permission to assign letters to divisions/officers'),
('cabinet:read', 'Permission to read cabinet papers'),
('cabinet:create', 'Permission to create cabinet papers'),
('cabinet:update', 'Permission to update cabinet papers'),
('cabinet:delete', 'Permission to delete cabinet papers'),
('admin:system', 'Full system administration permissions'),
('report:view', 'Permission to view reports'),
('audit:view', 'Permission to view audit logs');

-- =================================================================
-- Insert Roles
-- =================================================================
INSERT INTO roles (name, description) VALUES
('SUPER_ADMIN', 'Super administrator with all system permissions'),
('ADMIN', 'Administrator with most permissions'),
('POSTAL_OFFICER', 'Postal officer responsible for letter intake and routing'),
('DIVISION_HEAD', 'Head of a division with management permissions'),
('SUBJECT_OFFICER', 'Subject matter expert handling specific letters'),
('CLERK', 'General clerk with basic permissions'),
('READ_ONLY', 'Read-only access for viewing purposes');

-- =================================================================
-- Insert Role Permissions
-- =================================================================
-- Super Admin - All permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r CROSS JOIN permissions p 
WHERE r.name = 'SUPER_ADMIN';

-- Admin - Most permissions except super admin functions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r CROSS JOIN permissions p 
WHERE r.name = 'ADMIN' 
AND p.name IN ('user:read', 'user:create', 'user:update', 'letter:read', 'letter:create', 'letter:update', 'letter:assign', 'cabinet:read', 'cabinet:create', 'cabinet:update', 'report:view', 'audit:view');

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

-- Super Admin
('superadmin', 'superadmin@mohe.gov.lk', '$2a$10$Flj/pK//deNj6bQgVBzkv.q.K//M0tcMRFHcWVxcZKp5Q9W5QjBjK', 'System Super Administrator', '+94771234567', 
 (SELECT id FROM roles WHERE name = 'SUPER_ADMIN'), 
 (SELECT id FROM divisions WHERE name = 'IT Division'), 
 true, false),

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
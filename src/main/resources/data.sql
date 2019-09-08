INSERT INTO tenants(id, display_name, enabled, creation_date) VALUES
('zigmoi.com', 'Zigmoi, LLC', true, '2019-07-28 02:42:39');

INSERT INTO users (user_name, tenant_id, password, display_name, enabled, email, first_name, last_name, creation_date)
VALUES ('admin@zigmoi.com', 'zigmoi.com', '$2a$04$dvoJcMTDPkaQvB0slTdMBOLdinrh26jFpLZk1t04/fVJTblWtd736', 'John', true,
        'admin@zigmoi.com', 'admin', 'admin',
        '2019-07-28 02:42:39');

INSERT INTO user_roles(user_name, role)
values ('admin@zigmoi.com', 'ROLE_SUPER_ADMIN');
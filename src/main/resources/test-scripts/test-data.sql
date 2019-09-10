DELETE from tenants;
DELETE from user_roles;
DELETE from users;
DELETE from projects;

INSERT INTO tenants(id, display_name, enabled, creation_date)
VALUES ('t1.com', 'T1', true, '2019-07-28 02:42:39');

INSERT INTO tenants(id, display_name, enabled, creation_date)
VALUES ('t2.com', 'T1', true, '2019-07-28 02:42:39');


INSERT INTO users (user_name, tenant_id, password, display_name, enabled, email, first_name, last_name, creation_date)
VALUES ('admin@t1.com', 't1.com', '$2a$04$dvoJcMTDPkaQvB0slTdMBOLdinrh26jFpLZk1t04/fVJTblWtd736', 'John', true,
        'admin@t1.com', 'admin', 'admin',
        '2019-07-28 02:42:39');

INSERT INTO user_roles(user_name, role)
values ('admin@t1.com', 'ROLE_USER');

INSERT INTO users (user_name, tenant_id, password, display_name, enabled, email, first_name, last_name, creation_date)
VALUES ('admin@t2.com', 't2.com', '$2a$04$dvoJcMTDPkaQvB0slTdMBOLdinrh26jFpLZk1t04/fVJTblWtd736', 'John', true,
        'admin@t2.com', 'admin', 'admin',
        '2019-07-28 02:42:39');

INSERT INTO user_roles(user_name, role)
values ('admin@t2.com', 'ROLE_USER');

INSERT INTO users (user_name, tenant_id, password, display_name, enabled, email, first_name, last_name, creation_date)
VALUES ('u1@t1.com', 't1.com', '$2a$04$dvoJcMTDPkaQvB0slTdMBOLdinrh26jFpLZk1t04/fVJTblWtd736', 'John', true,
        'u1@t1.com', 'admin', 'admin',
        '2019-07-28 02:42:39');

INSERT INTO user_roles(user_name, role)
values ('u1@t1.com', 'ROLE_USER');

INSERT INTO users (user_name, tenant_id, password, display_name, enabled, email, first_name, last_name, creation_date)
VALUES ('u1@t2.com', 't2.com', '$2a$04$dvoJcMTDPkaQvB0slTdMBOLdinrh26jFpLZk1t04/fVJTblWtd736', 'John', true,
        'u1@t2.com', 'admin', 'admin',
        '2019-07-28 02:42:39');

INSERT INTO user_roles(user_name, role)
values ('u1@t2.com', 'ROLE_USER');


INSERT INTO `projects` (`resource_id`, `tenant_id`, `description`) VALUES ('p1', 't1.com', 'desc');
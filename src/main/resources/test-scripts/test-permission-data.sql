DELETE from project_acl;

INSERT INTO `project_acl` (`acl_rule_id`, `identity`, `permission_id`, `tenant_id`, `resource_id`, `effect`) VALUES
('1', 'admin@t1.com', 'read-project', 't1.com', '*', 'ALLOW');
INSERT INTO `project_acl` (`acl_rule_id`, `identity`, `permission_id`, `tenant_id`, `resource_id`, `effect`) VALUES
('10', 'admin@t1.com', 'assign-list-project-members', 't1.com', '*', 'ALLOW');
INSERT INTO `project_acl` (`acl_rule_id`, `identity`, `permission_id`, `tenant_id`, `resource_id`, `effect`) VALUES
('2', 'admin@t1.com', 'assign-read-project', 't1.com', '*', 'ALLOW');
INSERT INTO `project_acl` (`acl_rule_id`, `identity`, `permission_id`, `tenant_id`, `resource_id`, `effect`) VALUES
('3', 'admin@t1.com', 'update-project', 't1.com', '*', 'ALLOW');
INSERT INTO `project_acl` (`acl_rule_id`, `identity`, `permission_id`, `tenant_id`, `resource_id`, `effect`) VALUES
('4', 'admin@t1.com', 'assign-update-project', 't1.com', '*', 'ALLOW');
INSERT INTO `project_acl` (`acl_rule_id`, `identity`, `permission_id`, `tenant_id`, `resource_id`, `effect`) VALUES
('5', 'admin@t1.com', 'delete-project', 't1.com', '*', 'ALLOW');
INSERT INTO `project_acl` (`acl_rule_id`, `identity`, `permission_id`, `tenant_id`, `resource_id`, `effect`) VALUES
('6', 'admin@t1.com', 'assign-delete-project', 't1.com', '*', 'ALLOW');
INSERT INTO `project_acl` (`acl_rule_id`, `identity`, `permission_id`, `tenant_id`, `resource_id`, `effect`) VALUES
('7', 'admin@t1.com', 'create-project', 't1.com', '*', 'ALLOW');
INSERT INTO `project_acl` (`acl_rule_id`, `identity`, `permission_id`, `tenant_id`, `resource_id`, `effect`) VALUES
('8', 'admin@t1.com', 'assign-create-project', 't1.com', '*', 'ALLOW');
INSERT INTO `project_acl` (`acl_rule_id`, `identity`, `permission_id`, `tenant_id`, `resource_id`, `effect`) VALUES
('9', 'admin@t1.com', 'list-project-members', 't1.com', '*', 'ALLOW');
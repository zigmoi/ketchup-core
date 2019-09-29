INSERT INTO `project_acl` (`tenant_id`, `acl_rule_id`, `identity`, `permission_id`, `project_resource_id`, `effect`) VALUES
( 't1.com', '1', 'admin@t1.com', 'read-project', '*', 'ALLOW'),
( 't1.com', '2', 'admin@t1.com', 'assign-read-project', '*', 'ALLOW'),
( 't1.com', '3', 'admin@t1.com', 'update-project', '*', 'ALLOW'),
( 't1.com', '4', 'admin@t1.com', 'assign-update-project', '*', 'ALLOW'),
( 't1.com', '5', 'admin@t1.com', 'delete-project', '*', 'ALLOW'),
( 't1.com', '6', 'admin@t1.com', 'assign-delete-project', '*', 'ALLOW'),
( 't1.com', '7', 'admin@t1.com', 'create-project', '*', 'ALLOW'),
( 't1.com', '8', 'admin@t1.com', 'assign-create-project', '*', 'ALLOW'),
( 't1.com', '9', 'admin@t1.com', 'list-project-members', '*', 'ALLOW'),
( 't1.com', '10', 'admin@t1.com', 'assign-list-project-members', '*', 'ALLOW');
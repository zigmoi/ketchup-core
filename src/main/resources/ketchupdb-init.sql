-- -------------------------------------------------------------
-- Database: ketchupdb
-- Generation Time: 2021-01-23 22:08:33.5400
-- -------------------------------------------------------------


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


DROP TABLE IF EXISTS `application_revisions`;
CREATE TABLE `application_revisions` (
  `application_resource_id` varchar(36) NOT NULL,
  `project_resource_id` varchar(36) NOT NULL,
  `revision_resource_id` varchar(36) NOT NULL,
  `tenant_id` varchar(50) NOT NULL,
  `application_data_json` text,
  `commit_id` varchar(255) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_on` datetime DEFAULT NULL,
  `error_message` varchar(255) DEFAULT NULL,
  `helm_chart_id` varchar(255) DEFAULT NULL,
  `helm_release_id` varchar(255) DEFAULT NULL,
  `last_updated_by` varchar(255) DEFAULT NULL,
  `last_updated_on` datetime DEFAULT NULL,
  `pipeline_status_json` text,
  `status` varchar(255) DEFAULT NULL,
  `version` varchar(255) DEFAULT NULL,
  `helm_release_version` varchar(255) DEFAULT NULL,
  `original_revision_version_id` varchar(36) DEFAULT NULL,
  `rollback` bit(1) NOT NULL,
  `deployment_trigger_type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`application_resource_id`,`project_resource_id`,`revision_resource_id`,`tenant_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `applications`;
CREATE TABLE `applications` (
  `application_resource_id` varchar(36) NOT NULL,
  `project_resource_id` varchar(36) NOT NULL,
  `tenant_id` varchar(50) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_on` datetime DEFAULT NULL,
  `data` text,
  `display_name` varchar(255) DEFAULT NULL,
  `last_updated_by` varchar(255) DEFAULT NULL,
  `last_updated_on` datetime DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`application_resource_id`,`project_resource_id`,`tenant_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `deployments`;
CREATE TABLE `deployments` (
  `deployment_resource_id` varchar(36) NOT NULL,
  `project_resource_id` varchar(36) NOT NULL,
  `tenant_id` varchar(50) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_on` datetime DEFAULT NULL,
  `data` text,
  `display_name` varchar(255) DEFAULT NULL,
  `last_updated_by` varchar(255) DEFAULT NULL,
  `last_updated_on` datetime DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`deployment_resource_id`,`project_resource_id`,`tenant_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `pipeline_artifacts`;
CREATE TABLE `pipeline_artifacts` (
  `pipeline_artifact_resource_id` varchar(36) NOT NULL,
  `project_resource_id` varchar(36) NOT NULL,
  `tenant_id` varchar(50) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_on` datetime DEFAULT NULL,
  `format` varchar(255) DEFAULT NULL,
  `last_updated_by` varchar(255) DEFAULT NULL,
  `last_updated_on` datetime DEFAULT NULL,
  `resource_content` text,
  `resource_type` varchar(255) DEFAULT NULL,
  `application_resource_id` varchar(36) NOT NULL,
  `revision_resource_id` varchar(36) NOT NULL,
  PRIMARY KEY (`pipeline_artifact_resource_id`,`project_resource_id`,`tenant_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `project_acl`;
CREATE TABLE `project_acl` (
  `acl_rule_id` varchar(36) NOT NULL,
  `tenant_id` varchar(100) NOT NULL,
  `effect` varchar(255) DEFAULT NULL,
  `identity` varchar(255) DEFAULT NULL,
  `permission_id` varchar(255) DEFAULT NULL,
  `project_resource_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`acl_rule_id`,`tenant_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `project_settings`;
CREATE TABLE `project_settings` (
  `setting_resource_id` varchar(36) NOT NULL,
  `tenant_id` varchar(50) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_on` datetime DEFAULT NULL,
  `data` text,
  `display_name` varchar(255) DEFAULT NULL,
  `last_updated_by` varchar(255) DEFAULT NULL,
  `last_updated_on` datetime DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `project_resource_id` varchar(36) NOT NULL,
  PRIMARY KEY (`setting_resource_id`,`tenant_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `projects`;
CREATE TABLE `projects` (
  `resource_id` varchar(36) NOT NULL,
  `tenant_id` varchar(100) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_on` datetime DEFAULT NULL,
  `last_updated_by` varchar(255) DEFAULT NULL,
  `last_updated_on` datetime DEFAULT NULL,
  PRIMARY KEY (`resource_id`,`tenant_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `tenants`;
CREATE TABLE `tenants` (
  `id` varchar(50) NOT NULL,
  `display_name` varchar(255) DEFAULT NULL,
  `enabled` bit(1) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_on` datetime DEFAULT NULL,
  `last_updated_by` varchar(255) DEFAULT NULL,
  `last_updated_on` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `user_roles`;
CREATE TABLE `user_roles` (
  `user_name` varchar(255) NOT NULL,
  `role` varchar(255) DEFAULT NULL,
  KEY `FKscvbq0n9bo03s7w6ujfyrtg4a` (`user_name`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `user_name` varchar(255) NOT NULL,
  `display_name` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `enabled` bit(1) NOT NULL,
  `first_name` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_on` datetime DEFAULT NULL,
  `last_updated_by` varchar(255) DEFAULT NULL,
  `last_updated_on` datetime DEFAULT NULL,
  PRIMARY KEY (`user_name`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;


INSERT INTO `project_acl` (`acl_rule_id`, `tenant_id`, `effect`, `identity`, `permission_id`, `project_resource_id`) VALUES
('1', 't1.com', 'ALLOW', 'admin@t1.com', 'read-project', '*'),
('2', 't1.com', 'ALLOW', 'admin@t1.com', 'assign-read-project', '*'),
('3', 't1.com', 'ALLOW', 'admin@t1.com', 'update-project', '*'),
('4', 't1.com', 'ALLOW', 'admin@t1.com', 'assign-update-project', '*'),
('5', 't1.com', 'ALLOW', 'admin@t1.com', 'delete-project', '*'),
('6', 't1.com', 'ALLOW', 'admin@t1.com', 'assign-delete-project', '*'),
('7', 't1.com', 'ALLOW', 'admin@t1.com', 'create-project', '*'),
('8', 't1.com', 'ALLOW', 'admin@t1.com', 'assign-create-project', '*');

INSERT INTO `tenants` (`id`, `display_name`, `enabled`, `created_by`, `created_on`, `last_updated_by`, `last_updated_on`) VALUES
('zigmoi.com', 'Zigmoi, LLC', b'1', 'admin@zigmoi.com', '2019-09-04 13:03:48', 'admin@zigmoi.com', '2019-09-04 13:03:48');

INSERT INTO `user_roles` (`user_name`, `role`) VALUES
('admin@zigmoi.com', 'ROLE_SUPER_ADMIN'),
('admin@t1.com', 'ROLE_TENANT_ADMIN');

INSERT INTO `users` (`user_name`, `display_name`, `email`, `enabled`, `first_name`, `last_name`, `password`, `created_by`, `created_on`, `last_updated_by`, `last_updated_on`) VALUES
('admin@t1.com', 'Tenant Admin', 'admin@t1.com', b'1', 'Tenant Admin', 'Tenant Admin', '$2a$04$Yh6df.WoQKnoU9KNBeWIAeI1RWSPLSiLpCBESvkgUbkqF8U9VywhS', 'admin@t1.com', '2019-09-04 13:03:48', 'admin@t1.com', '2019-09-04 13:03:48'),
('admin@zigmoi.com', 'John', 'admin@zigmoi.com', b'1', 'admin', 'admin', '$2a$04$dvoJcMTDPkaQvB0slTdMBOLdinrh26jFpLZk1t04/fVJTblWtd736', 'admin@zigmoi.com', '2019-09-04 13:03:48', 'admin@zigmoi.com', '2019-09-04 13:03:48');



/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
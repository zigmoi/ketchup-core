-- -------------------------------------------------------------
-- TablePlus 2.8.2(256)
--
-- https://tableplus.com/
--
-- Database: ketchupdb
-- Generation Time: 2019-09-07 17:08:39.8310
-- -------------------------------------------------------------


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


CREATE TABLE `core_permission_meta` (
  `permission_id` varchar(255) NOT NULL,
  `permission_category` varchar(255) DEFAULT NULL,
  `permission_description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`permission_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE `deployment_acl_store` (
  `acl_rule_id` varchar(255) NOT NULL,
  `deployment_project_id` varchar(255) DEFAULT NULL,
  `deployment_resource_id` varchar(255) DEFAULT NULL,
  `deployment_tenant_id` varchar(255) DEFAULT NULL,
  `identity` varchar(255) DEFAULT NULL,
  `permission_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`acl_rule_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE `global_setting_acl_store` (
  `acl_rule_id` varchar(255) NOT NULL,
  `global_setting_resource_id` varchar(255) DEFAULT NULL,
  `global_setting_tenant_id` varchar(255) DEFAULT NULL,
  `identity` varchar(255) DEFAULT NULL,
  `permission_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`acl_rule_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE `project_acl` (
  `acl_rule_id` varchar(255) NOT NULL,
  `effect` varchar(255) DEFAULT NULL,
  `identity` varchar(255) DEFAULT NULL,
  `permission_id` varchar(255) DEFAULT NULL,
  `resource_id` varchar(36) DEFAULT NULL,
  `tenant_id` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`acl_rule_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE `project_members` (
  `resource_id` varchar(36) NOT NULL,
  `tenant_id` varchar(100) NOT NULL,
  `member` varchar(255) DEFAULT NULL,
  KEY `FKbpwmc3gtd139kfmcs675ffb6f` (`resource_id`,`tenant_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE `project_setting_acl_store` (
  `acl_rule_id` varchar(255) NOT NULL,
  `identity` varchar(255) DEFAULT NULL,
  `permission_id` varchar(255) DEFAULT NULL,
  `project_setting_project_id` varchar(255) DEFAULT NULL,
  `project_setting_resource_id` varchar(255) DEFAULT NULL,
  `project_setting_tenant_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`acl_rule_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE `projects` (
  `resource_id` varchar(36) NOT NULL,
  `tenant_id` varchar(100) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`resource_id`,`tenant_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE `tenants` (
  `id` varchar(255) NOT NULL,
  `creation_date` datetime DEFAULT NULL,
  `display_name` varchar(255) DEFAULT NULL,
  `enabled` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE `user_projects` (
  `user_name` varchar(255) NOT NULL,
  `resource_id` varchar(36) DEFAULT NULL,
  `tenant_id` varchar(100) DEFAULT NULL,
  KEY `FK53pyjgpry56yo65v49nnv82fc` (`user_name`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE `user_roles` (
  `user_name` varchar(255) NOT NULL,
  `role` varchar(255) DEFAULT NULL,
  KEY `FKscvbq0n9bo03s7w6ujfyrtg4a` (`user_name`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE `users` (
  `user_name` varchar(255) NOT NULL,
  `tenant_id` varchar(100) DEFAULT NULL,
  `creation_date` datetime DEFAULT NULL,
  `display_name` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `enabled` bit(1) NOT NULL,
  `first_name` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`user_name`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;




/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*
SQLyog Ultimate v9.50 
MySQL - 5.5.21 : Database - katana
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`katana` /*!40100 DEFAULT CHARACTER SET latin1 */;

USE `katana`;

/*Table structure for table `classes` */

DROP TABLE IF EXISTS `classes`;

CREATE TABLE `classes` (
  `class_id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `class_name` varchar(30) NOT NULL,
  `spell_1` mediumint(8) unsigned NOT NULL,
  `spell_2` mediumint(8) unsigned NOT NULL,
  `spell_3` mediumint(8) unsigned NOT NULL,
  `spell_4` mediumint(8) unsigned NOT NULL,
  `model_id` mediumint(8) unsigned NOT NULL,
  PRIMARY KEY (`class_id`),
  KEY `spell_1` (`spell_1`),
  KEY `spell_2` (`spell_2`),
  KEY `spell_3` (`spell_3`),
  KEY `spell_4` (`spell_4`),
  CONSTRAINT `classes_ibfk_1` FOREIGN KEY (`spell_1`) REFERENCES `spells` (`spell_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `classes_ibfk_2` FOREIGN KEY (`spell_2`) REFERENCES `spells` (`spell_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `classes_ibfk_3` FOREIGN KEY (`spell_3`) REFERENCES `spells` (`spell_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `classes_ibfk_4` FOREIGN KEY (`spell_4`) REFERENCES `spells` (`spell_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;

/*Table structure for table `creature_instance` */

DROP TABLE IF EXISTS `creature_instance`;

CREATE TABLE `creature_instance` (
  `map_id` mediumint(8) unsigned NOT NULL,
  `creature_id` mediumint(8) unsigned NOT NULL,
  KEY `map_id` (`map_id`),
  KEY `creature_id` (`creature_id`),
  CONSTRAINT `creature_instance_ibfk_1` FOREIGN KEY (`map_id`) REFERENCES `map_template` (`map_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `creature_instance_ibfk_2` FOREIGN KEY (`creature_id`) REFERENCES `creatures` (`creature_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `creatures` */

DROP TABLE IF EXISTS `creatures`;

CREATE TABLE `creatures` (
  `creature_id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `creature_name` varchar(30) NOT NULL,
  `health` mediumint(8) unsigned NOT NULL,
  `level` tinyint(3) unsigned NOT NULL,
  `attack_speed` mediumint(8) NOT NULL,
  `attack_damage` mediumint(8) NOT NULL,
  `move_speed` float NOT NULL,
  `model_id` mediumint(8) unsigned NOT NULL,
  `script` varchar(30) NOT NULL,
  PRIMARY KEY (`creature_id`),
  KEY `model_id` (`model_id`),
  CONSTRAINT `creatures_ibfk_1` FOREIGN KEY (`model_id`) REFERENCES `models` (`model_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;

/*Table structure for table `leaderboard` */

DROP TABLE IF EXISTS `leaderboard`;

CREATE TABLE `leaderboard` (
  `user_id` mediumint(8) unsigned NOT NULL,
  `location_id` mediumint(8) unsigned NOT NULL,
  `points` mediumint(8) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`user_id`,`location_id`),
  KEY `location_id` (`location_id`),
  CONSTRAINT `leaderboard_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `leaderboard_ibfk_2` FOREIGN KEY (`location_id`) REFERENCES `locations` (`location_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `locations` */

DROP TABLE IF EXISTS `locations`;

CREATE TABLE `locations` (
  `location_id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `location_name` varchar(255) NOT NULL,
  `latitude` double NOT NULL,
  `longitude` double NOT NULL,
  `radius` double NOT NULL,
  PRIMARY KEY (`location_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;

/*Table structure for table `map_template` */

DROP TABLE IF EXISTS `map_template`;

CREATE TABLE `map_template` (
  `map_id` mediumint(8) unsigned NOT NULL,
  `location_id` mediumint(8) unsigned NOT NULL,
  `map_name` text NOT NULL,
  `background` text NOT NULL,
  PRIMARY KEY (`map_id`,`location_id`),
  KEY `location_id` (`location_id`),
  CONSTRAINT `map_template_ibfk_1` FOREIGN KEY (`location_id`) REFERENCES `locations` (`location_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `models` */

DROP TABLE IF EXISTS `models`;

CREATE TABLE `models` (
  `model_id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `file` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`model_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=latin1;

/*Table structure for table `spells` */

DROP TABLE IF EXISTS `spells`;

CREATE TABLE `spells` (
  `spell_id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `spell_name` varchar(255) NOT NULL,
  `damage` mediumint(11) NOT NULL DEFAULT '0',
  `cooldown` mediumint(11) unsigned NOT NULL,
  PRIMARY KEY (`spell_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=latin1;

/*Table structure for table `users` */

DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
  `user_id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(32) NOT NULL,
  `password` varchar(128) NOT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5692 DEFAULT CHARSET=latin1;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

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

/*Data for the table `classes` */

LOCK TABLES `classes` WRITE;

insert  into `classes`(`class_id`,`class_name`,`spell_1`,`spell_2`,`spell_3`,`spell_4`,`model_id`) values (1,'HEALER',1,2,3,4,1),(2,'DAMAGE',5,6,7,8,2);

UNLOCK TABLES;

/*Data for the table `creature_instance` */

LOCK TABLES `creature_instance` WRITE;

insert  into `creature_instance`(`map_id`,`creature_id`) values (1,1),(2,1);

UNLOCK TABLES;

/*Data for the table `creatures` */

LOCK TABLES `creatures` WRITE;

insert  into `creatures`(`creature_id`,`creature_name`,`health`,`level`,`attack_speed`,`attack_damage`,`move_speed`,`model_id`,`script`) values (1,'Professor',5000,2,1,1,1,3,'GenericAI'),(2,'Watermelon',5000,2,1,1,1,5,'DoNothingAI'),(3,'creature 3',5000,2,1,1,1,5,'GenericAI');

UNLOCK TABLES;

/*Data for the table `leaderboard` */

LOCK TABLES `leaderboard` WRITE;

UNLOCK TABLES;

/*Data for the table `locations` */

LOCK TABLES `locations` WRITE;

insert  into `locations`(`location_id`,`location_name`,`latitude`,`longitude`,`radius`) values (1,'ENGINEERING',43.65775073741513,-79.37644600868225,0.007),(2,'LIBRARY',43.657971955708184,-79.3805980682373,0.000225);

UNLOCK TABLES;

/*Data for the table `map_template` */

LOCK TABLES `map_template` WRITE;

insert  into `map_template`(`map_id`,`location_id`,`map_name`,`background`) values (1,1,'ENGINEERING','ryerson_game_bg.png'),(2,2,'NOTEBOOK','notebook_game_bg.png');

UNLOCK TABLES;

/*Data for the table `models` */

LOCK TABLES `models` WRITE;

insert  into `models`(`model_id`,`file`) values (1,'healer.png'),(2,'attack.png'),(3,'ryeboss.png'),(4,'clip.png'),(5,'watermelon.png'),(6,'eggy.png');

UNLOCK TABLES;

/*Data for the table `spells` */

LOCK TABLES `spells` WRITE;

insert  into `spells`(`spell_id`,`spell_name`,`damage`,`cooldown`) values (1,'Heal 1',0,5000),(2,'Heal 2',0,3500),(3,'Heal 3',0,6000),(4,'Heal 4',0,2500),(5,'Damage 1',0,5000),(6,'Damage 2',0,6000),(7,'Damage 3',0,7000),(8,'Damage 4',0,7500);

UNLOCK TABLES;

/*Data for the table `users` */

LOCK TABLES `users` WRITE;

UNLOCK TABLES;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- MySQL dump 10.13  Distrib 5.7.17, for Linux (x86_64)
--
-- Host: localhost    Database: ForumDB
-- ------------------------------------------------------
-- Server version	5.7.17-0ubuntu0.16.04.1

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

--
-- Table structure for table `Follow`
--

DROP TABLE IF EXISTS `Follow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Follow` (
  `follower` varchar(127) NOT NULL,
  `followee` varchar(127) NOT NULL,
  UNIQUE KEY `follower` (`follower`,`followee`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Forum`
--

DROP TABLE IF EXISTS `Forum`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Forum` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `short_name` varchar(127) DEFAULT NULL,
  `user` varchar(30) DEFAULT NULL,
  `likes` bigint(20) NOT NULL DEFAULT '0',
  `dislikes` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  UNIQUE KEY `short_name` (`short_name`),
  KEY `user` (`user`),
  CONSTRAINT `Forum_ibfk_1` FOREIGN KEY (`user`) REFERENCES `User` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Post`
--

DROP TABLE IF EXISTS `Post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Post` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `thread` bigint(20) DEFAULT NULL,
  `forum` varchar(127) DEFAULT NULL,
  `user` varchar(127) DEFAULT NULL,
  `message` mediumtext,
  `date` datetime DEFAULT NULL,
  `parent` bigint(20) NOT NULL DEFAULT '0',
  `isApproved` tinyint(1) DEFAULT '0',
  `isHighlighted` tinyint(1) DEFAULT '0',
  `isEdited` tinyint(1) DEFAULT '0',
  `isSpam` tinyint(1) DEFAULT '0',
  `isDeleted` tinyint(1) DEFAULT '0',
  `likes` bigint(20) DEFAULT '0',
  `dislikes` bigint(20) NOT NULL DEFAULT '0',
  `path` varchar(255) DEFAULT NULL,
  `root_parent` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `thread` (`thread`),
  KEY `forum` (`forum`),
  KEY `user` (`user`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Subscription`
--

DROP TABLE IF EXISTS `Subscription`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Subscription` (
  `thread` bigint(20) DEFAULT NULL,
  `user` varchar(127) DEFAULT NULL,
  UNIQUE KEY `thread` (`thread`,`user`),
  KEY `user` (`user`),
  CONSTRAINT `Subscription_ibfk_1` FOREIGN KEY (`thread`) REFERENCES `Thread` (`id`),
  CONSTRAINT `Subscription_ibfk_2` FOREIGN KEY (`user`) REFERENCES `User` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Thread`
--

DROP TABLE IF EXISTS `Thread`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Thread` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user` varchar(127) DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  `title` mediumtext,
  `slug` mediumtext,
  `message` mediumtext,
  `isClosed` tinyint(1) DEFAULT NULL,
  `isDeleted` tinyint(1) DEFAULT NULL,
  `forum` varchar(127) NOT NULL,
  `likes` bigint(20) NOT NULL DEFAULT '0',
  `dislikes` bigint(20) NOT NULL DEFAULT '0',
  `posts` bigint(20) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `user` (`user`),
  KEY `Thread_Forum_short_name_fk` (`forum`),
  CONSTRAINT `Thread_Forum_short_name_fk` FOREIGN KEY (`forum`) REFERENCES `Forum` (`short_name`),
  CONSTRAINT `Thread_ibfk_2` FOREIGN KEY (`user`) REFERENCES `User` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `User`
--

DROP TABLE IF EXISTS `User`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `User` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `email` varchar(127) NOT NULL,
  `username` varchar(127) DEFAULT NULL,
  `about` text,
  `name` varchar(127) DEFAULT NULL,
  `isAnonymous` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-01-25 13:16:20

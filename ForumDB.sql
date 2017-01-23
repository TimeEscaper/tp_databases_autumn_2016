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
-- Dumping data for table `Follow`
--

LOCK TABLES `Follow` WRITE;
/*!40000 ALTER TABLE `Follow` DISABLE KEYS */;
/*!40000 ALTER TABLE `Follow` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `Forum`
--

LOCK TABLES `Forum` WRITE;
/*!40000 ALTER TABLE `Forum` DISABLE KEYS */;
INSERT INTO `Forum` VALUES (1,'Forum With Sufficiently Large Name','forumwithsufficientlylargename','example3@mail.ru',0,0),(2,'Forum I','forum1','richard.nixon@example.com',0,0),(3,'Forum II','forum2','richard.nixon@example.com',0,0),(4,'Форум Три','forum3','richard.nixon@example.com',0,0);
/*!40000 ALTER TABLE `Forum` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `Post`
--

LOCK TABLES `Post` WRITE;
/*!40000 ALTER TABLE `Post` DISABLE KEYS */;
INSERT INTO `Post` VALUES (1,1,'forum2','example@mail.ru','my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0','2015-10-19 16:53:55',0,1,0,0,1,0,0,0,'/00001',1),(2,1,'forum2','example2@mail.ru','my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1','2016-04-23 22:55:37',0,1,0,0,0,0,0,0,'/00002',2),(3,1,'forum2','example3@mail.ru','my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2','2016-12-22 14:40:22',1,1,0,1,1,0,0,0,'/00001/00003',1),(4,1,'forum2','example3@mail.ru','my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3','2017-01-01 22:26:15',0,0,1,1,1,0,0,0,'/00004',4),(5,1,'forum2','example3@mail.ru','my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4','2017-01-11 11:42:10',4,0,0,0,0,0,0,0,'/00004/00005',4),(6,1,'forum2','example2@mail.ru','my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5','2017-01-23 05:53:26',0,1,1,1,1,0,0,0,'/00006',6),(7,1,'forum2','example@mail.ru','my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6','2017-01-23 07:06:33',0,0,1,1,1,0,0,0,'/00007',7),(8,2,'forumwithsufficientlylargename','example4@mail.ru','my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0','2016-12-04 04:21:53',0,0,1,1,0,0,0,0,'/00008',8),(9,2,'forumwithsufficientlylargename','example3@mail.ru','my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1','2016-12-08 15:42:32',0,1,0,0,1,0,0,0,'/00009',9),(10,2,'forumwithsufficientlylargename','richard.nixon@example.com','my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2','2016-12-10 18:22:32',0,0,0,0,1,0,0,0,'/0000a',10),(11,2,'forumwithsufficientlylargename','example4@mail.ru','my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3','2017-01-17 19:51:32',0,1,0,1,0,0,0,0,'/0000b',11),(12,2,'forumwithsufficientlylargename','example2@mail.ru','my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4','2017-01-21 03:41:55',10,1,1,1,0,0,0,0,'/0000a/0000c',10),(13,2,'forumwithsufficientlylargename','richard.nixon@example.com','my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5','2017-01-23 09:18:20',8,0,0,0,1,0,0,0,'/00008/0000d',8),(14,2,'forumwithsufficientlylargename','example@mail.ru','my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6','2017-01-23 16:21:20',10,0,0,1,1,0,0,0,'/0000a/0000e',10),(15,2,'forumwithsufficientlylargename','example2@mail.ru','my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7','2017-01-23 18:38:12',0,1,0,1,1,0,0,0,'/0000f',15),(16,2,'forumwithsufficientlylargename','example3@mail.ru','my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8','2017-01-23 21:22:59',0,0,1,0,1,0,0,0,'/0000g',16),(17,2,'forumwithsufficientlylargename','example2@mail.ru','my message 9my message 9my message 9my message 9my message 9my message 9my message 9my message 9my message 9my message 9my message 9','2017-01-23 21:27:16',0,0,1,0,1,0,0,0,'/0000h',17),(18,3,'forum1','example2@mail.ru','my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0','2014-01-14 02:40:03',0,0,0,1,1,0,0,0,'/0000i',18),(19,3,'forum1','example2@mail.ru','my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1','2014-05-10 11:23:36',0,0,0,1,0,0,0,0,'/0000j',19),(20,3,'forum1','example@mail.ru','my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2','2016-09-26 23:55:48',19,0,0,0,1,0,0,0,'/0000j/0000k',19),(21,3,'forum1','example3@mail.ru','my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3','2017-01-01 11:37:14',20,0,0,1,1,0,0,1,'/0000j/0000k/0000l',19),(22,3,'forum1','example3@mail.ru','my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4','2017-01-18 14:26:19',18,0,1,0,1,0,0,0,'/0000i/0000m',18),(23,3,'forum1','example@mail.ru','my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5','2017-01-23 05:54:21',18,0,1,1,0,0,0,0,'/0000i/0000n',18),(24,3,'forum1','example4@mail.ru','my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6','2017-01-23 13:05:09',0,0,0,0,0,0,0,0,'/0000o',24),(25,3,'forum1','example2@mail.ru','my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7','2017-01-23 14:40:10',23,1,1,0,0,0,0,0,'/0000i/0000n/0000p',18),(26,4,'forum1','richard.nixon@example.com','my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0','2014-08-18 21:27:59',0,0,0,1,0,0,0,0,'/0000q',26),(27,4,'forum1','example2@mail.ru','my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1','2015-04-29 04:33:47',26,0,1,0,0,0,0,0,'/0000q/0000r',26),(28,4,'forum1','example4@mail.ru','my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2','2015-05-18 14:13:24',27,1,1,0,0,0,0,0,'/0000q/0000r/0000s',26),(29,4,'forum1','example2@mail.ru','my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3','2016-01-01 02:25:01',27,0,0,0,1,0,0,0,'/0000q/0000r/0000t',26),(30,4,'forum1','example2@mail.ru','my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4','2016-06-03 06:32:41',28,0,1,0,1,0,0,0,'/0000q/0000r/0000s/0000u',26),(31,4,'forum1','richard.nixon@example.com','my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5','2016-10-12 23:11:52',30,0,1,0,1,0,0,0,'/0000q/0000r/0000s/0000u/0000v',26),(32,4,'forum1','example4@mail.ru','my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6','2016-11-26 11:11:22',0,1,1,0,0,0,0,0,'/0000w',32),(33,4,'forum1','example2@mail.ru','my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7','2017-01-13 18:01:53',26,0,0,1,0,0,0,0,'/0000q/0000x',26),(34,4,'forum1','example3@mail.ru','my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8','2017-01-17 05:12:13',0,1,0,1,1,0,0,0,'/0000y',34);
/*!40000 ALTER TABLE `Post` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `Subscription`
--

LOCK TABLES `Subscription` WRITE;
/*!40000 ALTER TABLE `Subscription` DISABLE KEYS */;
INSERT INTO `Subscription` VALUES (1,'example2@mail.ru'),(2,'example3@mail.ru'),(2,'richard.nixon@example.com'),(4,'example2@mail.ru');
/*!40000 ALTER TABLE `Subscription` ENABLE KEYS */;
UNLOCK TABLES;

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
  PRIMARY KEY (`id`),
  KEY `user` (`user`),
  KEY `Thread_Forum_short_name_fk` (`forum`),
  CONSTRAINT `Thread_Forum_short_name_fk` FOREIGN KEY (`forum`) REFERENCES `Forum` (`short_name`),
  CONSTRAINT `Thread_ibfk_2` FOREIGN KEY (`user`) REFERENCES `User` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Thread`
--

LOCK TABLES `Thread` WRITE;
/*!40000 ALTER TABLE `Thread` DISABLE KEYS */;
INSERT INTO `Thread` VALUES (1,'example3@mail.ru','2014-01-01 00:00:01','Thread With Sufficiently Large Title','newslug','hey hey hey hey!',0,0,'forum2',0,0),(2,'example2@mail.ru','2013-12-31 00:01:01','Thread I','thread1','hey!',0,0,'forumwithsufficientlylargename',0,0),(3,'richard.nixon@example.com','2013-12-30 00:01:01','Thread II','thread2','hey hey!',0,0,'forum1',0,1),(4,'example@mail.ru','2013-12-29 00:01:01','Тред Три','thread3','hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! ',0,0,'forum1',0,0);
/*!40000 ALTER TABLE `Thread` ENABLE KEYS */;
UNLOCK TABLES;

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

--
-- Dumping data for table `User`
--

LOCK TABLES `User` WRITE;
/*!40000 ALTER TABLE `User` DISABLE KEYS */;
INSERT INTO `User` VALUES (1,'example@mail.ru','user1','hello im user1','John',0),(2,'richard.nixon@example.com',NULL,NULL,NULL,1),(3,'example2@mail.ru','user2','Wowowowow','NewName',0),(4,'example3@mail.ru','user3','Wowowowow!!!','NewName2',0),(5,'example4@mail.ru','user4','hello im user4','Jim',0);
/*!40000 ALTER TABLE `User` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-01-23 23:12:28

-- MySQL dump 10.11
--
-- Host: localhost    Database: qmaclone
-- ------------------------------------------------------
-- Server version	5.0.67-modified

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
-- Table structure for table `bbs_response`
--

DROP TABLE IF EXISTS `bbs_response`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `bbs_response` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `threadId` bigint(20) unsigned NOT NULL,
  `name` text NOT NULL,
  `userCode` int(11) NOT NULL,
  `machineIp` text NOT NULL,
  `dispInfo` int(11) NOT NULL,
  `postTime` bigint(20) NOT NULL,
  `body` text NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `threadId` (`threadId`)
) ENGINE=MyISAM AUTO_INCREMENT=378 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `bbs_response`
--

LOCK TABLES `bbs_response` WRITE;
/*!40000 ALTER TABLE `bbs_response` DISABLE KEYS */;
/*!40000 ALTER TABLE `bbs_response` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bbs_thread`
--

DROP TABLE IF EXISTS `bbs_thread`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `bbs_thread` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `lastUpdate` bigint(20) NOT NULL,
  `title` text NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `lastUpdateIndex` (`lastUpdate`)
) ENGINE=MyISAM AUTO_INCREMENT=29 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `bbs_thread`
--

LOCK TABLES `bbs_thread` WRITE;
/*!40000 ALTER TABLE `bbs_thread` DISABLE KEYS */;
/*!40000 ALTER TABLE `bbs_thread` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_log`
--

DROP TABLE IF EXISTS `chat_log`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `chat_log` (
  `RES_ID` int(11) NOT NULL auto_increment,
  `DATE` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `NAME` text NOT NULL,
  `BODY` text NOT NULL,
  `CLASS_LEVEL` int(11) NOT NULL,
  `USER_CODE` int(11) NOT NULL,
  `MACHINE_IP` text NOT NULL,
  PRIMARY KEY  (`RES_ID`)
) ENGINE=MyISAM AUTO_INCREMENT=1676761 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `chat_log`
--

LOCK TABLES `chat_log` WRITE;
/*!40000 ALTER TABLE `chat_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `chat_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `correct_count`
--

DROP TABLE IF EXISTS `correct_count`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `correct_count` (
  `USER_CODE` int(10) unsigned NOT NULL,
  `GENRE` int(11) NOT NULL,
  `TYPE` int(11) NOT NULL,
  `GOOD` int(10) unsigned NOT NULL default '0',
  `BAD` int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (`USER_CODE`,`GENRE`,`TYPE`),
  KEY `USER_CODE` (`USER_CODE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `correct_count`
--

LOCK TABLES `correct_count` WRITE;
/*!40000 ALTER TABLE `correct_count` DISABLE KEYS */;
/*!40000 ALTER TABLE `correct_count` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `creation_log`
--

DROP TABLE IF EXISTS `creation_log`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `creation_log` (
  `PROBLEM_ID` int(10) unsigned NOT NULL,
  `USER_CODE` int(10) unsigned NOT NULL,
  `DATE` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `MACHINE_IP` text NOT NULL,
  KEY `PROBLEM_ID` (`PROBLEM_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `creation_log`
--

LOCK TABLES `creation_log` WRITE;
/*!40000 ALTER TABLE `creation_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `creation_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ignore_id`
--

DROP TABLE IF EXISTS `ignore_id`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `ignore_id` (
  `USER_CODE` int(10) unsigned NOT NULL,
  `TARGET_USER_CODE` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`USER_CODE`,`TARGET_USER_CODE`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `ignore_id`
--

LOCK TABLES `ignore_id` WRITE;
/*!40000 ALTER TABLE `ignore_id` DISABLE KEYS */;
/*!40000 ALTER TABLE `ignore_id` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `limited_ip`
--

DROP TABLE IF EXISTS `limited_ip`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `limited_ip` (
  `IP` varchar(64) NOT NULL,
  PRIMARY KEY  (`IP`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `limited_ip`
--

LOCK TABLES `limited_ip` WRITE;
/*!40000 ALTER TABLE `limited_ip` DISABLE KEYS */;
/*!40000 ALTER TABLE `limited_ip` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `limited_user_code`
--

DROP TABLE IF EXISTS `limited_user_code`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `limited_user_code` (
  `USER_CODE` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`USER_CODE`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `limited_user_code`
--

LOCK TABLES `limited_user_code` WRITE;
/*!40000 ALTER TABLE `limited_user_code` DISABLE KEYS */;
/*!40000 ALTER TABLE `limited_user_code` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `link`
--

DROP TABLE IF EXISTS `link`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `link` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `lastUpdate` bigint(20) NOT NULL,
  `homePageName` text NOT NULL,
  `authorName` text NOT NULL,
  `url` text NOT NULL,
  `bannerUrl` text NOT NULL,
  `description` text NOT NULL,
  `userCode` int(11) NOT NULL,
  `valid` tinyint(1) NOT NULL default '1',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `lastUpdateIndex` (`lastUpdate`)
) ENGINE=MyISAM AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `link`
--

LOCK TABLES `link` WRITE;
/*!40000 ALTER TABLE `link` DISABLE KEYS */;
/*!40000 ALTER TABLE `link` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `page_view`
--

DROP TABLE IF EXISTS `page_view`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `page_view` (
  `TYPE` varchar(16) NOT NULL,
  `COUNT` int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (`TYPE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `page_view`
--

LOCK TABLES `page_view` WRITE;
/*!40000 ALTER TABLE `page_view` DISABLE KEYS */;
/*!40000 ALTER TABLE `page_view` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `player`
--

DROP TABLE IF EXISTS `player`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `player` (
  `USER_CODE` int(10) unsigned NOT NULL,
  `NAME` varchar(6) NOT NULL,
  `GREETING` varchar(10) NOT NULL,
  `HIGH_SCORE` int(10) unsigned NOT NULL,
  `AVERAGE_SCORE` int(10) unsigned NOT NULL,
  `PLAY_COUNT` int(10) unsigned NOT NULL,
  `VICTORY_POINT` int(11) NOT NULL,
  `LEVEL_NAME` int(10) unsigned NOT NULL,
  `LEVEL_NUMBER` int(10) unsigned NOT NULL,
  `AVERAGE_RANK` float unsigned NOT NULL,
  `GENRE` int(10) unsigned NOT NULL,
  `TYPE` int(10) unsigned NOT NULL,
  `CLASS_LEVEL` int(10) unsigned NOT NULL,
  `IMAGE_FILE_NAME` varchar(16) NOT NULL default 'noimage.jpg',
  `PLAY_SOUND` tinyint(1) NOT NULL default '0',
  `MULTI_GENRE` tinyint(1) NOT NULL default '0',
  `MULTI_TYPE` tinyint(1) NOT NULL default '0',
  `DIFFICULT` tinyint(1) NOT NULL default '0',
  `DIFFICULT_SELECT` int(11) NOT NULL default '0',
  `RANKING_MOVE` tinyint(1) NOT NULL default '1',
  `LAST_LOGIN` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `BBS_DISP_INFO` int(11) NOT NULL default '2',
  `BBS_AGE` tinyint(1) NOT NULL default '1',
  `TIMER_MODE` int(11) NOT NULL default '0',
  `PREFECTURE` int(11) NOT NULL default '0',
  `CHAT` tinyint(1) NOT NULL default '1',
  `NEW_PROBLEM` tinyint(1) default '1',
  `PUBLIC_EVENT` tinyint(1) default '1',
  PRIMARY KEY  (`USER_CODE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `player`
--

LOCK TABLES `player` WRITE;
/*!40000 ALTER TABLE `player` DISABLE KEYS */;
/*!40000 ALTER TABLE `player` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `player_answer`
--

DROP TABLE IF EXISTS `player_answer`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `player_answer` (
  `PROBLEM_ID` int(11) NOT NULL,
  `ANSWER` varchar(255) NOT NULL,
  `COUNT` int(11) NOT NULL default '0',
  PRIMARY KEY  (`PROBLEM_ID`,`ANSWER`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `player_answer`
--

LOCK TABLES `player_answer` WRITE;
/*!40000 ALTER TABLE `player_answer` DISABLE KEYS */;
/*!40000 ALTER TABLE `player_answer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `problem`
--

DROP TABLE IF EXISTS `problem`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `problem` (
  `ID` int(11) NOT NULL,
  `GENRE` int(11) NOT NULL,
  `TYPE` int(11) NOT NULL,
  `SENTENCE` text NOT NULL,
  `ANSWER0` text NOT NULL,
  `ANSWER1` text NOT NULL,
  `ANSWER2` text NOT NULL,
  `ANSWER3` text NOT NULL,
  `CHOICE0` text NOT NULL,
  `CHOICE1` text NOT NULL,
  `CHOICE2` text NOT NULL,
  `CHOICE3` text NOT NULL,
  `GOOD` int(11) NOT NULL,
  `BAD` int(11) NOT NULL,
  `CREATER` text NOT NULL,
  `NOTE` text NOT NULL,
  `IMAGE_ANSWER` tinyint(1) NOT NULL default '0',
  `IMAGE_CHOICE` tinyint(1) NOT NULL default '0',
  `RANDOM_FLAG` int(11) NOT NULL default '5',
  `VOTE_GOOD` int(10) unsigned default '0',
  `VOTE_BAD` int(10) unsigned default '0',
  PRIMARY KEY  (`ID`),
  FULLTEXT KEY `index_problem_ngram` USING NGRAM, NO NORMALIZE, 7 (`SENTENCE`,`ANSWER0`,`ANSWER1`,`ANSWER2`,`ANSWER3`,`CHOICE0`,`CHOICE1`,`CHOICE2`,`CHOICE3`,`NOTE`),
  FULLTEXT KEY `index_creater` USING NGRAM, NORMALIZE, 512 (`CREATER`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `problem`
--

LOCK TABLES `problem` WRITE;
/*!40000 ALTER TABLE `problem` DISABLE KEYS */;
/*!40000 ALTER TABLE `problem` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `problem_questionnaire`
--

DROP TABLE IF EXISTS `problem_questionnaire`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `problem_questionnaire` (
  `problemId` int(11) NOT NULL,
  `text` text NOT NULL,
  `date` bigint(20) NOT NULL,
  `deleted` tinyint(1) NOT NULL default '0',
  KEY `indexProblemId` (`problemId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `problem_questionnaire`
--

LOCK TABLES `problem_questionnaire` WRITE;
/*!40000 ALTER TABLE `problem_questionnaire` DISABLE KEYS */;
/*!40000 ALTER TABLE `problem_questionnaire` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rating_history`
--

DROP TABLE IF EXISTS `rating_history`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `rating_history` (
  `USER_CODE` int(11) NOT NULL,
  `TIME` bigint(20) NOT NULL,
  `RATING` int(11) NOT NULL,
  KEY `USER_CODE` (`USER_CODE`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `rating_history`
--

LOCK TABLES `rating_history` WRITE;
/*!40000 ALTER TABLE `rating_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `rating_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `report_problem`
--

DROP TABLE IF EXISTS `report_problem`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `report_problem` (
  `USER_CODE` int(10) unsigned NOT NULL,
  `PROBLEM_ID` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`USER_CODE`,`PROBLEM_ID`),
  KEY `USER_CODE` (`USER_CODE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `report_problem`
--

LOCK TABLES `report_problem` WRITE;
/*!40000 ALTER TABLE `report_problem` DISABLE KEYS */;
/*!40000 ALTER TABLE `report_problem` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `theme_mode`
--

DROP TABLE IF EXISTS `theme_mode`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `theme_mode` (
  `THEME` text NOT NULL,
  `QUERY` text NOT NULL,
  PRIMARY KEY  (`THEME`(16),`QUERY`(16))
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `theme_mode`
--

LOCK TABLES `theme_mode` WRITE;
/*!40000 ALTER TABLE `theme_mode` DISABLE KEYS */;
/*!40000 ALTER TABLE `theme_mode` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `theme_mode_score`
--

DROP TABLE IF EXISTS `theme_mode_score`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `theme_mode_score` (
  `THEME` text NOT NULL,
  `USER_CODE` int(11) NOT NULL default '0',
  `SCORE` int(11) default NULL,
  PRIMARY KEY  (`THEME`(16),`USER_CODE`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `theme_mode_score`
--

LOCK TABLES `theme_mode_score` WRITE;
/*!40000 ALTER TABLE `theme_mode_score` DISABLE KEYS */;
/*!40000 ALTER TABLE `theme_mode_score` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `user` (
  `ID` int(11) NOT NULL,
  `NAME` varchar(8) NOT NULL,
  `HIGH_SCORE` int(11) NOT NULL,
  `AVERAGE_SCORE` int(11) NOT NULL,
  `PLAY_COUNT` int(11) NOT NULL,
  `VICTORY_POINT` int(11) NOT NULL,
  `LEVEL` int(11) NOT NULL,
  `LEVEL_NUMBER` int(11) NOT NULL,
  `AVERAGE_RANK` float NOT NULL,
  `GENRE` int(11) NOT NULL,
  `GREETING` varchar(10) NOT NULL,
  `CLASS_LEVEL` int(11) NOT NULL,
  `REPORT_PROBLEMS` text NOT NULL,
  PRIMARY KEY  (`ID`),
  KEY `ID` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2009-03-18  2:47:57

CREATE TABLE theme_mode_editor (
	userCode INT PRIMARY KEY,
	status INT
);

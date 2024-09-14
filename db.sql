-- MySQL dump 10.13  Distrib 8.0.19, for Win64 (x86_64)
--
-- Host: 192.168.1.3    Database: SGCloginer
-- ------------------------------------------------------
-- Server version	8.0.37-0ubuntu0.22.04.3

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `Announcement`
--

DROP TABLE IF EXISTS `Announcement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Announcement` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '公告主键',
  `content` text COMMENT '公告内容',
  `create_date` datetime DEFAULT NULL COMMENT '发布日期',
  `deadline_date` datetime DEFAULT NULL COMMENT '截止日期',
  `level` varchar(255) DEFAULT NULL COMMENT '重要等级，由高到低为high important normal low',
  `img_url` varchar(255) DEFAULT NULL COMMENT '公告展示图片',
  `type` enum('both','loginer','web') NOT NULL DEFAULT 'both',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `AttackLog`
--

DROP TABLE IF EXISTS `AttackLog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `AttackLog` (
  `id` int DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `date` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `GroupUser`
--

DROP TABLE IF EXISTS `GroupUser`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `GroupUser` (
  `GroupId` varchar(11) NOT NULL,
  `UserId` varchar(8) NOT NULL,
  `JoinMark` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否在申请阶段',
  `GroupRole` enum('judge','normal') NOT NULL DEFAULT 'normal',
  PRIMARY KEY (`GroupId`,`UserId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `RaceGroup`
--

DROP TABLE IF EXISTS `RaceGroup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `RaceGroup` (
  `GroupId` varchar(11) NOT NULL COMMENT '比赛组id',
  `Host` varchar(8) DEFAULT NULL COMMENT '比赛组主持人',
  `GroupName` varchar(30) NOT NULL COMMENT '比赛组组名',
  `BanElfList` json DEFAULT NULL COMMENT '公ban精灵列表',
  `PickElfList` json DEFAULT NULL COMMENT '限定pick精灵列表',
  `EnableBan` varchar(10) NOT NULL DEFAULT 'forbid',
  `EnablePool` varchar(10) NOT NULL DEFAULT 'forbid',
  `LimitPool` json DEFAULT NULL,
  `BanPickFlow` json DEFAULT NULL,
  `HidePick` varchar(7) DEFAULT NULL,
  `ThirdMark` enum('enable','forbid') DEFAULT 'enable',
  `AwardPool` json DEFAULT NULL,
  `LimitMarks` text,
  `MarkStone` enum('enable','forbid') DEFAULT 'enable',
  `DisabledSuits` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci,
  PRIMARY KEY (`GroupId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `SeerElf`
--

DROP TABLE IF EXISTS `SeerElf`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `SeerElf` (
  `ID` int NOT NULL,
  `DefName` varchar(255) NOT NULL,
  `TypeID` int DEFAULT NULL,
  `LevelStar` int DEFAULT NULL,
  `PickedNum` int NOT NULL DEFAULT '0',
  `BannedNum` int NOT NULL DEFAULT '0',
  `PositionLeft` varchar(255) DEFAULT NULL,
  `PositionTop` varchar(255) DEFAULT NULL,
  `effectID` int DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `elfId` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserConfig`
--

DROP TABLE IF EXISTS `UserConfig`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `UserConfig` (
  `userid` varchar(8) NOT NULL,
  `headUrl` varchar(255) DEFAULT NULL,
  `ElfLike` json DEFAULT NULL,
  PRIMARY KEY (`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserInformation`
--

DROP TABLE IF EXISTS `UserInformation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `UserInformation` (
  `UserId` varchar(10) NOT NULL COMMENT '由数字字母组成的8位id',
  `Phone` varchar(11) DEFAULT NULL COMMENT '绑定电话号码',
  `Password` varchar(255) DEFAULT NULL,
  `NickName` varchar(8) DEFAULT NULL COMMENT '最大十六个字符',
  `type` int NOT NULL DEFAULT '0' COMMENT '0为普通用户，1为高级用户',
  `activitaion` enum('1','0') DEFAULT '0',
  `register_date` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`UserId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'SGCloginer'
--

--
-- Dumping routines for database 'SGCloginer'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-08-08 17:42:56

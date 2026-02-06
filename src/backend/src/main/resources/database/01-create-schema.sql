-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: chef_pro
-- ------------------------------------------------------
-- Server version	5.5.5-10.4.32-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `allergens_dishes`
--
CREATE DATABASE IF NOT EXISTS chef_pro;
USE chef_pro;

DROP TABLE IF EXISTS `allergens_dishes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `allergens_dishes` (
  `menu_ID` int(11) NOT NULL,
  `dish_ID` int(11) NOT NULL,
  `allergen` varchar(50) NOT NULL,
  PRIMARY KEY (`menu_ID`,`dish_ID`,`allergen`),
  KEY `fk_official_allergen` (`allergen`),
  CONSTRAINT `allergens_dishes_ibfk_1` FOREIGN KEY (`menu_ID`, `dish_ID`) REFERENCES `dishes` (`menu_ID`, `dish_ID`) ON DELETE CASCADE,
  CONSTRAINT `fk_official_allergen` FOREIGN KEY (`allergen`) REFERENCES `official_allergens_list` (`allergen_name`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `chefs`
--

DROP TABLE IF EXISTS `chefs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chefs` (
  `user_ID` int(11) NOT NULL,
  `photo` varchar(255) DEFAULT NULL,
  `bio` text DEFAULT NULL,
  `prizes` text DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `address` varchar(500) DEFAULT NULL,
  `languages` varchar(255) DEFAULT NULL,
  `cover_photo` text DEFAULT NULL,
  PRIMARY KEY (`user_ID`),
  CONSTRAINT `chefs_ibfk_1` FOREIGN KEY (`user_ID`) REFERENCES `users` (`user_ID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `diners`
--

DROP TABLE IF EXISTS `diners`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `diners` (
  `user_ID` int(11) NOT NULL,
  `address` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`user_ID`),
  CONSTRAINT `diners_ibfk_1` FOREIGN KEY (`user_ID`) REFERENCES `users` (`user_ID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dishes`
--

DROP TABLE IF EXISTS `dishes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dishes` (
  `menu_ID` int(11) NOT NULL,
  `dish_ID` int(11) NOT NULL,
  `title` varchar(150) NOT NULL,
  `description` text DEFAULT NULL,
  `category` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`menu_ID`,`dish_ID`),
  CONSTRAINT `dishes_ibfk_1` FOREIGN KEY (`menu_ID`) REFERENCES `menu` (`menu_ID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `menu`
--

DROP TABLE IF EXISTS `menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `menu` (
  `menu_ID` int(11) NOT NULL AUTO_INCREMENT,
  `chef_ID` int(11) NOT NULL,
  `title` varchar(150) NOT NULL,
  `description` text DEFAULT NULL,
  `price_per_person` decimal(10,2) NOT NULL,
  `min_number_diners` int(11) DEFAULT NULL,
  `max_number_diners` int(11) DEFAULT NULL,
  `kitchen_requirements` text DEFAULT NULL,
  PRIMARY KEY (`menu_ID`),
  KEY `chef_ID` (`chef_ID`),
  CONSTRAINT `menu_ibfk_1` FOREIGN KEY (`chef_ID`) REFERENCES `chefs` (`user_ID`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `official_allergens_list`
--

DROP TABLE IF EXISTS `official_allergens_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `official_allergens_list` (
  `allergen_name` varchar(50) NOT NULL,
  PRIMARY KEY (`allergen_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `reservations`
--

DROP TABLE IF EXISTS `reservations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservations` (
  `chef_ID` int(11) NOT NULL,
  `date` date NOT NULL,
  `diner_ID` int(11) NOT NULL,
  `menu_ID` int(11) NOT NULL,
  `n_diners` int(11) NOT NULL,
  `address` varchar(255) DEFAULT NULL,
  `status` enum('PENDING','CONFIRMED','REJECTED','CANCELLED') NOT NULL DEFAULT 'PENDING',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`chef_ID`,`date`),
  KEY `diner_ID` (`diner_ID`),
  KEY `menu_ID` (`menu_ID`),
  CONSTRAINT `reservations_ibfk_1` FOREIGN KEY (`chef_ID`) REFERENCES `chefs` (`user_ID`),
  CONSTRAINT `reservations_ibfk_2` FOREIGN KEY (`diner_ID`) REFERENCES `diners` (`user_ID`),
  CONSTRAINT `reservations_ibfk_3` FOREIGN KEY (`menu_ID`) REFERENCES `menu` (`menu_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `reviews`
--

DROP TABLE IF EXISTS `reviews`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reviews` (
  `review_ID` int(11) NOT NULL AUTO_INCREMENT,
  `reviewed_user` int(11) NOT NULL,
  `reviewer_user` int(11) NOT NULL,
  `score` int(11) NOT NULL,
  `comment` text DEFAULT NULL,
  `date` date DEFAULT (CURDATE()),
  PRIMARY KEY (`review_ID`),
  KEY `reviewed_user` (`reviewed_user`),
  KEY `reviewer_user` (`reviewer_user`),
  CONSTRAINT `reviews_ibfk_1` FOREIGN KEY (`reviewed_user`) REFERENCES `users` (`user_ID`),
  CONSTRAINT `reviews_ibfk_2` FOREIGN KEY (`reviewer_user`) REFERENCES `users` (`user_ID`),
  CONSTRAINT `chk_score` CHECK (`score` >= 1 and `score` <= 5)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_ID` int(11) NOT NULL AUTO_INCREMENT,
  `role` enum('ADMIN','CHEF','DINER') NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(100) NOT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  `name` varchar(100) DEFAULT NULL,
  `lastname` varchar(100) DEFAULT NULL,
  `photo` LONGTEXT DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`user_ID`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `phone_number` (`phone_number`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-02-04 14:13:20

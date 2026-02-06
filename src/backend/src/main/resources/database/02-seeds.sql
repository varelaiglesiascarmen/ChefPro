-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: chef_pro
-- ------------------------------------------------------
-- Server version	5.5.5-10.4.32-MariaDB
--
-- Test users (username / password → role):
--   admin   / admin1234    → ADMIN
--   gordon  / chef1234     → CHEF
--   dani    / chef1234     → CHEF
--   juan    / comensal1234 → DINER
--   ana     / comensal1234 → DINER

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
-- Dumping data for table `allergens_dishes`
--

LOCK TABLES `allergens_dishes` WRITE;
/*!40000 ALTER TABLE `allergens_dishes` DISABLE KEYS */;
INSERT INTO `allergens_dishes` VALUES (1,1,'Gluten'),(1,2,'Lácteos'),(1,3,'Lácteos'),(2,1,'Gluten'),(2,1,'OTROS'),(2,2,'Pescado'),(2,3,'Moluscos'),(2,3,'OTROS');
/*!40000 ALTER TABLE `allergens_dishes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `chefs`
--

LOCK TABLES `chefs` WRITE;
/*!40000 ALTER TABLE `chefs` DISABLE KEYS */;
INSERT INTO `chefs` VALUES (2,'https://images.unsplash.com/photo-1577219491135-ce391730fb2c','Pasión por la cocina internacional y la perfección.','3 Estrellas Michelin'),(3,'https://images.unsplash.com/photo-1583394293214-28ded15ee548','Cocina de vanguardia con raíces tradicionales.','2 Soles Repsol');
/*!40000 ALTER TABLE `chefs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `diners`
--

LOCK TABLES `diners` WRITE;
/*!40000 ALTER TABLE `diners` DISABLE KEYS */;
INSERT INTO `diners` VALUES (4,'Calle Gran Vía 23, Madrid'),(5,'Avenida Diagonal 45, Barcelona');
/*!40000 ALTER TABLE `diners` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `dishes`
--

LOCK TABLES `dishes` WRITE;
/*!40000 ALTER TABLE `dishes` DISABLE KEYS */;
INSERT INTO `dishes` VALUES (1,1,'Bruschetta al Pomodoro','Pan de cristal con tomate y albahaca.','Entrante'),(1,2,'Risotto Funghi','Arroz carnaroli con boletus.','Principal'),(1,3,'Panna Cotta','Con frutos rojos silvestres.','Postre'),(2,1,'Salmorejo Cordobés','Con virutas de jamón ibérico.','Entrante'),(2,2,'Urta a la Roteña','Pescado de roca típico de Cádiz.','Principal'),(2,3,'Pulpo a la Gallega','Con pimentón de la Vera y cachelos.','Entrante');
/*!40000 ALTER TABLE `dishes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `menu`
--

LOCK TABLES `menu` WRITE;
/*!40000 ALTER TABLE `menu` DISABLE KEYS */;
INSERT INTO `menu` VALUES (1,2,'Italia Clásica','Menú degustación del norte de Italia.',55.00,2,8,'Horno necesario'),(2,3,'Aires del Sur','Pescados frescos y verduras de la huerta.',80.50,4,12,'Fuegos de gas');
/*!40000 ALTER TABLE `menu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `official_allergens_list`
--

LOCK TABLES `official_allergens_list` WRITE;
/*!40000 ALTER TABLE `official_allergens_list` DISABLE KEYS */;
INSERT INTO `official_allergens_list` VALUES ('Altramuces'),('Apio'),('Cacahuetes'),('Crustáceos'),('Dióxido de azufre y sulfitos'),('Frutos de cáscara'),('Gluten'),('Granos de sésamo'),('Huevos'),('Lácteos'),('Moluscos'),('Mostaza'),('OTROS'),('Pescado'),('Soja');
/*!40000 ALTER TABLE `official_allergens_list` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `reservations`
--

LOCK TABLES `reservations` WRITE;
/*!40000 ALTER TABLE `reservations` DISABLE KEYS */;
INSERT INTO `reservations` VALUES (2,'2026-03-15',4,1,4,'Calle Gran Vía 23, Madrid','CONFIRMED','2026-02-04 12:41:55'),(2,'2026-05-01',5,1,2,'Calle Falsa 123','CANCELLED','2026-02-04 12:44:14'),(3,'2026-03-20',5,2,6,'Casa de Ana, Salón principal','PENDING','2026-02-04 12:41:55');
/*!40000 ALTER TABLE `reservations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `reviews`
--

LOCK TABLES `reviews` WRITE;
/*!40000 ALTER TABLE `reviews` DISABLE KEYS */;
INSERT INTO `reviews` VALUES (1,2,4,5,'Increíble experiencia, el chef fue muy profesional.','2026-01-10'),(2,3,5,4,'La comida excelente, aunque llegó 5 min tarde.','2026-01-12');
/*!40000 ALTER TABLE `reviews` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'ADMIN','admin','$2a$10$6maG.SKdrCRX16NKsJZ5TewH/z.GqeLKHgjSsSB.9RoFi2psh8NeW','admin@app.com','600000000','Super','Admin','2026-02-04 12:14:54'),(2,'CHEF','gordon','$2a$10$.jvy7lGrZg/153is3Xmm.uX4rs89y4/YKNx7CfBUj/KXLqwqbH.le','gordon@kitchen.com','611111111','Gordon','Ramz','2026-02-04 12:14:54'),(3,'CHEF','dani','$2a$10$.jvy7lGrZg/153is3Xmm.uX4rs89y4/YKNx7CfBUj/KXLqwqbH.le','dani@garcia.com','622222222','Dani','Garcia','2026-02-04 12:14:54'),(4,'DINER','juan','$2a$10$D18OcHT11Des2Bnk6fngjupe4mSvBLMtrV1RQk.yil7craNt2aN6q','juan@gmail.com','633333333','Juan','Pérez','2026-02-04 12:14:54'),(5,'DINER','ana','$2a$10$D18OcHT11Des2Bnk6fngjupe4mSvBLMtrV1RQk.yil7craNt2aN6q','ana@hotmail.com','644444444','Ana','López','2026-02-04 12:14:54');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-02-04 14:16:24

-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: localhost    Database: pharmacie_db
-- ------------------------------------------------------
-- Server version	8.0.41

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
-- Table structure for table `approvisionnements`
--

DROP TABLE IF EXISTS `approvisionnements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `approvisionnements` (
  `id_approvisionnement` int NOT NULL AUTO_INCREMENT,
  `date_approvisionnement` datetime NOT NULL,
  `id_fournisseur` int NOT NULL,
  `montant_total_ht` decimal(12,2) NOT NULL,
  `montant_total_ttc` decimal(12,2) NOT NULL,
  `montant_tva` decimal(12,2) NOT NULL,
  `reference_bon_commande` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_approvisionnement`),
  KEY `id_fournisseur` (`id_fournisseur`),
  CONSTRAINT `approvisionnements_ibfk_1` FOREIGN KEY (`id_fournisseur`) REFERENCES `fournisseurs` (`id_fournisseur`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `approvisionnements`
--

LOCK TABLES `approvisionnements` WRITE;
/*!40000 ALTER TABLE `approvisionnements` DISABLE KEYS */;
INSERT INTO `approvisionnements` VALUES (1,'2025-06-28 01:52:45',1,31928.00,37675.04,5747.04,'BC-20250628-025226'),(2,'2025-06-28 01:59:31',1,2070.00,2442.60,372.60,'BC-20250628-025911'),(3,'2025-06-28 11:55:44',1,1290.00,1522.20,232.20,'BC-20250628-125521'),(4,'2025-06-28 11:57:37',1,1290.00,1522.20,232.20,'BC-20250628-125728'),(5,'2025-06-28 12:09:06',1,1110.00,1309.80,199.80,'BC-20250628-130848'),(6,'2025-06-28 12:40:00',1,1000.00,1180.00,180.00,'BC-20250628-133948'),(7,'2025-06-29 20:05:20',1,92268.00,108876.24,16608.24,'BC-20250629-210448'),(8,'2025-06-29 21:20:25',1,400000.00,472000.00,72000.00,'BC-20250629-221904'),(9,'2025-06-30 19:07:26',1,10000.00,11800.00,1800.00,'BC-20250630-200704'),(10,'2025-07-06 14:01:01',1,100000.00,118000.00,18000.00,'BC-20250706-150023');
/*!40000 ALTER TABLE `approvisionnements` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `assurances_social`
--

DROP TABLE IF EXISTS `assurances_social`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `assurances_social` (
  `id_assurance` int NOT NULL AUTO_INCREMENT,
  `nom_assurance` varchar(100) NOT NULL,
  `taux_de_prise_en_charge` decimal(5,4) NOT NULL,
  `date_de_creation` date NOT NULL,
  PRIMARY KEY (`id_assurance`),
  UNIQUE KEY `nom_assurance` (`nom_assurance`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `assurances_social`
--

LOCK TABLES `assurances_social` WRITE;
/*!40000 ALTER TABLE `assurances_social` DISABLE KEYS */;
INSERT INTO `assurances_social` VALUES (1,'CNAMGS',0.8000,'2025-06-27'),(2,'ASCOMA',0.8000,'2025-06-27'),(3,'AXA Assurances',0.6500,'2025-06-27'),(4,'MUTELLE SANTE X',0.7500,'2025-06-27'),(5,'OGAR Assurance Santé',0.8000,'2025-06-27'),(6,'SUNU Assurance Gabon',0.7000,'2025-06-27'),(7,'NSIA Assurance Santé',0.7500,'2025-06-27'),(8,'Mutelle des focntionnaires',0.9900,'2025-06-27'),(9,'Mutelle d\'entreprise',0.9900,'2025-06-27'),(10,'CNAMSG_AGPU',0.9900,'2025-06-27');
/*!40000 ALTER TABLE `assurances_social` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comptes_comptables`
--

DROP TABLE IF EXISTS `comptes_comptables`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comptes_comptables` (
  `id_compte` int NOT NULL AUTO_INCREMENT,
  `numero_compte` varchar(20) NOT NULL,
  `nom_compte` varchar(100) NOT NULL,
  `type_compte` enum('ACTIF','PASSIF','PRODUIT','CHARGE','CAPITAL') NOT NULL,
  `description_compte` text,
  PRIMARY KEY (`id_compte`),
  UNIQUE KEY `numero_compte` (`numero_compte`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comptes_comptables`
--

LOCK TABLES `comptes_comptables` WRITE;
/*!40000 ALTER TABLE `comptes_comptables` DISABLE KEYS */;
INSERT INTO `comptes_comptables` VALUES (1,'512','Banque','ACTIF','Compte courant bancaire de la pharmacie'),(2,'530','Caisse','ACTIF','Argent liquide disponible en caisse.'),(3,'607','Achats de marchandises','CHARGE','Achats de médicaments et produits pour la revente.'),(4,'707','Ventes de marchandises','PRODUIT','Ventes de médicaments et produits pharmaceutiques.'),(5,'4457','TVA collectée','PASSIF','TVA sur les ventes à reverser à l\'\'État.'),(6,'401','Fournisseurs','PASSIF','Dettes envers les fournisseurs.'),(7,'411','Clients','ACTIF','Créances sur les clients.'),(8,'4456','TVA déductible','ACTIF','TVA sur les achats de bien et service déductible');
/*!40000 ALTER TABLE `comptes_comptables` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `factures`
--

DROP TABLE IF EXISTS `factures`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `factures` (
  `id_facture` int NOT NULL AUTO_INCREMENT,
  `numero_facture` varchar(100) DEFAULT NULL,
  `date_heure` datetime DEFAULT NULL,
  `id_utilisateur` int NOT NULL,
  `total_ht` decimal(10,2) NOT NULL,
  `total_ttc` decimal(10,2) NOT NULL,
  `date_facture` datetime DEFAULT NULL,
  `id_assurance` int DEFAULT NULL,
  `montant_prise_en_charge` decimal(10,2) DEFAULT '0.00',
  `montant_restant_a_payer_client` decimal(10,2) DEFAULT '0.00',
  PRIMARY KEY (`id_facture`),
  UNIQUE KEY `numero_facture` (`numero_facture`),
  KEY `fk_id_assurance` (`id_assurance`),
  CONSTRAINT `fk_id_assurance` FOREIGN KEY (`id_assurance`) REFERENCES `assurances_social` (`id_assurance`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=56 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `factures`
--

LOCK TABLES `factures` WRITE;
/*!40000 ALTER TABLE `factures` DISABLE KEYS */;
INSERT INTO `factures` VALUES (14,'DAVIDIBINGA-000014',NULL,1,13670.30,16130.95,'2025-06-18 00:25:05',NULL,0.00,0.00),(15,'JEANNE-000015',NULL,4,29325.42,34604.00,'2025-06-18 01:00:00',NULL,0.00,0.00),(16,'DAVIDIBINGA-000016',NULL,1,4957.61,5849.98,'2025-06-18 07:54:52',NULL,0.00,0.00),(17,'JEANNE-000017',NULL,4,34532.94,40748.88,'2025-06-18 07:59:43',NULL,0.00,0.00),(18,'JEANNE-000018',NULL,4,10500.00,12390.00,'2025-06-18 08:00:40',NULL,0.00,0.00),(19,'JEANNE-000019',NULL,4,10500.00,12390.00,'2025-06-18 08:01:14',NULL,0.00,0.00),(20,'DAVIDIBINGA-000020',NULL,1,4957.61,5849.98,'2025-06-26 12:33:56',NULL,0.00,0.00),(21,'DAVIDIBINGA-000021',NULL,1,1202.52,1418.98,'2025-06-26 12:36:23',NULL,0.00,0.00),(22,'DAVIDIBINGA-000022',NULL,1,13732.88,16204.80,'2025-06-26 12:36:44',NULL,0.00,0.00),(23,'JEANNE-000023',NULL,4,1475.21,1740.75,'2025-06-26 12:44:49',NULL,0.00,0.00),(24,'DAVIDIBINGA-000024',NULL,1,3755.08,4431.00,'2025-06-26 12:57:20',NULL,0.00,0.00),(25,'DAVIDIBINGA-000025',NULL,1,1676.38,1978.12,'2025-06-26 12:58:49',NULL,0.00,0.00),(26,'DAVIDIBINGA-000026',NULL,1,25168.01,29698.25,'2025-06-26 12:59:48',NULL,0.00,0.00),(27,'DAVIDIBINGA-000027',NULL,1,1202.52,1418.98,'2025-06-26 13:00:20',NULL,0.00,0.00),(28,'DAVIDIBINGA-000028',NULL,1,1202.52,1418.98,'2025-06-26 13:02:43',NULL,0.00,0.00),(29,'DAVIDIBINGA-000029',NULL,1,1394.75,1645.80,'2025-06-26 13:17:22',NULL,0.00,0.00),(30,'DAVIDIBINGA-000030',NULL,1,8314.83,9811.50,'2025-06-26 14:26:13',NULL,0.00,0.00),(31,'DAVIDIBINGA-000031',NULL,1,129623.94,152956.25,'2025-06-26 14:32:14',NULL,0.00,0.00),(32,'JEANNE-000032',NULL,4,200271.19,236320.00,'2025-06-26 14:43:04',NULL,0.00,0.00),(33,'JEANNE-000033',NULL,4,4309923.73,5085710.00,'2025-06-26 14:46:42',NULL,0.00,0.00),(34,'JEANNE-000034',NULL,4,111579.66,131664.00,'2025-06-26 20:05:48',NULL,0.00,0.00),(35,'DAVIDIBINGA-000035',NULL,1,1202.52,1418.98,'2025-06-27 10:12:44',NULL,0.00,0.00),(36,'FACT-A5904357',NULL,1,1587.10,1587.10,'2025-06-27 13:58:15',1,1110.97,476.13),(39,'FACT-393B45A6',NULL,1,1587.10,1587.10,'2025-06-27 14:23:14',1,1110.97,476.13),(40,'FACT-55662FB8',NULL,1,57471.90,57471.90,'2025-06-27 14:25:59',2,45977.52,11494.38),(41,'FACT-2C05B9A4',NULL,1,3174.20,3174.20,'2025-06-27 15:22:10',2,2539.36,634.84),(42,'FACT-9F09CF7F',NULL,1,23836.00,23836.00,'2025-06-27 16:16:33',1,16685.20,7150.80),(43,'FACT-B6CACF2F',NULL,1,1587.10,1587.10,'2025-06-27 23:24:14',1,1110.97,476.13),(44,'FACT-AC6375E9',NULL,1,57112.00,57112.00,'2025-06-28 02:07:18',2,45689.60,11422.40),(45,'FACT-332AB555',NULL,1,1587.10,1587.10,'2025-06-28 11:24:01',NULL,0.00,1587.10),(46,'FACT-5AA4070B',NULL,1,1587.10,1587.10,'2025-06-28 11:28:06',1,1110.97,476.13),(47,'FACT-B0233F78',NULL,1,41205.60,41205.60,'2025-06-28 18:46:34',4,30904.20,10301.40),(48,'FACT-6CA27CD9',NULL,1,11912.10,11912.10,'2025-06-29 18:26:16',10,10332.73,1579.37),(49,'FACT-47D7E70B',NULL,1,1115100.00,1115100.00,'2025-06-29 21:21:51',NULL,0.00,1115100.00),(50,'FACT-2036AEB5',NULL,1,1593.00,1593.00,'2025-06-30 18:10:47',1,1274.40,318.60),(51,'FACT-51EAD6C4',NULL,1,5310.00,5310.00,'2025-06-30 19:08:13',2,4248.00,1062.00),(52,'FACT-CC63A4A9',NULL,7,10684.90,10684.90,'2025-06-30 19:09:13',10,10578.05,106.85),(53,'FACT-8A74F808',NULL,1,11499.10,11499.10,'2025-07-02 17:08:17',NULL,0.00,11499.10),(54,'FACT-8E3236C2',NULL,1,8755.60,8755.60,'2025-07-06 14:02:45',1,7004.48,1751.12),(55,'FACT-B774E666',NULL,1,6543.10,6543.10,'2025-07-08 12:57:45',1,5234.48,1308.62);
/*!40000 ALTER TABLE `factures` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fournisseurs`
--

DROP TABLE IF EXISTS `fournisseurs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fournisseurs` (
  `id_fournisseur` int NOT NULL AUTO_INCREMENT,
  `nom_fournisseur` varchar(100) NOT NULL,
  `contact_fournisseur` varchar(100) DEFAULT NULL,
  `telephone_fournisseur` varchar(50) DEFAULT NULL,
  `email_fournisseur` varchar(100) DEFAULT NULL,
  `adresse_fournisseur` text,
  PRIMARY KEY (`id_fournisseur`),
  UNIQUE KEY `nom_fournisseur` (`nom_fournisseur`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fournisseurs`
--

LOCK TABLES `fournisseurs` WRITE;
/*!40000 ALTER TABLE `fournisseurs` DISABLE KEYS */;
INSERT INTO `fournisseurs` VALUES (1,'Pharma Gabon','Mme NDOBA MENGUE Flore','077047741','nd_mengue@pharmagabon.ga','SNI - Owendo(Gabon)');
/*!40000 ALTER TABLE `fournisseurs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lignes_approvisionnement`
--

DROP TABLE IF EXISTS `lignes_approvisionnement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lignes_approvisionnement` (
  `id_ligne_approvisionnement` int NOT NULL AUTO_INCREMENT,
  `id_approvisionnement` int NOT NULL,
  `id_produit` int NOT NULL,
  `quantite` int NOT NULL,
  `prix_unitaire` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id_ligne_approvisionnement`),
  KEY `id_approvisionnement` (`id_approvisionnement`),
  KEY `id_produit` (`id_produit`),
  CONSTRAINT `lignes_approvisionnement_ibfk_1` FOREIGN KEY (`id_approvisionnement`) REFERENCES `approvisionnements` (`id_approvisionnement`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `lignes_approvisionnement_ibfk_2` FOREIGN KEY (`id_produit`) REFERENCES `produits` (`id_produit`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lignes_approvisionnement`
--

LOCK TABLES `lignes_approvisionnement` WRITE;
/*!40000 ALTER TABLE `lignes_approvisionnement` DISABLE KEYS */;
INSERT INTO `lignes_approvisionnement` VALUES (1,1,284,13,2456.00),(2,2,284,1,2070.00),(3,3,282,1,1290.00),(4,4,285,1,1290.00),(5,5,282,1,1110.00),(6,6,282,1,1000.00),(7,7,289,12,7689.00),(8,8,282,100,1000.00),(9,8,284,100,3000.00),(10,9,301,1,10000.00),(11,10,332,10,10000.00);
/*!40000 ALTER TABLE `lignes_approvisionnement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lignesfacture`
--

DROP TABLE IF EXISTS `lignesfacture`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lignesfacture` (
  `id_ligne_facture` int NOT NULL AUTO_INCREMENT,
  `id_facture` int DEFAULT NULL,
  `id_produit` int DEFAULT NULL,
  `nom_produit` varchar(255) DEFAULT NULL,
  `reference_produit` varchar(50) DEFAULT NULL,
  `quantite_vendue` int DEFAULT NULL,
  `prix_unitaire_ht` decimal(10,2) DEFAULT NULL,
  `prix_unitaire_ttc` decimal(10,2) DEFAULT NULL,
  `sous_total` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`id_ligne_facture`)
) ENGINE=InnoDB AUTO_INCREMENT=104 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lignesfacture`
--

LOCK TABLES `lignesfacture` WRITE;
/*!40000 ALTER TABLE `lignesfacture` DISABLE KEYS */;
INSERT INTO `lignesfacture` VALUES (18,14,282,NULL,NULL,2,1418.98,NULL,2837.95),(19,14,284,NULL,NULL,3,4431.00,NULL,13293.00),(20,15,284,NULL,NULL,2,4431.00,NULL,8862.00),(21,15,328,NULL,NULL,1,7385.00,NULL,7385.00),(22,15,296,NULL,NULL,2,9178.50,NULL,18357.00),(23,16,282,NULL,NULL,1,1418.98,NULL,1418.98),(24,16,284,NULL,NULL,1,4431.00,NULL,4431.00),(25,17,283,NULL,NULL,1,1350.40,NULL,1350.40),(26,17,297,NULL,NULL,1,7174.00,NULL,7174.00),(27,17,337,NULL,NULL,1,3540.00,NULL,3540.00),(28,17,282,NULL,NULL,1,1418.98,NULL,1418.98),(29,17,285,NULL,NULL,1,2215.50,NULL,2215.50),(30,17,317,NULL,NULL,1,12660.00,NULL,12660.00),(31,17,351,NULL,NULL,1,12390.00,NULL,12390.00),(32,18,351,NULL,NULL,1,12390.00,NULL,12390.00),(33,19,351,NULL,NULL,1,12390.00,NULL,12390.00),(34,20,282,NULL,NULL,1,1418.98,NULL,1418.98),(35,20,284,NULL,NULL,1,4431.00,NULL,4431.00),(36,21,282,NULL,NULL,1,1418.98,NULL,1418.98),(37,22,283,NULL,NULL,12,1350.40,NULL,16204.80),(38,23,305,NULL,NULL,1,1740.75,NULL,1740.75),(39,24,284,NULL,NULL,1,4431.00,NULL,4431.00),(40,25,286,NULL,NULL,1,1978.12,NULL,1978.12),(41,26,294,NULL,NULL,1,29698.25,NULL,29698.25),(42,27,282,NULL,NULL,1,1418.98,NULL,1418.98),(43,28,282,NULL,NULL,1,1418.98,NULL,1418.98),(44,29,295,NULL,NULL,1,1645.80,NULL,1645.80),(45,30,289,NULL,NULL,1,9811.50,NULL,9811.50),(46,31,301,NULL,NULL,1,15086.50,NULL,15086.50),(47,31,351,NULL,NULL,3,12390.00,NULL,37170.00),(48,31,283,NULL,NULL,3,1350.40,NULL,4051.20),(49,31,300,NULL,NULL,6,10339.00,NULL,62034.00),(50,31,303,NULL,NULL,1,2700.80,NULL,2700.80),(51,31,298,NULL,NULL,2,1107.75,NULL,2215.50),(52,31,294,NULL,NULL,1,29698.25,NULL,29698.25),(53,32,290,NULL,NULL,10,23632.00,NULL,236320.00),(54,33,290,NULL,NULL,100,23632.00,NULL,2363200.00),(55,33,297,NULL,NULL,10,7174.00,NULL,71740.00),(56,33,303,NULL,NULL,100,2700.80,NULL,270080.00),(57,33,304,NULL,NULL,100,1793.50,NULL,179350.00),(58,33,283,NULL,NULL,100,1350.40,NULL,135040.00),(59,33,327,NULL,NULL,100,4220.00,NULL,422000.00),(60,33,324,NULL,NULL,100,10022.50,NULL,1002250.00),(61,33,321,NULL,NULL,100,1582.50,NULL,158250.00),(62,33,336,NULL,NULL,100,4838.00,NULL,483800.00),(63,34,283,NULL,NULL,10,1350.40,NULL,13504.00),(64,34,293,NULL,NULL,10,11816.00,NULL,118160.00),(65,35,282,NULL,NULL,1,1418.98,NULL,1418.98),(66,39,NULL,NULL,'MED001',1,1587.10,1587.10,1587.10),(67,40,NULL,NULL,'MED001',1,1587.10,1587.10,1587.10),(68,40,NULL,NULL,'MED002',2,1510.40,1510.40,3020.80),(69,40,NULL,NULL,'MED009',2,26432.00,26432.00,52864.00),(70,41,NULL,NULL,'MED001',2,1587.10,1587.10,3174.20),(71,42,NULL,NULL,'MED003',2,4956.00,4956.00,9912.00),(72,42,NULL,NULL,'MED006',2,6962.00,6962.00,13924.00),(73,43,NULL,NULL,'MED001',1,1587.10,1587.10,1587.10),(74,44,NULL,NULL,'MED007',1,8850.00,8850.00,8850.00),(75,44,NULL,NULL,'MED009',1,26432.00,26432.00,26432.00),(76,44,NULL,NULL,'MED003',1,4956.00,4956.00,4956.00),(77,44,NULL,NULL,'MED020',1,16874.00,16874.00,16874.00),(78,45,NULL,NULL,'MED001',1,1587.10,1587.10,1587.10),(79,46,NULL,NULL,'MED001',1,1587.10,1587.10,1587.10),(80,47,NULL,NULL,'MED001',1,1587.10,1587.10,1587.10),(81,47,NULL,NULL,'MED005',1,2212.50,2212.50,2212.50),(82,47,NULL,NULL,'MED009',1,26432.00,26432.00,26432.00),(83,47,NULL,NULL,'MED008',1,10974.00,10974.00,10974.00),(84,48,NULL,NULL,'MED001',1,1587.10,1587.10,1587.10),(85,48,NULL,NULL,'MED007',1,8850.00,8850.00,8850.00),(86,48,NULL,NULL,'PARA002',1,1475.00,1475.00,1475.00),(87,49,NULL,NULL,'MED001',200,1587.10,1587.10,317420.00),(88,49,NULL,NULL,'MED002',200,1510.40,1510.40,302080.00),(89,49,NULL,NULL,'MED003',100,4956.00,4956.00,495600.00),(90,50,NULL,NULL,'MED027',1,1593.00,1593.00,1593.00),(91,51,NULL,NULL,'MED001',1,1587.10,1587.10,1587.10),(92,51,NULL,NULL,'MED002',1,1510.40,1510.40,1510.40),(93,51,NULL,NULL,'MED005',1,2212.50,2212.50,2212.50),(94,52,NULL,NULL,'MED002',1,1510.40,1510.40,1510.40),(95,52,NULL,NULL,'MED005',1,2212.50,2212.50,2212.50),(96,52,NULL,NULL,'MED006',1,6962.00,6962.00,6962.00),(97,53,NULL,NULL,'MED001',1,1587.10,1587.10,1587.10),(98,53,NULL,NULL,'MED003',2,4956.00,4956.00,9912.00),(99,54,NULL,NULL,'MED001',1,1587.10,1587.10,1587.10),(100,54,NULL,NULL,'MED003',1,4956.00,4956.00,4956.00),(101,54,NULL,NULL,'MED005',1,2212.50,2212.50,2212.50),(102,55,NULL,NULL,'MED001',1,1587.10,1587.10,1587.10),(103,55,NULL,NULL,'MED003',1,4956.00,4956.00,4956.00);
/*!40000 ALTER TABLE `lignesfacture` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `produits`
--

DROP TABLE IF EXISTS `produits`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `produits` (
  `id_produit` int NOT NULL AUTO_INCREMENT,
  `reference` varchar(50) NOT NULL,
  `nom` varchar(255) NOT NULL,
  `description` text,
  `prix_ht` decimal(10,2) NOT NULL,
  `quantite` int NOT NULL DEFAULT '0',
  `type_produit` varchar(20) NOT NULL,
  `est_generique` tinyint(1) DEFAULT NULL,
  `est_sur_ordonnance` tinyint(1) DEFAULT NULL,
  `categorie_parapharmacie` varchar(100) DEFAULT NULL,
  `date_ajout` datetime DEFAULT CURRENT_TIMESTAMP,
  `derniere_mise_a_jour` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `est_remboursable` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id_produit`),
  UNIQUE KEY `reference` (`reference`)
) ENGINE=InnoDB AUTO_INCREMENT=355 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `produits`
--

LOCK TABLES `produits` WRITE;
/*!40000 ALTER TABLE `produits` DISABLE KEYS */;
INSERT INTO `produits` VALUES (282,'MED001','Paracétamol 500mg','Soulage la douleur et abaisse la fièvre. Indiqué pour les maux de têtes, états gripaux',1345.00,204,'Medicament',1,0,NULL,'2025-06-18 01:02:02','2025-06-29 22:20:24',1),(283,'MED002','Amoxiciline 250mg','Antibiotique à large spectre pour les infections bactériennes (respiratoires, urinaires, cutanées)',1280.00,324,'Medicament',0,0,NULL,'2025-06-18 01:03:35','2025-06-27 11:50:15',1),(284,'MED003','Ibuprofène 400mg','Anti-inflammatoire non stéroïdien (AINS) pour les douleurs légères à modérées et inflammation.',4200.00,122,'Medicament',0,0,NULL,'2025-06-18 01:05:26','2025-06-29 22:20:24',1),(285,'MED004','Loperamide 2mg','Antidiarrhéique pour le traitement symptomatique des diarrhées aiguës et chroniques.',2100.00,80,'Medicament',1,0,NULL,'2025-06-18 01:06:42','2025-06-28 12:57:37',1),(286,'MED005','Oméprazole 20mg','Inhibiteur de la pompe à protons (IPP) pour le traitement des ulcères gastriques et du reflux gastro-œsophagien.',1875.00,29,'Medicament',0,1,NULL,'2025-06-18 01:07:34','2025-06-27 11:50:15',1),(287,'MED006','Dextrométhorphane 15mg','Antitussif pour soulager la toux sèche et d\'\'irritation.',5900.00,60,'Medicament',1,0,NULL,'2025-06-18 01:08:38','2025-06-27 11:50:15',1),(288,'MED007','Cetizine 10mg','Antihistaminique pour le soulagement des symptômes d\'\'allergies (rhinite, urticaire).',7500.00,20,'Medicament',1,0,NULL,'2025-06-18 01:09:22','2025-06-27 11:50:15',1),(289,'MED008','Metformine 500mg','Antidiabétique oral pour le traitement du diabète de type 2, en particulier chez les patients en surpoids.',9300.00,39,'Medicament',0,1,NULL,'2025-06-18 01:10:13','2025-06-29 21:05:19',1),(290,'MED009','Amlodipine 5mg','Antihypertenseur de la classe des inhibiteurs calciques pour l\'\'hypertension artérielle et l\'\'angine de poitrine.',22400.00,100,'Medicament',1,1,NULL,'2025-06-18 01:11:16','2025-06-27 11:50:15',1),(291,'MED010','Atorvastatine 20mg','Statine utilisée pour abaisser le taux de cholestérol sanguin.',3500.00,40,'Medicament',0,1,NULL,'2025-06-18 01:12:54','2025-06-27 11:50:15',1),(292,'MED011','Sertraline 50mg','Antidépresseur ISRS pour le traitement de la dépression, des troubles paniques et de l\'\'anxiété.',4590.00,50,'Medicament',0,1,NULL,'2025-06-18 01:13:41','2025-06-27 11:50:15',1),(293,'MED012','Furosémide 40mg','Diurétique de l\'\'anse pour le traitement de l\'\'œdème et de l\'\'hypertension.',11200.00,30,'Medicament',1,1,NULL,'2025-06-18 01:15:09','2025-06-27 11:50:15',1),(294,'MED013','Azithromycine 500mg','Antibiotique macrolide pour infections respiratoires, cutanées et des voies urinaires.',28150.00,38,'Medicament',0,1,NULL,'2025-06-18 01:16:29','2025-06-27 11:50:15',1),(295,'MED014','Prednisone 20mg','Corticoïde puissant avec des propriétés anti-inflammatoires et immunosuppressives.',1560.00,24,'Medicament',0,1,NULL,'2025-06-18 01:17:20','2025-06-27 11:50:15',1),(296,'MED015','Tramadol 50mg','Analgésique opioïde pour le traitement des douleurs modérées à sévères.',8700.00,38,'Medicament',1,1,NULL,'2025-06-18 01:18:01','2025-06-27 11:50:15',1),(297,'MED016','Salbutamol 100mcg','Bronchodilatateur à action rapide pour le soulagement des crises d\'\'asthme.',6800.00,29,'Medicament',1,1,NULL,'2025-06-18 01:18:57','2025-06-27 11:50:15',1),(298,'MED017','Lorazepam 1mg','Anxiolytique de la famille des benzodiazépines pour le traitement de l\'\'anxiété et de l\'\'insomnie.',1050.00,18,'Medicament',0,1,NULL,'2025-06-18 01:19:57','2025-06-27 11:50:15',1),(299,'MED018','Levothyroxine 50mcg','Hormone thyroïdienne pour le traitement de l\'\'hypothyroïdie.',20100.00,22,'Medicament',0,1,NULL,'2025-06-18 01:20:52','2025-06-27 11:50:15',1),(300,'MED019','Hydrochlorothiazide 25mg','Diurétique thiazidique pour l\'\'hypertension et l\'\'œdème.',9800.00,44,'Medicament',1,1,NULL,'2025-06-18 01:21:41','2025-06-27 11:50:15',1),(301,'MED020','Fluconazole 150mg','Antifongique pour le traitement des infections fongiques systémiques et superficielles.',14300.00,19,'Medicament',0,1,NULL,'2025-06-18 01:22:40','2025-06-30 20:07:25',1),(302,'MED021','Bisoprolol 5mg','Bêta-bloquant pour l\'\'hypertension, l\'\'angine de poitrine et l\'\'insuffisance cardiaque.',19900.00,21,'Medicament',1,1,NULL,'2025-06-18 01:23:47','2025-06-27 11:50:15',1),(303,'MED022','Gabapentine 300mg','Antiépileptique et analgésique pour les douleurs neuropathiques et l\'\'épilepsie.',2560.00,19,'Medicament',0,1,NULL,'2025-06-18 01:28:59','2025-06-27 11:50:15',1),(304,'MED023','Warfarine 5mg','Anticoagulant oral pour la prévention des thromboses.',1700.00,40,'Medicament',0,1,NULL,'2025-06-18 01:29:48','2025-06-27 11:50:15',1),(305,'MED024','Simvastatine 10mg','Statine pour la réduction du cholestérol et la prévention cardiovasculaire.',1650.00,22,'Medicament',1,1,NULL,'2025-06-18 01:30:41','2025-06-27 11:50:15',1),(306,'MED025','Ranitidine 150mg','Anti-ulcéreux, antagoniste des récepteurs H2, pour les brûlures d\'\'estomac et ulcères.',8000.00,40,'Medicament',1,0,NULL,'2025-06-18 01:31:21','2025-06-27 11:50:15',1),(307,'MED026','Clarithromycine 250mg','Antibiotique macrolide pour diverses infections bactériennes.',2100.00,100,'Medicament',0,1,NULL,'2025-06-18 01:32:12','2025-06-27 16:21:40',1),(308,'MED027','Baclofène 10mg','Myorelaxant pour le traitement de la spasticité musculaire.',1350.00,150,'Medicament',0,1,NULL,'2025-06-18 01:33:04','2025-06-27 11:50:15',1),(309,'MED028','Doxycyline 100mg','Antibiotique tétracycline pour infections bactériennes et acné sévère.',9200.00,200,'Medicament',1,1,NULL,'2025-06-18 01:33:54','2025-06-27 11:50:15',1),(310,'MED029','Valacivlovir 500mg','Antiviral pour le traitement de l\'\'herpès et du zona.',3800.00,70,'Medicament',0,1,NULL,'2025-06-18 01:35:08','2025-06-27 11:50:15',1),(311,'MED030','Codeine 30mg','Analgésique opioïde et antitussif pour la douleur et la toux sèche sévère.',7100.00,80,'Medicament',1,1,NULL,'2025-06-18 01:36:04','2025-06-27 11:50:15',1),(312,'MED031','Allopurinol 100mg','Médicament contre la goutte, réduit la production d\'\'acide urique.',1030.00,18,'Medicament',1,1,NULL,'2025-06-18 01:42:22','2025-06-27 11:50:15',1),(313,'MED032','Hydroxyzine 25mg','Antihistaminique sédatif pour l\'\'anxiété et l\'\'urticaire.',6500.00,30,'Medicament',0,0,NULL,'2025-06-18 01:43:29','2025-06-27 11:50:15',1),(314,'MED033','Digoxine 0.25mg','Digoxine 0.25mg',2500.00,70,'Medicament',0,1,NULL,'2025-06-18 01:44:08','2025-06-27 11:50:15',1),(315,'MED034','Quinine 250mg','Antipaludéen pour le traitement des formes graves de paludisme.',1800.00,110,'Medicament',0,1,NULL,'2025-06-18 01:44:50','2025-06-27 11:50:15',1),(316,'MED035','Métronidazole 500mg','Antibiotique et antiparasitaire pour les infections bactériennes et parasitaires.',7890.00,240,'Medicament',1,1,NULL,'2025-06-18 01:45:25','2025-06-27 11:50:15',1),(317,'MED036','Spironolactone 25mg','Diurétique épargneur de potassium pour l\'\'hypertension et l\'\'insuffisance cardiaque.',12000.00,109,'Medicament',0,1,NULL,'2025-06-18 01:46:02','2025-06-27 11:50:15',1),(318,'MED037','Ciprofloxacin 500mg','Antibiotique fluoroquinolone pour un large éventail d\'\'infections bactériennes.',2050.00,40,'Medicament',1,1,NULL,'2025-06-18 01:46:34','2025-06-27 11:50:15',1),(319,'MED038','Sildenafil 50mg','Médicament pour la dysfonction érectile et l\'\'hypertension artérielle pulmonaire.',3000.00,50,'Medicament',0,1,NULL,'2025-06-18 01:47:09','2025-06-27 11:50:15',1),(320,'MED039','Venlafaxine 75mg','Antidépresseur ISRSN pour la dépression et les troubles anxieux.',4800.00,90,'Medicament',0,1,NULL,'2025-06-18 01:47:43','2025-06-27 11:50:15',1),(321,'MED040','Albuterol HFA 90mcg','Inhalateur de secours pour l\'\'asthme et la BPCO.',1500.00,300,'Medicament',1,1,NULL,'2025-06-18 01:48:28','2025-06-27 11:50:15',1),(322,'MED041','Fluoxetine 20mg','Antidépresseur ISRS pour la dépression, les TOC et la boulimie nerveuse.',1600.00,60,'Medicament',1,1,NULL,'2025-06-18 01:49:03','2025-06-27 11:50:15',1),(323,'MED042','Risperidone 1mg','Antipsychotique atypique pour la schizophrénie et les troubles bipolaires.',3200.00,100,'Medicament',0,1,NULL,'2025-06-18 01:49:37','2025-06-27 11:50:15',1),(324,'MED043','Cyclobenzaprine 10mg','Relaxant musculaire pour les spasmes musculaires aigus.',9500.00,100,'Medicament',0,1,NULL,'2025-06-18 01:50:06','2025-06-27 11:50:15',1),(325,'MED044','Clopidogrel 75mg','Antiagrégant plaquettaire pour la prévention des événements cardiovasculaires.',2800.00,30,'Medicament',1,1,NULL,'2025-06-18 01:50:42','2025-06-27 11:50:15',1),(326,'MED045','Sulfaméthoxazole/Triméthoprime','Antibiotique sulfamide pour infections urinaires, respiratoires et digestives.',10000.00,90,'Medicament',1,1,NULL,'2025-06-18 01:51:22','2025-06-27 11:50:15',1),(327,'MED046','Escitalopram 10mg','Antidépresseur ISRS pour la dépression et les troubles anxieux généralisés.',4000.00,10,'Medicament',0,1,NULL,'2025-06-18 01:52:15','2025-06-27 11:50:15',1),(328,'MED047','Naproxène 500mg','Anti-inflammatoire non stéroïdien (AINS) pour douleurs, inflammation et fièvre.',7000.00,49,'Medicament',0,0,NULL,'2025-06-18 01:52:46','2025-06-27 11:50:15',1),(329,'MED048','Levofloxacine 500mg','Antibiotique fluoroquinolone pour infections respiratoires et urinaires sévères.',2400.00,60,'Medicament',0,1,NULL,'2025-06-18 01:53:19','2025-06-27 11:50:15',1),(330,'MED049','Rosuvastatine 10mg','Statine très efficace pour la réduction du cholestérol.',3900.00,140,'Medicament',0,1,NULL,'2025-06-18 01:53:57','2025-06-27 11:50:15',1),(331,'MED050','Duloxetine 60mg','Antidépresseur/Analgésique pour la dépression, l\'\'anxiété et les douleurs neuropathiques.',5500.00,90,'Medicament',0,1,NULL,'2025-06-18 01:54:32','2025-06-27 11:50:15',1),(332,'PARA001','Crème Hydratante Visage','Crème riche et non grasse pour hydrater en profondeur et protéger la peau du visage. Convient aux peaux sèches et sensibles.',15990.00,40,'Parapharmacie',NULL,NULL,'Soin Visage','2025-06-18 08:36:58','2025-07-06 15:01:01',0),(333,'PARA002','Lait Corporel Nourrissant','Lait hydratant et nourrissant pour le corps, enrichi en beurre de karité et huiles végétales. Pénètre rapidement.',1250.00,50,'Parapharmacie',NULL,NULL,'Soin Corps','2025-06-18 08:38:11','2025-06-18 08:38:11',0),(334,'PARA003','Gel Douche Douceur','Gel douche sans savon, respecte le pH physiologique de la peau. Idéal pour toute la famille, même les peaux délicates.',7200.00,100,'Parapharmacie',NULL,NULL,'Hygiène Corporelle','2025-06-18 08:38:59','2025-06-18 08:38:59',0),(335,'PARA004','Shampoing Usage Fréquent','Shampoing doux pour un usage quotidien, nettoie les cheveux en douceur sans les agresser. Convient à tous types de cheveux.',8900.00,60,'Parapharmacie',NULL,NULL,'Soin Cheveux','2025-06-18 08:39:40','2025-06-18 08:39:40',0),(336,'PARA005','Dentifrice Fluoré Protection Complète','Dentifrice avec fluorure pour une protection quotidienne contre les caries et la plaque dentaire. Haleine fraîche.',4100.00,900,'Parapharmacie',NULL,NULL,'Hygiène Buccale','2025-06-18 08:40:29','2025-06-26 15:46:42',0),(337,'PARA006','Brosse à Dents Souple','Brosse à dents avec poils souples pour un brossage efficace et respectueux des gencives sensibles.',3000.00,119,'Parapharmacie',NULL,NULL,'Hygiène Buccale','2025-06-18 08:42:07','2025-06-18 08:59:43',0),(338,'PARA007','Solution Hydroalcoolique 100ml','Gel désinfectant pour les mains, élimine 99.9% des bactéries et virus. Sans rinçage.',6500.00,80,'Parapharmacie',NULL,NULL,'Hygiène','2025-06-18 08:42:49','2025-06-18 08:42:49',0),(339,'PARA008','Pansements Adhésifs Assortiment','Boîte de pansements hypoallergéniques et résistants à l\'\'eau. Différentes tailles pour petits bobos.',5800.00,50,'Parapharmacie',NULL,NULL,'Premiers Soins','2025-06-18 08:43:38','2025-06-18 08:43:38',0),(340,'PARA009','Compresses Stériles Non Tissées','Paquet de compresses stériles pour le nettoyage et la protection des plaies.',4000.00,30,'Parapharmacie',NULL,NULL,'Premiers Soins','2025-06-18 08:44:18','2025-06-18 08:44:18',0),(341,'PARA010','Antiseptique Cutné Sans Alcool','Solution antiseptique douce pour désinfecter les plaies et la peau saine. Ne pique pas.',9200.00,340,'Parapharmacie',NULL,NULL,'Premiers Soins','2025-06-18 08:45:00','2025-06-18 08:45:00',0),(342,'PARA011','Thermomètre Frontal Infrarouge','Thermomètre sans contact pour une prise de température rapide et hygiénique. Idéal pour bébés.',25000.00,40,'Parapharmacie',NULL,NULL,'Dispositifs Médicaux','2025-06-18 08:45:41','2025-06-18 08:45:41',0),(343,'PARA012','Trousse de Premiers Soins Complète','Trousse compacte contenant l\'\'essentiel pour les urgences mineures à la maison ou en voyage.',35000.00,80,'Parapharmacie',NULL,NULL,'Premiers Soins','2025-06-18 08:46:22','2025-06-18 08:46:22',0),(344,'PARA013','Gants de Toilette Jetables','Paquet de gants à usage unique pour l\'\'hygiène corporelle des personnes alitées ou en voyage.',7000.00,600,'Parapharmacie',NULL,NULL,'Hygiène Corporelle','2025-06-18 08:47:00','2025-06-18 08:47:00',0),(345,'PARA014','Cotons-Tiges Boîte 200','Cotons-tiges en coton pur pour l\'\'hygiène des oreilles et le maquillage. Biodégradables.',2550.00,450,'Parapharmacie',NULL,NULL,'Hygiène','2025-06-18 08:47:38','2025-06-18 08:47:38',0),(346,'PARA015','Lingettes Nettoyantes Bébé','Lingettes ultra-douces et hypoallergéniques pour le change de bébé. Sans parfum ni alcool.',6000.00,700,'Parapharmacie',NULL,NULL,'Puériculture','2025-06-18 08:48:20','2025-06-18 08:48:20',0),(347,'PARA016','Lait Solaire SPF 50+','Protection solaire très haute pour le visage et le corps. Résistant à l\'\'eau. Pour peaux sensibles.',20000.00,520,'Parapharmacie',NULL,NULL,'Protection Solaire','2025-06-18 08:49:02','2025-06-18 08:49:02',0),(348,'PARA017','Après-Soleil Réparateur','Lait frais et apaisant pour hydrater la peau après une exposition au soleil. Prolonge le bronzage.',14000.00,20,'Parapharmacie',NULL,NULL,'Protection Solaire','2025-06-18 08:50:24','2025-06-18 08:50:24',0),(349,'PARA018','Baume à Lèvres Hydratant SPF 30','Baume protecteur pour les lèvres gercées ou exposées au soleil. Hydrate et répare.',4900.00,500,'Parapharmacie',NULL,NULL,'Soin Lèvres','2025-06-18 08:51:00','2025-06-18 08:51:00',0),(350,'PARA019','Savon de Marseille Liquide','Savon traditionnel pour l\'\'hygiène des mains et du corps. Formule naturelle et hypoallergénique.',9500.00,340,'Parapharmacie',NULL,NULL,'Hygiène Corporelle','2025-06-18 08:51:40','2025-06-18 08:51:40',0),(351,'PARA020','Nettoyant Auriculaire Doux','Solution douce pour l\'\'hygiène régulière des oreilles et la prévention des bouchons de cérumen.',10500.00,224,'Parapharmacie',NULL,NULL,'Hygiène','2025-06-18 08:52:23','2025-06-26 15:32:14',0),(352,'PARA021','Collyre Hydratant Yeux Secs','Gouttes oculaires apaisantes pour soulager la sécheresse oculaire et l\'\'irritation.',1180.00,200,'Parapharmacie',NULL,NULL,'Soin Yeux','2025-06-26 13:47:48','2025-06-26 13:47:48',0),(353,'PARA022','Masque Visage Purifiant Argile','Masque à l\'argile pour purifier la peau, resserrer les pores et matifier. Pour peaux mixtes à grasses.',16000.00,100,'Parapharmacie',NULL,NULL,'Soin Visage','2025-06-26 13:49:49','2025-06-26 13:49:49',0),(354,'PARA023','Serum Anti-age acide Hyaluronique','Sérum concentré en acide hyaluronique pour hydrater intensément et lisser les rides. Effet repulpant.',3500.00,120,'Parapharmacie',NULL,NULL,'Anti-age','2025-06-26 13:51:17','2025-06-27 11:14:51',0);
/*!40000 ALTER TABLE `produits` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transactions_comptables`
--

DROP TABLE IF EXISTS `transactions_comptables`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transactions_comptables` (
  `id_transaction` int NOT NULL AUTO_INCREMENT,
  `date_transaction` datetime NOT NULL,
  `reference_piece` varchar(255) DEFAULT NULL,
  `description_transaction` text NOT NULL,
  `montant` decimal(12,2) NOT NULL,
  `id_compte_debit` int NOT NULL,
  `id_compte_credit` int NOT NULL,
  `source_id` int DEFAULT NULL,
  `source_type` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id_transaction`),
  KEY `id_compte_debit` (`id_compte_debit`),
  KEY `id_compte_credit` (`id_compte_credit`),
  CONSTRAINT `transactions_comptables_ibfk_1` FOREIGN KEY (`id_compte_debit`) REFERENCES `comptes_comptables` (`id_compte`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `transactions_comptables_ibfk_2` FOREIGN KEY (`id_compte_credit`) REFERENCES `comptes_comptables` (`id_compte`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transactions_comptables`
--

LOCK TABLES `transactions_comptables` WRITE;
/*!40000 ALTER TABLE `transactions_comptables` DISABLE KEYS */;
INSERT INTO `transactions_comptables` VALUES (1,'2025-06-27 23:24:14','FACT-B6CACF2F','Vente HT (Facture FACT-B6CACF2F)',1587.10,7,4,43,'VENTE'),(2,'2025-06-27 23:24:14','FACT-B6CACF2F','Encaissement client (Facture FACT-B6CACF2F)',476.13,2,7,43,'ENCADD_VENTE_CLIENT'),(3,'2025-06-28 01:59:31','BC-20250628-025911','Approvisionnement Fact. Fournisseur Pharma Gabon (Réf: BC-20250628-025911) - Achats HT',2070.00,3,6,2,'ACHAT'),(4,'2025-06-28 01:59:31','BC-20250628-025911','Approvisionnement Fact. Fournisseur Pharma Gabon (Réf: BC-20250628-025911) - TVA Déductible',372.60,8,6,2,'ACHAT_TVA'),(5,'2025-06-28 02:07:18','FACT-AC6375E9','Vente HT (Facture FACT-AC6375E9)',57112.00,7,4,44,'VENTE'),(6,'2025-06-28 02:07:18','FACT-AC6375E9','Encaissement client (Facture FACT-AC6375E9)',11422.40,2,7,44,'ENCADD_VENTE_CLIENT'),(7,'2025-06-28 11:24:01','FACT-332AB555','Vente HT (Facture FACT-332AB555)',1587.10,7,4,45,'VENTE'),(8,'2025-06-28 11:24:01','FACT-332AB555','Encaissement client (Facture FACT-332AB555)',1587.10,2,7,45,'ENCADD_VENTE_CLIENT'),(9,'2025-06-28 11:28:06','FACT-5AA4070B','Vente HT (Facture FACT-5AA4070B)',1587.10,7,4,46,'VENTE'),(10,'2025-06-28 11:28:06','FACT-5AA4070B','Encaissement client (Facture FACT-5AA4070B)',476.13,2,7,46,'ENCADD_VENTE_CLIENT'),(11,'2025-06-28 12:40:00','BC-20250628-133948','Approvisionnement Fact. Fournisseur Pharma Gabon (Réf: BC-20250628-133948) - Achats HT',1000.00,3,1,6,'APPROVISIONNEMENT'),(12,'2025-06-28 12:40:00','BC-20250628-133948','Approvisionnement Fact. Fournisseur Pharma Gabon (Réf: BC-20250628-133948) - TVA Déductible',180.00,8,1,6,'APPROVISIONNEMENT_TVA'),(13,'2025-06-28 18:46:34','FACT-B0233F78','Vente HT (Facture FACT-B0233F78)',41205.60,7,4,47,'VENTE'),(14,'2025-06-28 18:46:34','FACT-B0233F78','Encaissement client (Facture FACT-B0233F78)',10301.40,2,7,47,'ENCADD_VENTE_CLIENT'),(15,'2025-06-29 18:26:16','FACT-6CA27CD9','Vente HT (Facture FACT-6CA27CD9)',11912.10,7,4,48,'VENTE'),(16,'2025-06-29 18:26:16','FACT-6CA27CD9','Encaissement client (Facture FACT-6CA27CD9)',1579.37,2,7,48,'ENCADD_VENTE_CLIENT'),(17,'2025-06-29 20:05:20','BC-20250629-210448','Approvisionnement Fact. Fournisseur Pharma Gabon (Réf: BC-20250629-210448) - Achats HT',92268.00,3,1,7,'APPROVISIONNEMENT'),(18,'2025-06-29 20:05:20','BC-20250629-210448','Approvisionnement Fact. Fournisseur Pharma Gabon (Réf: BC-20250629-210448) - TVA Déductible',16608.24,8,1,7,'APPROVISIONNEMENT_TVA'),(19,'2025-06-29 21:20:25','BC-20250629-221904','Approvisionnement Fact. Fournisseur Pharma Gabon (Réf: BC-20250629-221904) - Achats HT',400000.00,3,1,8,'APPROVISIONNEMENT'),(20,'2025-06-29 21:20:25','BC-20250629-221904','Approvisionnement Fact. Fournisseur Pharma Gabon (Réf: BC-20250629-221904) - TVA Déductible',72000.00,8,1,8,'APPROVISIONNEMENT_TVA'),(21,'2025-06-29 21:21:51','FACT-47D7E70B','Vente HT (Facture FACT-47D7E70B)',1115100.00,7,4,49,'VENTE'),(22,'2025-06-29 21:21:51','FACT-47D7E70B','Encaissement client (Facture FACT-47D7E70B)',1115100.00,2,7,49,'ENCADD_VENTE_CLIENT'),(23,'2025-06-30 18:10:47','FACT-2036AEB5','Vente HT (Facture FACT-2036AEB5)',1593.00,7,4,50,'VENTE'),(24,'2025-06-30 18:10:47','FACT-2036AEB5','Encaissement client (Facture FACT-2036AEB5)',318.60,2,7,50,'ENCADD_VENTE_CLIENT'),(25,'2025-06-30 19:07:26','BC-20250630-200704','Approvisionnement Fact. Fournisseur Pharma Gabon (Réf: BC-20250630-200704) - Achats HT',10000.00,3,1,9,'APPROVISIONNEMENT'),(26,'2025-06-30 19:07:26','BC-20250630-200704','Approvisionnement Fact. Fournisseur Pharma Gabon (Réf: BC-20250630-200704) - TVA Déductible',1800.00,8,1,9,'APPROVISIONNEMENT_TVA'),(27,'2025-06-30 19:08:13','FACT-51EAD6C4','Vente HT (Facture FACT-51EAD6C4)',5310.00,7,4,51,'VENTE'),(28,'2025-06-30 19:08:13','FACT-51EAD6C4','Encaissement client (Facture FACT-51EAD6C4)',1062.00,2,7,51,'ENCADD_VENTE_CLIENT'),(29,'2025-06-30 19:09:13','FACT-CC63A4A9','Vente HT (Facture FACT-CC63A4A9)',10684.90,7,4,52,'VENTE'),(30,'2025-06-30 19:09:13','FACT-CC63A4A9','Encaissement client (Facture FACT-CC63A4A9)',106.85,2,7,52,'ENCADD_VENTE_CLIENT'),(31,'2025-07-02 17:08:17','FACT-8A74F808','Vente HT (Facture FACT-8A74F808)',11499.10,7,4,53,'VENTE'),(32,'2025-07-02 17:08:17','FACT-8A74F808','Encaissement client (Facture FACT-8A74F808)',11499.10,2,7,53,'ENCADD_VENTE_CLIENT'),(33,'2025-07-06 14:01:01','BC-20250706-150023','Approvisionnement Fact. Fournisseur Pharma Gabon (Réf: BC-20250706-150023) - Achats HT',100000.00,3,1,10,'APPROVISIONNEMENT'),(34,'2025-07-06 14:01:01','BC-20250706-150023','Approvisionnement Fact. Fournisseur Pharma Gabon (Réf: BC-20250706-150023) - TVA Déductible',18000.00,8,1,10,'APPROVISIONNEMENT_TVA'),(35,'2025-07-06 14:02:45','FACT-8E3236C2','Vente HT (Facture FACT-8E3236C2)',8755.60,7,4,54,'VENTE'),(36,'2025-07-06 14:02:45','FACT-8E3236C2','Encaissement client (Facture FACT-8E3236C2)',1751.12,2,7,54,'ENCADD_VENTE_CLIENT'),(37,'2025-07-08 12:57:45','FACT-B774E666','Vente HT (Facture FACT-B774E666)',6543.10,7,4,55,'VENTE'),(38,'2025-07-08 12:57:45','FACT-B774E666','Encaissement client (Facture FACT-B774E666)',1308.62,2,7,55,'ENCADD_VENTE_CLIENT');
/*!40000 ALTER TABLE `transactions_comptables` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `utilisateurs`
--

DROP TABLE IF EXISTS `utilisateurs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `utilisateurs` (
  `id_utilisateur` int NOT NULL AUTO_INCREMENT,
  `nom_utilisateur` varchar(50) DEFAULT NULL,
  `mot_de_passe_hash` varchar(50) DEFAULT NULL,
  `role` varchar(50) NOT NULL,
  UNIQUE KEY `id_utilsateur` (`id_utilisateur`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `utilisateurs`
--

LOCK TABLES `utilisateurs` WRITE;
/*!40000 ALTER TABLE `utilisateurs` DISABLE KEYS */;
INSERT INTO `utilisateurs` VALUES (1,'davidibinga','adminpass123','admin'),(4,'Jeanne','vendeur123','vendeur'),(5,'Anne','vendeur123','vendeur'),(7,'Larry','vendeur123','vendeur');
/*!40000 ALTER TABLE `utilisateurs` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-07-09 18:16:57

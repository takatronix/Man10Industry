CREATE TABLE `player_data` (
  `key` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `player_uuid` varchar(40) DEFAULT NULL,
  `skill_id` int(11) DEFAULT NULL,
  `level` int(11) DEFAULT NULL,
  PRIMARY KEY (`key`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=latin1;
package red.man10.man10industry

import org.bukkit.Bukkit
import red.man10.MIPlugin
import red.man10.PlayerSkillData
import java.util.*

class MISkillData(val pl: MIPlugin) {

    var currentPlayerData: MutableMap<UUID, PlayerSkillData> = mutableMapOf()

//    fun loadAllDataFromPlayer(uuid: UUID, mysql: MySQLManager) { //OnPlayerLogin
//        var playerData: PlayerSkillData = mutableMapOf()
//        var allSkillData = mysql.query("select * from player_data where player_uuid = '$uuid'")
//        var idCount = 1
//        if (allSkillData.isLast) {
//            mysql.execute("insert into player_data values (0, '" + uuid.toString() + "', " + 1 + ", 0)")
//        }
//        while (allSkillData!!.next()) {
//            if (allSkillData.getInt("skill_id") == idCount) {
//                playerData.put(idCount, allSkillData.getInt("level"))
//            } else { //データーがなかったら
//                mysql.execute("insert into player_data values (0, '" + uuid.toString() + "', " + idCount.toString() + ", 0)")
//                playerData.put(idCount, 0)
//            }
//            idCount++
//        }
////        for (skillId in 1..pl.skills.count()) {
////            val skillData = mysql.query("select * from player_data where player_uuid = '$uuid' AND skill_id = '$skillId'")
////            if (!(skillData!!.next())) { //データーなかったら:
////                mysql.execute("insert into player_data values (0, '" + uuid.toString() + "', " + skillId.toString() + ", 0)")
////                playerData.put(skillId, 0)
////            } else { //あったら:
////                print(skillData.getInt("level"))
////                playerData.put(skillId, skillData.getInt("level"))
////            }
////        }
//
//        mysql.close()
//
//        pl.currentPlayerData[uuid] = playerData
//    }
//
//    fun saveAllDataFromPlayer(uuid: UUID, mysql: MySQLManager) { //OnPLayerLogout
//        val playerData = pl.currentPlayerData[uuid]!!
//
//        for (skillId in 1..pl.skills.count()) {
//            val level = playerData[skillId]!!
//            setPlayerData(uuid, level, mysql, skillId)
//        }
//
//        pl.currentPlayerData.remove(uuid)
//    }

}

class getPlayerData(val pl: MIPlugin): Thread(){

    override fun run() {

        val mysql = MySQLManager(pl, "MIGET")

        val skillValue = mysql.query("select * from player_data") ?: return

        while (skillValue.next()){
            val data = pl.skill.currentPlayerData
            if (data.containsKey(UUID.fromString(skillValue.getString("player_uuid")))){
                val map2 = data[UUID.fromString(skillValue.getString("player_uuid"))]
                map2!![skillValue.getInt("skill_id")] = skillValue.getInt("level")
            }else{
                data[UUID.fromString(skillValue.getString("player_uuid"))] = mutableMapOf(skillValue.getInt("skill_id") to skillValue.getInt("level"))
            }
            pl.skill.currentPlayerData = data
        }

        skillValue.close()

        val skilllimit = mysql.query("select * from player_skill_limit") ?: return

        while (skilllimit.next()){
            pl.player_slimit[UUID.fromString(skilllimit.getString("uuid"))] = skilllimit.getInt("skill_limit")
        }

        skilllimit.close()

        mysql.close()
    }

}

class setPlayerData(val pl: MIPlugin): Thread(){

    override fun run() {

        val skill = pl.skill.currentPlayerData

        val cs = Bukkit.getConsoleSender()

        val mysql = MySQLManager(pl, "MISET")

        for (i in skill) {
            for (j in i.value){
                val skillValue = mysql.query("SELECT COUNT(*) FROM player_data where player_uuid = '${i.key}' and skill_id = '${j.key}'")
                skillValue!!.first()
                if (skillValue.getInt("COUNT(*)") == 0) {
                    //No Data In DB, Create New
                    mysql.execute("insert into player_data values (0, '" + i.key.toString() + "', " + j.key.toString() + ", " + j.value + ") ")
                } else {
                    //Data ready
                    mysql.execute("update player_data set level = " + j.value + " where player_uuid = '" + i.key.toString() + "' and skill_id = " + j.key.toString())
                }
            }

        }

        val limit = pl.player_slimit

        cs.sendMessage(limit.toString())

        for (i in limit){

            val rs = mysql.query("SELECT COUNT(*) FROM player_skill_limit where uuid = '${i.key}'")

            rs!!.first()

            if (rs.getInt("COUNT(*)") == 0){

                cs.sendMessage(limit.toString())
                mysql.execute("INSERT INTO `player_skill_limit` (`uuid`, `skill_limit`) VALUES ('${i.key}', '${i.value}');")

            }else{

                mysql.execute("UPDATE `player_skill_limit` SET `skill_limit`='${i.value}' WHERE `uuid`='${i.key}'")

            }

        }

        mysql.close()
    }

}

class setData(val pl: MIPlugin): TimerTask(){

    override fun run() {

        val cs = Bukkit.getConsoleSender()

        cs.sendMessage("${pl.prefix}§a定期セーブ開始")

        val skill = pl.skill.currentPlayerData

        val mysql = MySQLManager(pl, "MISET")

        for (i in skill) {
            for (j in i.value){
                val skillValue = mysql.query("SELECT COUNT(*) FROM player_data where player_uuid = '${i.key}' and skill_id = '${j.key}'")

                skillValue!!.first()

                if (skillValue.getInt("COUNT(*)") == 0) {
                    //No Data In DB, Create New
                    mysql.execute("insert into player_data values (0, '" + i.key.toString() + "', " + j.key.toString() + ", " + j.value + ") ")
                } else {
                    //Data ready
                    mysql.execute("update player_data set level = " + j.value + " where player_uuid = '" + i.key.toString() + "' and skill_id = " + j.key.toString())
                }
            }

        }

        val limit = pl.player_slimit

        for (i in limit){

            val rs = mysql.query("SELECT COUNT(*) FROM player_skill_limit where uuid = '${i.key}'")

            rs!!.first()

            if (rs.getInt("COUNT(*)") == 0){

                cs.sendMessage("aaa")
                mysql.execute("INSERT INTO `player_skill_limit` (`uuid`, `skill_limit`) VALUES ('${i.key}', '${i.value}');")

            }else{

                mysql.execute("UPDATE `player_skill_limit` SET `skill_limit`='${i.value}' WHERE `uuid`='${i.key}'")

            }

        }

        mysql.close()
        cs.sendMessage("${pl.prefix}§a定期セーブ完了")
    }

}

class createTable(val pl: MIPlugin): Thread(){

    override fun run() {

        val mysql = MySQLManager(pl, "MICREATE")

        val cs = Bukkit.getConsoleSender()

        cs.sendMessage("${pl.prefix}§aプレイヤーデータテーブル作成開始")

        val rs = mysql.execute("CREATE TABLE `player_data` (\n" +
                "  `key` int(11) unsigned NOT NULL AUTO_INCREMENT,\n" +
                "  `player_uuid` varchar(40) DEFAULT NULL,\n" +
                "  `skill_id` int(11) DEFAULT NULL,\n" +
                "  `level` int(11) DEFAULT NULL,\n" +
                "  PRIMARY KEY (`key`)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=latin1;")

        if (rs){
            cs.sendMessage("${pl.prefix}§a作成成功")
        }else{
            cs.sendMessage("${pl.prefix}§c作成失敗")
        }

        cs.sendMessage("${pl.prefix}§aスキルレベル上限テーブル作成開始")

        val rs2 = mysql.execute("CREATE TABLE `player_skill_limit` (\n" +
                "  `key` int(11) NOT NULL AUTO_INCREMENT,\n" +
                "  `uuid` varchar(40) NOT NULL,\n" +
                "  `skill_limit` int(11) NOT NULL DEFAULT '500',\n" +
                "  PRIMARY KEY (`key`,`uuid`,`skill_limit`)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8;")

        if (rs2){
            cs.sendMessage("${pl.prefix}§a作成成功")
        }else{
            cs.sendMessage("${pl.prefix}§c作成失敗")
        }

    }

}
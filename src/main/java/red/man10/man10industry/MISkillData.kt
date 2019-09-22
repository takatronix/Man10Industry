package red.man10.man10industry

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import red.man10.MIPlugin
import red.man10.PlayerSkillData
import java.util.*

class MISkillData(val pl: MIPlugin) {



    fun save(){
        val cs = Bukkit.getConsoleSender()

        cs.sendMessage("${pl.prefix}§aセーブ開始")

        val skill = pl.playerData

        val mysql = MySQLManager(pl,"MISET")

        for( s in skill) {
            for (v in s.value){

                mysql.execute("UPDATE player_data set level = '${v.value}' where player_uuid = '${s.key}' and skill_id = '${v.key}'")
            }
        }

        for(l in pl.player_slimit){
            mysql.execute("UPDATE `player_skill_limit` SET `skill_limit`='${l.value}' WHERE `uuid`='${l.key}'")
        }

        cs.sendMessage("${pl.prefix}§aセーブ完了")

    }

    fun save(p:Player){
        val cs = Bukkit.getConsoleSender()

        cs.sendMessage("${pl.prefix}§aセーブ開始")

        val mysql = MySQLManager(pl,"MISET")


        for (skill in pl.playerData[p.uniqueId]!!){
            mysql.execute("UPDATE player_data set level = '${skill.value}' where player_uuid = '${p.uniqueId}' and skill_id = '${skill.key}'")
        }
        mysql.execute("UPDATE `player_skill_limit` SET `skill_limit`='${pl.player_slimit[p.uniqueId]}' WHERE `uuid`='${p.uniqueId}'")

        pl.player_slimit.remove(p.uniqueId)
        pl.playerData.remove(p.uniqueId)

        cs.sendMessage("${pl.prefix}§aセーブ完了")

    }


    fun load(p: Player){

        val map = PlayerSkillData()

        val mysql = MySQLManager(pl,"mi load")

        pl.playerData.remove(p.uniqueId)
        pl.player_slimit.remove(p.uniqueId)

        var data = mysql.query("SELECT * FROM player_data WHERE player_uuid='${p.uniqueId}'")

        if (!data.next()){
            for (i in 0 until pl.skills.size){
                mysql.execute("INSERT INTO `man10industry`.`player_data` (`player_uuid`, `skill_id`, `level`) VALUES ('${p.uniqueId}', '${i+1}', '0');")
            }
        }
        data = mysql.query("SELECT * FROM player_data WHERE player_uuid='${p.uniqueId}'")

        while (data.next()){
            map[data.getInt("skill_id")] = data.getInt("level")
        }

        pl.playerData[p.uniqueId] = map

        data.close()

        mysql.close()

        val limit = mysql.query("SELECT * from player_skill_limit")

        if (!limit.next()){
            mysql.execute("INSERT INTO `man10industry`.`player_skill_limit` (`uuid`) VALUES ('${p.uniqueId}');")
        }

        limit.beforeFirst()

        while (limit.next()){
            pl.player_slimit[UUID.fromString(limit.getString("uuid"))] = limit.getInt("skill_limit")
        }
        limit.close()

        mysql.close()
        Bukkit.getLogger().info("${p.name} ... loaded")
    }

    fun delete(p:Player){
        val mysql = MySQLManager(pl,"MIDELETE")

        mysql.execute("DELETE FROM `player_data` WHERE  `player_uuid`='${p.uniqueId}';")

        mysql.execute("DELETE FROM `player_skill_limit` WHERE `uuid`='${p.uniqueId}';")
    }


}




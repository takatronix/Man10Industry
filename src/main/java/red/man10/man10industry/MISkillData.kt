package red.man10.man10industry

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import red.man10.MIPlugin
import java.util.*

class MISkillData(val pl: MIPlugin) {



    fun save(){
        val cs = Bukkit.getConsoleSender()

        cs.sendMessage("${pl.prefix}§aセーブ開始")

        val skill = pl.currentPlayerData

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


    fun load(p: Player){

        pl.currentPlayerData[p.uniqueId] = mutableMapOf()

        val mysql = MySQLManager(pl,"mi load")

        val data = mysql.query("SELECT * FROM player_data WHERE player_uuid='${p.uniqueId}'")

        if (!data.next()){
            for (i in 0 until pl.skills.size){
                mysql.execute("INSERT INTO `man10industry`.`player_data` (`player_uuid`, `skill_id`, `level`) VALUES ('${p.uniqueId}', '$i', '0');")
            }
        }

        data.beforeFirst()

        while (data.next()){
            pl.currentPlayerData[p.uniqueId] = mutableMapOf(data.getInt("skill_id") to data.getInt("level"))
        }

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


}




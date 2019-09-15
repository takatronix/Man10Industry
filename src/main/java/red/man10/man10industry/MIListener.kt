package red.man10.man10industry

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerJoinEvent
import red.man10.MIPlugin


class MIListener(val pl: MIPlugin): Listener {

    @EventHandler
    fun onInvClose(e: InventoryCloseEvent) {

        val inv = e.inventory

        if (inv.name == pl.prefix + "§0加工メニュー"){


            loop@for (i in 0 until 45){

                val item = inv.getItem(i) ?: continue@loop

                e.player.inventory.addItem(item)

            }


        }

    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        
        if (!pl.skill.currentPlayerData.containsKey(e.player.uniqueId) || pl.skill.currentPlayerData[e.player.uniqueId] == null || pl.skill.currentPlayerData[e.player.uniqueId]!!.isEmpty()) {
            val p = mutableMapOf<Int, Int>()

            for(i in 0 until pl.skills.size){
                p[i+1] = 0
            }
            pl.skill.currentPlayerData.put(e.player.uniqueId, p)
        }

        if (!pl.player_slimit.containsKey(e.player.uniqueId) || pl.player_slimit[e.player.uniqueId] == null){

            pl.player_slimit[e.player.uniqueId] = 500

        }

    }
}
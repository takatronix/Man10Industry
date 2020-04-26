package red.man10.man10industry

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import red.man10.MIPlugin


class MIListener(val pl: MIPlugin): Listener {

    @EventHandler
    fun onInvClose(e: InventoryCloseEvent) {

        val inv = e.player.openInventory

        if (inv.type != InventoryType.CHEST) return

        if (inv.title == pl.prefix + "§0加工メニュー"){


            loop@for (i in 0 until 45){

                val item = inv.getItem(i) ?: continue@loop

                e.player.inventory.addItem(item)

            }


        }

    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        

        Thread(Runnable {
            pl.skill.load(e.player)
        }).start()

    }

    @EventHandler
    fun onPlayerLeft(e:PlayerQuitEvent){
        Thread(Runnable {
            pl.skill.save(e.player)
        }).start()

    }
}
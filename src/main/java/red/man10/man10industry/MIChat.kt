package red.man10.man10industry

import org.bukkit.entity.Player
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent


class MIChat(val pl: MIPlugin) {

    fun showTopMenu(p: Player) {
        p.sendMessage("§e********** §b§lm§3§lIndustry §f§lTop Menu §e**********")
        sendHoverText(p, "§b自分のスキルレベルを確認 §f=> §6§l[§7§lスキル確認§6§l]", "§bクリックでスキル確認", "/mi myskill")

        p.sendMessage("§e****************************************")
    }

    fun sendHoverText(p: Player, text: String, hoverText: String?, command: String?) {
        //////////////////////////////////////////
        //      ホバーテキストとイベントを作成する
        var hoverEvent: HoverEvent? = null
        if (hoverText != null) {
            val hover = ComponentBuilder(hoverText).create()
            hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, hover)
        }

        //////////////////////////////////////////
        //   クリックイベントを作成する
        var clickEvent: ClickEvent? = null
        if (command != null) {
            clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, command)
        }

        val message = ComponentBuilder(text).event(hoverEvent).event(clickEvent).create()
        p.spigot().sendMessage(*message)
    }

    fun returnProgressBar(value: Double): String {
        var progressBar = ""
        val doneBar = (value * 20).toInt()
        for (i in 1..doneBar) {
            progressBar += "§e▮"
        }
        for (i in 1..(20 - doneBar)) {
            progressBar += "§7▮"
        }
        return progressBar
    }
}
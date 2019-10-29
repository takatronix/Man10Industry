package red.man10.man10industry

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.awt.Graphics2D
import org.bukkit.entity.Player
import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import red.man10.MIPlugin
import red.man10.PlayerSkillData
import red.man10.man10industry.models.Machine


class MIMachine(val pl: MIPlugin) {

    /*
    fun process(p: PlayerSkillData, machine: Machine, inputs: MutableList<ItemStack>): Pair<MutableList<ItemStack>,MutableList<ItemStack>> {
        print(inputs)
        var usableRecieps = mutableListOf<Recipe>()
        for (recipe in machine.recipes) {
            var result = checkIfAContainsAllItemsOfB(recipe.inputs, inputs)
            print(result)
            if (result.first) {
                usableRecieps.add(recipe)
                print(result.second)
            }
        }
        print(usableRecieps)

        usableRecieps.filter {
            for (chanceSetWithSkill in it.chanceSets) {
                if (p[chanceSetWithSkill.key]!!.toInt() < chanceSetWithSkill.value.req) {
                    return@filter true
                }
            }
            return@filter false
        }

        print(":::")
        return Pair(mutableListOf(ItemStack(Material.APPLE, 10)),)
    }*/

    fun process(p: PlayerSkillData, machine: Machine, inputs: MutableList<ItemStack>, pla: Player): MutableList<ItemStack>? {
        for (recipe in machine.recipes) {
            if (recipe.inputs != inputs){
                continue
            }

            val chance = recipe.chanceSets
            val skillid = mutableListOf<Int>()
            for (c in chance) {
                for (i in 0 until pl.skills.size) {
                    if (pl.skills[i] == c.key){
                        skillid.add(i)
                        continue
                    }
                }
            }

            if (skillid.size == 0) return mutableListOf(ItemStack(Material.AIR))

            var flags = true

            for (i in skillid){
                val level = chance[pl.skills[i]]

                if (p[i]!! < level!!.req ){
                    pla.sendMessage("${pl.prefix}§cレベルが足りません")
                    return inputs
                }

                var min = 0.0
                for (l in level.chances){
                    if (p[i]!! >= l.key){
                        min = l.value
                    }
                }

                if (min < Math.random()){

                    flags = false

                    if (min < Math.random() && p[i]!! < 100 && pl.player_slimit[pla.uniqueId]!! > 0) {
                        pla.sendMessage("${pl.prefix}§e${pl.skills[i-1].name}スキル§aがレベルアップしました！§6[§f${p[i]!!}Lv->${p[i]!! + 1}Lv§6]")
                        val s = pl.playerData[pla.uniqueId]
                        s!![i] = p[i]!! + 1
                        pl.player_slimit[pla.uniqueId] = pl.player_slimit[pla.uniqueId]!! - 1
                    }
                }
            }

            if (!flags)return null

            return recipe.outputs

        }
        return mutableListOf(ItemStack(Material.AIR))
    }

    fun createMapItem(machineKey: String): ItemStack {
        var map = MappRenderer.getMapItem(pl, machineKey)
        map.itemMeta.displayName = "§b§l" + pl.machines[machineKey]!!.name + "§r§7(100/100)"
        return map
    }

    fun createAllMachineMapp(){


        for (machine in pl.machines) {

            MappRenderer.draw(machine.key, 0) { _: String, _: Int, g: Graphics2D ->

                //      画面更新をする
                val result = drawImage(g, machine.value.imageName, 0, 0, 128, 128)
                if (!result) {
                    g.drawString("No Image Found", 10, 10)
                }
                true
            }

            MappRenderer.displayTouchEvent(machine.key) { key: String, _: Int, player: Player, _: Int, _: Int ->
                if (player.hasPermission("mi.use")) {
                    pl.gui.openProcessingView(player, key)
                }
                true
            }
        }
    }

    fun drawImage(g: Graphics2D, imageKey: String, x: Int, y: Int, w: Int, h: Int): Boolean {
        val image = MappRenderer.image(imageKey)
        if (image == null) {
            Bukkit.getLogger().warning("no image:$imageKey")
            return false
        }

        g.drawImage(image, x, y, w, h, null)

        return true
    }
}
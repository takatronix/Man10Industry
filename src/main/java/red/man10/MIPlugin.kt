package red.man10

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import red.man10.man10industry.*
import red.man10.man10industry.models.Machine
import red.man10.man10industry.models.Recipe
import red.man10.man10industry.models.Skill
import red.man10.man10industry.models.*
import java.io.File
import java.lang.NumberFormatException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap

typealias PlayerSkillData = ConcurrentHashMap<Int,Int>

class MIPlugin : JavaPlugin() {

    val prefix = "§a[§bm§3Industry§a] §b"

    val ryml = File(this.dataFolder, "recipes")

    val unsealed = HashMap<UUID, MutableList<String>>()
    val listener = MIListener(this)
    val db = MIDatabase(this)
    val config = MIConfig(this)
    val util = MIUtility(this)
    val gui = MIGUI(this)
    val skill = MISkillData(this)
    val chat = MIChat(this)
    val machine = MIMachine(this)

    var chanceSets = HashMap<String, ChanceSet>()
    var recipies = HashMap<String, Recipe>()
    var skills = mutableListOf<Skill>()
    var machines = HashMap<String, Machine>()

    val playerData= ConcurrentHashMap<UUID, PlayerSkillData>()

    val player_slimit = ConcurrentHashMap<UUID, Int>()

    val setRecipe = HashMap<Player, SetRecipe>()

    //var mysql: MySQLManager? = null

    var isLocked = false

    override fun onEnable() {

        //val mysql = MySQLManager(this, "MI_ConnectionTest")

//        if (!(mysql.connected)) {
//            isLocked = true
//            broadcastMessage(prefix + "§aDatabase Error - §fLocking mIndustry")
//            //return
//        }

        ryml.mkdir()

        server.pluginManager.registerEvents(listener, this)
        server.pluginManager.registerEvents(gui, this)

        saveDefaultConfig()

        config.loadAll(Bukkit.getConsoleSender(), true)

        for (player in Bukkit.getOnlinePlayers()) {
            skill.load(player)
        }

        Thread(Runnable {
            while (true){
                Thread.sleep(3600000)
                skill.save()
            }
        }).start()

        this.createDefaultImagesAndSettings()

    }

    override fun onDisable() {
        // Plugin shutdown logic
        skill.save()
    }


    fun createDefaultImagesAndSettings() {
        try {

            //      画像フォルダがなければ作成
            val file = File(dataFolder, "images")
            if (!file.exists()) {
                file.mkdirs()
                saveResource("images", false)
            }

            saveResource("images/melter.png", false)
            saveResource("chance_sets.yml", false)
            saveResource("machines.yml", false)
            saveResource("skills.yml", false)


        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }



    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<String>): Boolean {
        if (sender is Player) {
            if (isLocked) {
                sender.sendMessage("$prefix§aPlugin Locked")
                if (sender.hasPermission("mi.op")){
                    sender.sendMessage("$prefix(Bypassing)")
                } else {
                    return true
                }
            }
            if (sender.hasPermission("mi.use")){
                when (args.size) {
                    0 -> {
                        chat.showTopMenu(sender)
                        return true
                    }
                    1 -> {
                        when (args[0]) {
                            "myskill" -> {

//                                if (!skill.currentPlayerData.containsKey(sender.uniqueId) || skill.currentPlayerData[sender.uniqueId] == null || skill.currentPlayerData[sender.uniqueId]!!.isEmpty()) {
//                                    val p = mutableMapOf<Int, Int>()
//
//                                    for(i in 0 until skills.size){
//                                        p[i+1] = 0
//                                    }
//                                    skill.currentPlayerData[sender.uniqueId] = p
//                                }
//
//                                if (!player_slimit.containsKey(sender.uniqueId) || player_slimit[sender.uniqueId] == null){
//                                    player_slimit[sender.uniqueId] = 500
//                                }

                                var skillId = 1
                                for (skill in skills) {
                                    when (skillId) {
                                        1 -> {
                                            sender.sendMessage("$prefix§e加工スキル:")
                                        }
                                        5 -> {
                                            sender.sendMessage("$prefix§e魔法スキル:")
                                        }
                                        9 -> {
                                            sender.sendMessage("$prefix§e学問スキル:")
                                        }
                                        13 -> {
                                            sender.sendMessage("$prefix§eスペシャルスキル:")
                                        }
                                    }
                                    val skillArrow = "§8 " + "〉".repeat(7 - skill.name.length)


                                    val level = playerData[sender.uniqueId]!![skillId]!!
                                    sender.sendMessage(prefix +
                                            "§b " + skill.name +
                                            skillArrow +
                                            "§a " + String.format("%3s", level.toString()) + "Lv " +
                                            chat.returnProgressBar((level.toDouble()/100)))
                                    //sender.sendMessage(prefix + "§b" + skill.name + " §a" + currentPlayerData[sender.uniqueId]!![skillId] + "Lv - " + chat.returnProgressBar(0.3))
                                    //sender.sendMessage(prefix + "§b" + chat.returnProgressBar(0.3) + " §b" + skill.name + " §a" + currentPlayerData[sender.uniqueId]!![skillId] + "Lv ")

                                    skillId++
                                }
                                return true
                            }
                        }
                    }
                }
            }
            if (sender.hasPermission("mi.op")) {
                when (args.size) {
                    0 -> {
                    }
                    1 -> {
                        when (args[0]) {
                            "help" -> {
                                showHelp(sender)
                                return true
                            }
                            "reload" -> {
                                config.loadAll(sender, false)
                                return true
                            }
                            "list" -> {
                                showList(sender)
                                return true
                            }
                            "save" ->{
                                Thread(Runnable {
                                    skill.save()
                                }).start()
                                return true
                            }
                        }
                    }
                    2 -> {
                        when (args[0]) {


                            "usemachine" -> {
                                gui.openProcessingView(sender, args[1])
                                return true
                            }
                            "setinput" -> {
                                gui.openInputSetView(sender, args[1])
                                return true
                            }
                            "setoutput" -> {
                                gui.openOutputSetView(sender, args[1])
                                return true
                            }
                            "update" -> {
                                playerData[Bukkit.getPlayer(args[1]).uniqueId]!!.clear()
                            }
                            "get" -> {
                                if (machines[args[1]] != null) {
                                    sender.inventory.addItem(machine.createMapItem(args[1]))
                                    return true
                                }
                            }
                            "deleterecipe" ->{
                                Thread(Runnable {
                                    config.deleteRecipe(args[1], sender)
                                }).start()
                                return true
                            }
                            "deletemachine" ->{
                                Thread(Runnable {
                                    config.deleteMachine(args[1], sender)
                                }).start()
                                return true
                            }
                            "deletechance" ->{
                                Thread(Runnable {
                                    config.deleteChance(args[1], sender)
                                }).start()
                                return true
                            }
                            "showrecipe" ->{
                                Thread(Runnable {

                                    for (recipe in config.showRecipes(args[1])){
                                        sender.sendMessage("$prefix§a$recipe")
                                    }
                                }).start()
                                return true
                            }
                            "fix" ->{
                                Thread(Runnable {
                                    skill.delete(Bukkit.getPlayer(args[1]))
                                    skill.load(Bukkit.getPlayer(args[1]))
                                    sender.sendMessage("$prefix§afixed player data")
                                }).start()
                                return true
                            }
                        }
                    }
                    3 -> {
                        when(args[0]) {

                            "seal" -> {

                                if (!recipies.containsKey(args[2])){
                                    sender.sendMessage("$prefix§bthe recipeId does not exist")
                                    return true
                                }

                                val p = Bukkit.getPlayer(args[1])

                                if (!unsealed.containsKey(p.uniqueId)){
                                    sender.sendMessage("$prefix§bthe player has already sealed")
                                    return true
                                }

                                if (!unsealed[p.uniqueId]!!.contains(args[2])){
                                    sender.sendMessage("$prefix§bthe player has already sealed")
                                    return true
                                }

                                unsealed[p.uniqueId]!!.remove(args[2])

                                config.setUnsealed()

                                sender.sendMessage("$prefix§bSealed. You have to reload plugin /mi reload.")
                                return true
                            }

                            "unseal" -> {

                                if (!recipies.containsKey(args[2])){
                                    sender.sendMessage("$prefix§bthe recipeId does not exist")
                                    return true
                                }

                                val p = Bukkit.getPlayer(args[1])



                                if (!unsealed.containsKey(p.uniqueId)){
                                    unsealed[p.uniqueId]!!.add(args[2])
                                }else{
                                    if (unsealed[p.uniqueId]!!.contains(args[2])){
                                        sender.sendMessage("$prefix§bthe player has already unsealed")
                                        return true
                                    }
                                    unsealed[p.uniqueId] = mutableListOf(args[2])
                                }

                                config.setUnsealed()

                                sender.sendMessage("$prefix§bUnsealed. You have to reload plugin /mi reload.")
                                return true
                            }
                            "info" -> {
                                when (args[1]) {
                                    "c" -> {
                                        if (chanceSets[args[2]]  != null) {
                                            sender.sendMessage(prefix + "§bData of ChanceSet: " + args[2])
                                            sender.sendMessage(prefix + "§7" + chanceSets[args[2]])
                                        } else {
                                            sender.sendMessage(prefix + "§bChanceSet: " + args[2] + " doesn't exist")
                                        }
                                        return true
                                    }
                                    "m" -> {
                                        if (machines[args[2]] != null) {
                                            sender.sendMessage(prefix + "§bData of Machine: " + args[2])
                                            sender.sendMessage(prefix + "§7" + machines[args[2]])
                                        } else {
                                            sender.sendMessage(prefix + "§bMachine: " + args[2] + " doesn't exist")
                                        }
                                        return true
                                    }
                                    "r" -> {
                                        if (recipies[args[2]] != null) {
                                            sender.sendMessage(prefix + "§bData of Recipe: " + args[2])
                                            sender.sendMessage(prefix + "§7" + recipies[args[2]])
                                        } else {
                                            sender.sendMessage(prefix + "§bRecipe: " + args[2] + " doesn't exist")
                                        }
                                        return true
                                    }
                                }
                            }

                        }
                    }
                    4 -> {
                        when(args[0]) {
                            "setlevel" -> {
                                val targetPlayer = Bukkit.getPlayer(args[1])
                                if (targetPlayer == null) {
                                    sender.sendMessage(prefix + "§bPlayer doesn't exist")
                                    return true
                                }
                                //val targetSkill = skills[args[2].toInt() + 1]
                                if (skills.size < args[2].toInt()) {
                                    sender.sendMessage(prefix + "§bSkill doesn't exist")
                                    return true
                                }
                                val level = args[3].toInt()
                                if (level < 0 || level > 100) {
                                    sender.sendMessage(prefix + "§bLevel value is invalid")
                                    return true
                                }
                                if (!playerData.containsKey(targetPlayer.uniqueId)){
                                    playerData[targetPlayer.uniqueId]!![args[2].toInt()] = level
                                }else{
                                    val s = playerData[targetPlayer.uniqueId]
                                    s!![args[2].toInt()] = level
                                }
                                sender.sendMessage(prefix + "§bLevel set")
                                return true
                            }

                            "createmachine" -> {
                                Thread(Runnable {
                                    config.createMachine(args[1],args[2],args[3])
                                    sender.sendMessage("$prefix§bCreate new machine. You have to reload plugin /mi reload.")
                                }).start()
                                return true
                            }
                            "createchance" -> {
                                Thread(Runnable {
                                    config.createChance(args[1],args[2],args[3])
                                    sender.sendMessage("$prefix§bCreate new chance. You have to reload plugin /mi reload.")
                                }).start()
                                return true
                            }


                        }

                    }

                    5->{
                        when(args[0]) {
                            "createrecipe" -> {

                                if (recipies.containsKey(args[1])) {
                                    sender.sendMessage("$prefix§bthe recipeId has already used")
                                    return true
                                }

                                if (!machines.containsKey(args[3])) {
                                    sender.sendMessage("$prefix§bthe machineId does not exist")
                                    return true
                                }

                                try {
                                    args[4].toInt()
                                } catch (e: NumberFormatException){
                                    sender.sendMessage("$prefix§bsealed is only number 1 or 0")
                                    return true
                                }

                                config.createRecipe(args[1], args[2], args[3], sender, args[4].toInt())
                                return true
                            }
                        }
                    }

                }
            }
            warnWrongCommand(sender)
        } else {
            sender.sendMessage(prefix + "Can't run from console.")
            return true
        }
        return false
    }

    fun showHelp(sender: CommandSender) {
        sender.sendMessage("§a******** §b§lm§3§lIndustry §a********")
        sender.sendMessage("§bData Managements")
        sender.sendMessage("§3/mi reload §7Load all data")
        sender.sendMessage("§3/mi list §7View all CS, Machines, Recipes, Skills")
        sender.sendMessage("§3/mi info [c/m/r] [key] §7View specific data of CS/ Machine/ Recipe")
        sender.sendMessage("§3/mi usemachine [machineKey] §7Use a machine")
        sender.sendMessage("§3/mi setlevel [playerId] [skillId] [level] §7Set a level of player")
        sender.sendMessage("§3/mi update [playerId] §7Update player's skill cache by DB.")
        sender.sendMessage("§3/mi get [machineId] §7Get a machine")
        sender.sendMessage("§3/mi createrecipe [recipeId] [chanceId] [machineId] [sealed(1(true)/0(false))] §7Create a new recipe.")
        sender.sendMessage("§3/mi createmachine [machineId] [machine name] [image] §7Create a new machine.")
        sender.sendMessage("§3/mi createchance [chanceId] [minlevel] [data] §7Create new chance data.")
        sender.sendMessage("§3/mi deleterecipe [recipeId] §7Delete a recipe.")
        sender.sendMessage("§3/mi deletemachine [machineId] §7Delete a machine.")
        sender.sendMessage("§3/mi deletechance [chanceId] §7Delete a chance.")
        sender.sendMessage("§bVer 1.0")
        sender.sendMessage("§a***************************")
    }

    fun showList(sender: CommandSender) {
        sender.sendMessage(prefix + "§bChanceSets: ")
        sender.sendMessage(prefix + "§7" + chanceSets.keys)

        sender.sendMessage(prefix + "§bSkills: ")
        val skillStringList = mutableListOf<String>()
        skills.onEach { skillStringList.add(it.name) }
        sender.sendMessage(prefix + "§7" + skillStringList)

        sender.sendMessage(prefix + "§bRecipes: ")
        sender.sendMessage(prefix + "§7" + recipies.keys)

        sender.sendMessage(prefix + "§bMachines: ")
        sender.sendMessage(prefix + "§7" + machines.keys)
    }

    fun warnWrongCommand(sender: CommandSender) {
        sender.sendMessage(prefix + "Wrong Command. /mi help")
    }

    class SetRecipe(){

        var input = ""
        var machine = ""
        var chance = ""
        var sealed = 0

    }

}

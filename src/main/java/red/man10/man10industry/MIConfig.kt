package red.man10.man10industry

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import red.man10.MIPlugin
import red.man10.man10industry.models.Machine
import red.man10.man10industry.models.Recipe
import red.man10.man10industry.models.Skill
import red.man10.man10industry.models.SkillGenre
import red.man10.man10industry.models.*
import java.io.File

class MIConfig(val pl: MIPlugin) {

    fun loadAll(cs: CommandSender, isFirst: Boolean) { //Load Config & Player Data
        Bukkit.getScheduler().runTaskAsynchronously(pl) {

            cs.sendMessage(pl.prefix + "§bLoading all configurations...")
            loadChanceSets(cs)
            loadSkills(cs)
            loadRecipes(cs)
            loadMachines(cs)

            if (isFirst) {
                MappRenderer.setup(pl)
            }

            //      Mapp setup
            pl.machine.createAllMachineMapp()

            cs.sendMessage(pl.prefix + "§bConfigurations Loaded!")
        }
    }

    fun setInput(encodedItems: String, recipeKey: String) {
        MySQLManager(pl,"MIRecipe").execute("UPDATE recipes SET input=" +
                "'$encodedItems' WHERE recipe_id='$recipeKey'")

    }

    fun setOutput(encodedItems: String, recipeKey: String) {
        MySQLManager(pl,"MIRecipe").execute("UPDATE recipes SET output=" +
                "'$encodedItems' WHERE recipe_id='$recipeKey'")

    }

    fun createMachine(id :String,name:String,image:String){
        val file = loadFile("machines",Bukkit.getConsoleSender())
        val ymlFile = YamlConfiguration.loadConfiguration(file)
        ymlFile.createSection(id)
        ymlFile.set("$id.name",name)
        ymlFile.set("$id.image_name",image)
        ymlFile.set("$id.recipes", mutableListOf<String>())
        ymlFile.save(file)
        Bukkit.getLogger().info("created new machine")
    }

    fun createRecipe(id:String,chance:String){
        MySQLManager(pl,"MIrecipe").execute("INSERT INTO recipes (recipe_id,chance_set) VALUES('$id','$chance');")
        Bukkit.getLogger().info("created new recipe")
    }

    fun createChance(id:String,minLevel:String,map:String){
        val file = loadFile("chance_sets",Bukkit.getConsoleSender())
        val ymlFile = YamlConfiguration.loadConfiguration(file)

        ymlFile.createSection(id)
        ymlFile.set("$id.req",minLevel.toInt())
        val maps = map.split(",")
        for (m in maps){
            val mapData = m.split(":")
            ymlFile.set("$id.map.${mapData[0]}",mapData[1].toDouble())

        }
        ymlFile.save(file)
        Bukkit.getLogger().info("created new chance data")
    }

    fun setRecipe(machineId :String, recipeId:String){
        val file = loadFile("machines",Bukkit.getConsoleSender())
        val ymlFile = YamlConfiguration.loadConfiguration(file)
        val recipe = ymlFile.getStringList("$machineId.recipes").toMutableList()

        if (recipe.indexOf(recipeId) >=0){
            Bukkit.getLogger().info("$recipeId is already exist.")
            return
        }
        recipe.add(recipeId)
        ymlFile.set("$machineId.recipes",recipe)
        ymlFile.save(file)
        Bukkit.getLogger().info("set recipe")
    }

    fun removeRecipe(machineId: String, recipeId: String){
        val file = loadFile("machines",Bukkit.getConsoleSender())
        val ymlFile = YamlConfiguration.loadConfiguration(file)
        val recipe = ymlFile.getStringList("$machineId.recipes")

        recipe.remove(recipeId)

        ymlFile.set("$machineId.recipes",recipe)
        ymlFile.save(file)
        Bukkit.getLogger().info("set recipe")

    }

    fun deleteMachine(machineId: String){
        val file = loadFile("machines",Bukkit.getConsoleSender())
        val ymlFile = YamlConfiguration.loadConfiguration(file)

        ymlFile.set(machineId,null)
        ymlFile.save(file)
        Bukkit.getLogger().info("delete machine")
    }
    fun deleteRecipe(recipeId: String){
        MySQLManager(pl,"Mirecipe").execute("DELETE FROM recipes WHERE recipe_id='$recipeId';")
        Bukkit.getLogger().info("delete recipe")
    }
    fun deleteChance(chanceId:String){
        val file = loadFile("chance_sets",Bukkit.getConsoleSender())
        val ymlFile = YamlConfiguration.loadConfiguration(file)

        ymlFile.set(chanceId,null)
        ymlFile.save(file)
        Bukkit.getLogger().info("delete chance")

    }
    fun showRecipes(id:String):MutableList<String>{
        val file = loadFile("machines",Bukkit.getConsoleSender())
        val ymlFile = YamlConfiguration.loadConfiguration(file)

        return ymlFile.getStringList("$id.recipes")
    }

    private fun loadChanceSets(cs: CommandSender) {
        val file = loadFile("chance_sets", cs)

        pl.chanceSets.clear()
        val ymlFile = YamlConfiguration.loadConfiguration(file)
        val chanceSetKeys = ymlFile.getKeys(false)
        cs.sendMessage(pl.prefix + "§eChance Sets:")
        for (chanceSetKey in chanceSetKeys) {
            val isCorrect = (
                    ymlFile.getKeys(true).contains("$chanceSetKey.req") &&
                    ymlFile.getKeys(true).contains("$chanceSetKey.map")
                    )
            if (isCorrect) {

                val map = HashMap<Int, Double>()

                for (key in ymlFile.getConfigurationSection("${chanceSetKey}.map").getKeys(false)) {
                    map[key.toInt()] = ymlFile.getDouble("${chanceSetKey}.map.${key}")
                }

                val newChanceSet = ChanceSet(
                        ymlFile.getInt("$chanceSetKey.req"),
                        map
                )
                pl.chanceSets[chanceSetKey] = newChanceSet
                cs.sendMessage(pl.prefix + "§a" + chanceSetKey + " ○")
            } else {
                cs.sendMessage(pl.prefix + "§c" + chanceSetKey + " ×")
            }
        }
//        print(pl.chanceSets)
    }

    private fun loadSkills(cs: CommandSender) {
//        for (uuid in pl.currentPlayerData.keys) {
//            pl.skill.saveAllDataFromPlayer(uuid, mysql)
//        }

        val file = loadFile("skills", cs)
        val ymlFile = YamlConfiguration.loadConfiguration(file)

        pl.skills.clear()

        cs.sendMessage(pl.prefix + "§eSkills:")
        var newSkills = mutableListOf<Skill>()
        var skillGenre = SkillGenre.Craft
        val skillAmount = ymlFile.getKeys(false).count()
        for (i in 1..skillAmount) {
            val skillName = ymlFile.getString(i.toString())
            if (skillName != null) {
                val newSkill = Skill(skillName, skillGenre)
                newSkills.add(newSkill)
                cs.sendMessage(pl.prefix + "§a" + i + " ○")
            } else {
                cs.sendMessage(pl.prefix + "§a" + i + " ×")
            }
            skillGenre = when {
                i <= 4 -> SkillGenre.Craft
                i <= 8 -> SkillGenre.Magic
                i <= 12 -> SkillGenre.Study
                else -> SkillGenre.Special
            }
        }
        pl.skills = newSkills

//        for (player in Bukkit.getOnlinePlayers()) {
//            pl.skill.loadAllDataFromPlayer(player.uniqueId, mysql)
//        }
    }

//    private fun loadRecipes(cs: CommandSender) {
//        val file = loadFile("recipes", cs)
//
//        pl.recipies.clear()
//        val ymlFile = YamlConfiguration.loadConfiguration(file)
//        val recipeKeys = ymlFile.getKeys(false)
//        cs.sendMessage(pl.prefix + "§eRecipes:")
//        for (recipeKey in recipeKeys) {
//            //try {
////            print(ymlFile.getKeys(true))
//            val isCorrect: Boolean = (
//                    //ymlFile.getKeys(true).contains(recipeKey + ".inputs") &&
//                    //ymlFile.getKeys(true).contains(recipeKey + ".outputs") &&
//                    ymlFile.getKeys(true).contains(recipeKey + ".chance_sets")
//                    )
//            if (isCorrect) {
//                var newInputs = mutableListOf<ItemStack>()
//                if (ymlFile.getString(recipeKey + ".inputs") != null) {
//                    newInputs = pl.util.itemStackArrayFromBase64(ymlFile.getString(recipeKey + ".inputs"))
//                }
//                var newOutputs = mutableListOf<ItemStack>()
//                if (ymlFile.getString(recipeKey + ".outputs") != null) {
//                    newOutputs = pl.util.itemStackArrayFromBase64(ymlFile.getString(recipeKey + ".outputs"))
//                }
//                val newChanceSets = HashMap<Skill, ChanceSet>()
//
//                for (key in ymlFile.getConfigurationSection("${recipeKey}.chance_sets").getKeys(false)) {
//                    newChanceSets.put( pl.skills[key.toInt()], pl.chanceSets[ymlFile.getString("${recipeKey}.chance_sets.${key}")]!! )
//                }
//                val newRecipe = Recipe(
//                        newInputs,
//                        newOutputs,
//                        newChanceSets
//                )
//                pl.recipies.put(recipeKey, newRecipe)
//                cs.sendMessage(pl.prefix + "§a" + recipeKey + " ○")
//            } else {
//                cs.sendMessage(pl.prefix + "§c" + recipeKey + " ×")
//            }
//        }
//    }

    private fun loadRecipes(cs:CommandSender){
        val mysql = MySQLManager(pl,"MIrecipe")

        val rs = mysql.query("SELECT * FROM recipes")

        while (rs.next()){
            try{
                val input = pl.util.itemStackArrayFromBase64(rs.getString("input"))
                val output = pl.util.itemStackArrayFromBase64(rs.getString("output"))
                val chance = HashMap<Skill,ChanceSet>()
                val data = rs.getString("chance_set").split(",")
                for (d in data){
                    val split = d.split(":")
                    chance[pl.skills[split[0].toInt()]] = pl.chanceSets[split[1]]!!
                }
                pl.recipies[rs.getString("recipe_id")] = Recipe(input,output,chance)
                cs.sendMessage(pl.prefix + "§a" + rs.getString("recipe_id")+ " ○")
            }catch (e:Exception){
                cs.sendMessage(e.message)
            }
        }
        rs.close()
        mysql.close()
    }

    private fun loadMachines(cs: CommandSender) {
        pl.machines.clear()

        val file = loadFile("machines", cs)
        val ymlFile = YamlConfiguration.loadConfiguration(file)

        val machineKeys = ymlFile.getKeys(false)

        cs.sendMessage(pl.prefix + "§eMachines:")

        for (machineKey in machineKeys) {
            val isCorrect: Boolean = (
                    ymlFile.getKeys(true).contains(machineKey + ".name") &&
                    ymlFile.getKeys(true).contains(machineKey + ".image_name") &&
                    ymlFile.getKeys(true).contains(machineKey + ".recipes")
                    )
            if (isCorrect) {
                val recipeKeys = ymlFile.getList(machineKey  + ".recipes")
//                print(recipeKeys)
                val newRecipes = mutableListOf<Recipe>()
                for (recipeKey in recipeKeys) {
                    (pl.recipies[recipeKey.toString()])?.let {
                        print(it)
                        newRecipes.add(it)
                    }
                }

                val newMachine = Machine(
                        ymlFile.getString(machineKey + ".name"),
                        ymlFile.getString(machineKey + ".image_name"),
                        newRecipes
                )
                pl.machines.put(machineKey, newMachine)
                cs.sendMessage(pl.prefix + "§a" + machineKey + " ○")
            } else {
                cs.sendMessage(pl.prefix + "§c" + machineKey + " ×")
            }
        }
    }

    private fun loadFile(name: String, cs: CommandSender): File {
        val file = File(pl.dataFolder.path + "/" + name + ".yml")
        if (!file.exists()) {
            file.createNewFile()
            cs.sendMessage(pl.prefix + "Generated " + name + ".yml")
        }
        return file
    }

//    fun loadRecipes(cs: CommandSender) {
//        val dir = File(pl.dataFolder.path + "/recipes")
//        if (!dir.exists()) {
//            dir.mkdir()
//        }
//
//        val recipeFiles= dir.listFiles()
//        for (recipeFile in recipeFiles) {
//
//            val ymlFile = YamlConfiguration.loadConfiguration(recipeFile)
//            val isCorrect = (ymlFile.getString("chance") != null &&
//                            ymlFile.getKeys(false).contains("chance"))
//            val recipeKey = recipeFile.nameWithoutExtension
//
//            if (isCorrect) {
//                var newRecipe = Recipe()
//                var patternKeys = ymlFile.getKeys(false)
//                for (patternKey in patternKeys) {
//                    //newRecipe.patterns.put()
//                }
//                cs.sendMessage(pl.prefix + "レシピ" + recipeKey + "をロードしました")
//            } else {
//                cs.sendMessage(pl.prefix + "ERROR: レシピ" + recipeKey + "はロードできません")
//            }
//
//        }
//    }
//
//    fun loadMachines(cs: CommandSender) {
//        val dir = File(pl.dataFolder.path + "/machine")
//        if (!dir.exists()) {
//            dir.mkdir()
//        }
//
//        val machineFiles= dir.listFiles()
//        for (machineFile in machineFiles) {
//            val ymlFile = YamlConfiguration.loadConfiguration(machineFile)
//            val isCorrect = (
//                    ymlFile.getString("name") != null &&
//                            ymlFile.getStringList("recipes") != null
//                    )
//            val machineKey = machineFile.nameWithoutExtension
//            if (isCorrect) {
//                var newMachine = Machine()
//                newMachine.name = ymlFile.getString("name")
//                cs.sendMessage(pl.prefix + "マシン" + machineKey + "をロードしました")
//            } else {
//                cs.sendMessage(pl.prefix + "ERROR: マシン" + machineKey + "は読み込めません")
//            }
//        }
//    }
//
//    fun loadSkills() {
//        var dir = File(pl.dataFolder.toString() + "/skill")
//    }
}
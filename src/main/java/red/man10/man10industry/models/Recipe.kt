package red.man10.man10industry.models

import org.bukkit.inventory.ItemStack

data class Recipe(
        var inputs: MutableList<ItemStack>,
        var outputs: MutableList<ItemStack>,
        var chanceSets: HashMap<Skill, ChanceSet>
)
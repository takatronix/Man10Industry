package red.man10.man10industry.models

data class Machine (
        var name: String,
        var imageName: String,
        var recipes: MutableList<Recipe>
)
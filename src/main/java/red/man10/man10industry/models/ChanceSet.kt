package red.man10.man10industry.models

data class ChanceSet(
        var req: Int, //必須レベル
        var chances: HashMap<Int, Double> //レベル, 確率
)
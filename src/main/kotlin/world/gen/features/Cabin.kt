package world.gen.features

import kotlinx.serialization.Serializable
import util.Dice

@Serializable
class Cabin : ChunkFeature(
    0, Stage.BUILD
) {

    override fun doDig() {
        val width = Dice.range(6, 10)
        val height = Dice.range(6, 10)
        val x = Dice.range(3, 63 - width)
        val y = Dice.range(3, 63 - height)
        buildHut(x, y, width, height)
    }

}

package world.gen.features

import kotlinx.serialization.Serializable
import util.Dice
import world.gen.decors.Hut

@Serializable
class Cabin : ChunkFeature(
    0, Stage.BUILD
) {

    override fun doDig() {
        val width = Dice.range(6, 10)
        val height = Dice.range(6, 10)
        val x = Dice.range(3, 63 - width)
        val y = Dice.range(3, 63 - height)
        val fertility = if (Dice.chance(0.3f)) 0f else Dice.float(0.2f, 1f)
        buildHut(x, y, width, height, fertility) { rooms ->
            Hut().furnish(rooms[0], carto)
        }
    }

}

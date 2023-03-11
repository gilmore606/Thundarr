package world.gen.features

import kotlinx.serialization.Serializable
import util.Dice
import world.ChunkScratch
import world.gen.decors.Hut

@Serializable
class Cabin : Feature() {
    override fun order() = 0
    override fun stage() = Stage.BUILD

    companion object {
        fun canBuildOn(meta: ChunkScratch) = !meta.hasFeature(Village::class)
    }

    override fun trailDestinationChance() = 1f

    override fun doDig() {
        val width = Dice.range(6, 10)
        val height = Dice.range(6, 10)
        val x = Dice.range(3, 63 - width)
        val y = Dice.range(3, 63 - height)
        val fertility = if (Dice.chance(0.3f)) 0f else Dice.float(0.2f, 1f)
        buildHut(x, y, width, height, fertility) { rooms ->
            Hut().furnish(rooms[0], carto)
        }
        carto.addTrailBlock(x0 + x, y0 + y, x0 + x + width -1, y0 + y + height - 1)
    }

}

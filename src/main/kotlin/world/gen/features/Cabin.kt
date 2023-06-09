package world.gen.features

import actors.NPC
import kotlinx.serialization.Serializable
import util.Dice
import util.Rect
import util.XY
import world.Chunk
import world.ChunkScratch
import world.gen.AnimalSpawn
import world.gen.biomes.*
import world.gen.decors.Hut
import world.gen.habitats.*

@Serializable
class Cabin : Feature() {
    override fun order() = 0
    override fun stage() = Stage.BUILD

    companion object {
        fun canBuildOn(meta: ChunkScratch) = !meta.hasFeature(Village::class)
    }

    override fun trailDestinationChance() = 1f

    var bounds = Rect(0, 0, 0, 0)

    override fun doDig() {
        val width = Dice.range(6, 10)
        val height = Dice.range(6, 10)
        val x = Dice.range(3, 63 - width)
        val y = Dice.range(3, 63 - height)
        val fertility = if (Dice.chance(0.3f)) 0f else Dice.float(0.2f, 1f)
        buildHut(x, y, width, height, fertility) { rooms ->
            Hut().furnish(rooms[0], carto)
        }

        bounds = Rect(x0 + x, y0 + y, x0 + x+width-1, y0 + y+height-1)
        carto.addTrailBlock(bounds.x0, bounds.y0, bounds.x1, bounds.y1)
    }

    override fun animalSpawns() = listOf(
        AnimalSpawn(
            { NPC.Tag.NPC_HERMIT },
            setOf(Mountain, Hill, ForestHill, Desert, Forest, Plain, Swamp),
            setOf(TemperateA, TemperateB, TropicalA, TropicalB, AlpineA, AlpineB),
            0f, 1000f, 1, 1, 1f
        )
    )

    override fun animalSpawnPoint(chunk: Chunk, animalType: NPC.Tag): XY? =
        findSpawnPoint(chunk, animalType, bounds)
}

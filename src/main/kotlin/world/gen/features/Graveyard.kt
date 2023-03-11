package world.gen.features

import kotlinx.serialization.Serializable
import util.*
import world.ChunkScratch
import world.gen.biomes.*
import world.gen.decors.Church
import world.gen.decors.Decor
import world.gen.decors.Graveyard
import world.terrains.Terrain

@Serializable
class Graveyard(
    private val isAbandoned: Boolean = false
) : Feature() {
    override fun order() = 3
    override fun stage() = Stage.BUILD

    companion object {
        fun canBuildOn(meta: ChunkScratch) = meta.biome in listOf(Plain, Scrub, Hill, ForestHill, Mountain, Forest, Swamp, Desert, Suburb)
                && !meta.hasFeature(Rivers::class) && !meta.hasFeature(Coastlines::class) && !meta.hasFeature(Highways::class)
    }

    private val graveyardChance = 0.8f
    private val shrineChance = 0.5f
    override fun trailDestinationChance() = 0.5f

    override fun doDig() {
        val width = Dice.range(16, 30)
        val height = Dice.range(16, 30)
        val x = x0 + Dice.range(5, 52 - width)
        val y = y0 + Dice.range(5, 52 - height)
        carveBlock(x, y, x + width - 1, y + height - 1, Terrain.Type.TEMP1)
        repeat(3) { fuzzTerrain(Terrain.Type.TEMP1, 0.5f) }

        val graveWidth = (width * Dice.float(0.4f, 0.8f)).toInt()
        val graveHeight =  (height * Dice.float(0.4f, 0.8f)).toInt()
        val gravex = x + (width - graveWidth) / 2
        val gravey = y + (height - graveHeight) / 2
        var gravePlaced = false
        if (Dice.chance(graveyardChance)) {
            Graveyard(Dice.float(0.2f, 1f), Dice.float(0.1f, 0.6f)).furnish(
                Decor.Room(Rect(gravex, gravey, gravex + graveWidth - 1, gravey + graveHeight - 1)),
            carto, isAbandoned)
            gravePlaced = true
            carto.addTrailBlock(gravex, gravey, gravex + graveWidth - 1, gravey + graveHeight - 1)
        }

        if (Dice.chance(shrineChance) || !gravePlaced) {
            val shrineWidth = Dice.range(6, 10)
            val shrineHeight = Dice.range(7, 11)
            val shrineRect = when (CARDINALS.random()) {
                NORTH -> {
                    val shrinex = gravex + Dice.range(-2, 4)
                    val shriney = gravey - shrineHeight - 3
                    Rect(shrinex, shriney, shrinex + shrineWidth - 1, shriney + shrineHeight - 1)
                }
                SOUTH -> {
                    val shrinex = gravex + Dice.range(-2, 4)
                    val shriney = gravey + graveHeight + 3
                    Rect(shrinex, shriney, shrinex + shrineWidth - 1, shriney + shrineHeight - 1)
                }
                WEST -> {
                    val shrinex = gravex - shrineWidth - 3
                    val shriney = gravey + Dice.range(-2, 4)
                    Rect(shrinex, shriney, shrinex + shrineWidth - 1, shriney + shrineHeight - 1)
                }
                else -> {
                    val shrinex = gravex + graveWidth + 3
                    val shriney = gravey + Dice.range(-2, 4)
                    Rect(shrinex, shriney, shrinex + shrineWidth - 1, shriney + shrineHeight - 1)
                }
            }
            buildHut(
                shrineRect.x0 - x0, shrineRect.y0 - y0, shrineWidth, shrineHeight, 0.5f,
                isAbandoned = isAbandoned, hasWindows = true, splittable = false,
                forceFloor = Terrain.Type.TERRAIN_STONEFLOOR
            ) { rooms ->
                Church().furnish(rooms[0], carto, isAbandoned)
            }
        }

        swapTerrain(Terrain.Type.TEMP1, meta.biome.baseTerrain)
    }
}

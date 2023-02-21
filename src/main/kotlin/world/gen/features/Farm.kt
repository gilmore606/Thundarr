package world.gen.features

import kotlinx.serialization.Serializable
import util.*
import world.ChunkScratch
import world.gen.biomes.Plain
import world.gen.biomes.Scrub
import world.gen.biomes.Swamp
import world.gen.decors.Barn
import world.gen.decors.Decor
import world.gen.decors.Garden
import world.terrains.Terrain

@Serializable
class Farm(
    private val isAbandoned: Boolean = false
) : ChunkFeature(
    3, Stage.BUILD
) {

    companion object {
        fun canBuildOn(meta: ChunkScratch) = meta.biome in listOf(Plain, Scrub, Swamp)
                && !meta.hasFeature(Rivers::class) && !meta.hasFeature(Coastlines::class) && !meta.hasFeature(Highways::class)
    }

    private val fieldChance = 0.7f
    private val barnChance = 0.7f

    override fun cellTitle() = "farm"

    override fun doDig() {
        val width = Dice.range(35, 50)
        val height = Dice.range(35, 50)
        val x = x0 + Dice.range(5, 52 - width)
        val y = y0 + Dice.range(5, 52 - height)
        carveBlock(x, y, x + width - 1, y + height - 1, Terrain.Type.TEMP1)
        repeat(3) { fuzzTerrain(Terrain.Type.TEMP1, 0.5f) }

        val field = Rect(x + Dice.range(12, 18), y + Dice.range(12, 18),
            x + width - Dice.range(1, 10), y + height - Dice.range(1, 5))
        var fieldPlaced = false
        if (Dice.chance(fieldChance)) {
            Garden(0.5f, meta.biome, meta.habitat, Terrain.Type.TERRAIN_DIRT)
                .furnish(Decor.Room(field), carto, isAbandoned)
            fieldPlaced = true
        }

        if (Dice.chance(barnChance) || !fieldPlaced) {
            val barnWidth = Dice.range(7, 11)
            val barnHeight = Dice.range(7, 11)
            val barnRect = when (CARDINALS.random()) {
                NORTH -> {
                    val barnx0 = field.x0 + Dice.range(-3, 18)
                    val barny0 = field.y0 - barnHeight - Dice.range(2, 4)
                    Rect(barnx0, barny0, barnx0 + barnWidth - 1, barny0 + barnHeight - 1)
                }
                SOUTH -> {
                    val barnx0 = field.x0 + Dice.range(-3, 18)
                    val barny0 = field.y1 + Dice.range(2, 4)
                    Rect(barnx0, barny0, barnx0 + barnWidth - 1, barny0 + barnHeight - 1)
                }
                WEST -> {
                    val barnx0 = field.x0 - barnWidth - Dice.range(2, 4)
                    val barny0 = field.y0 + Dice.range(-3, 18)
                    Rect(barnx0, barny0, barnx0 + barnWidth - 1, barny0 + barnHeight - 1)
                }
                else -> {
                    val barnx0 = field.x1 + Dice.range(2, 4)
                    val barny0 = field.y0 + Dice.range(-3, 18)
                    Rect(barnx0, barny0, barnx0 + barnWidth - 1, barny0 + barnHeight - 1)
                }
            }
            buildHut(
                barnRect.x0 - x0, barnRect.y0 - y0, barnWidth, barnHeight, 0.5f,
                isAbandoned = isAbandoned, hasWindows = false, forceFloor = Terrain.Type.TERRAIN_DIRT
            ) { rooms ->
                rooms.forEach { room ->
                    Barn().furnish(room, carto, isAbandoned)
                }
            }
        }

        swapTerrain(Terrain.Type.TEMP1, Terrain.Type.TERRAIN_GRASS)
    }

}

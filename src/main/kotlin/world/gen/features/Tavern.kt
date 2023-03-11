package world.gen.features

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.HighwaySign
import util.Dice
import util.XY
import world.ChunkScratch
import world.gen.biomes.Glacier
import world.gen.biomes.Ocean
import world.gen.decors.Tavern
import world.terrains.Terrain

@Serializable
class Tavern(
    private val name: String,
    private val villageDirection: XY
) : Feature() {
    override fun order() = 4
    override fun stage() = Stage.BUILD

    companion object {
        fun canBuildOn(meta: ChunkScratch) = meta.biome !in listOf(Ocean, Glacier)
    }

    override fun trailDestinationChance() = 1f
    override fun mapIcon() = Glyph.MAP_BUILDING
    override fun mapPOITitle() = name
    override fun mapPOIDescription() = "A roadside inn."

    override fun doDig() {
        val width = Dice.range(8, 12)
        val height = Dice.range(8, 12)
        val x = x0 + Dice.range(20, 40 - width) - villageDirection.x * 14
        val y = y0 + Dice.range(20, 40 - height) - villageDirection.y * 14
        carveBlock(x-1, y-1, x + width, y + height, Terrain.Type.TEMP1)
        repeat(3) { fuzzTerrain(Terrain.Type.TEMP1, 0.4f) }
        swapTerrain(Terrain.Type.TEMP1, meta.biome.bareTerrain(x, y))

        buildHut(x-x0, y-y0, width, height, 0.3f, hasWindows = true, splittable = false,
            buildByOutsideDoor = { x, y ->
                spawnThing(x, y, HighwaySign(name))
            }
        ) { rooms ->
            Tavern().furnish(rooms[0], carto)
        }
    }

}

package world.gen.features

import actors.actors.NPC
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.Dice
import util.Rect
import util.XY
import world.Chunk
import world.ChunkScratch
import world.gen.biomes.*
import world.gen.habitats.*
import world.gen.spawnsets.AnimalSet
import world.level.CHUNK_SIZE
import world.quests.FetchQuest
import world.terrains.Terrain

@Serializable
class Lake(
    val name: String,
) : Feature() {
    override fun order() = 3
    override fun stage() = Stage.TERRAIN
    override fun name() = name
    override fun cellTitle() = name
    override fun mapIcon(onBiome: Biome?): Glyph? = Glyph.MAP_LAKE
    override fun mapPOITitle() = name
    override fun mapPOIDescription() = ""
    override fun trailDestinationChance() = 1f
    override fun canBeQuestDestination() = Dice.chance(0.5f)
    override fun createQuest() = FetchQuest()
    override fun loreKnowabilityRadius() = 500
    override fun xpValue() = 8

    companion object {
        fun canBuildOn(meta: ChunkScratch) = !meta.hasFeature(Village::class)
                && meta.biome !in listOf(Ocean, Glacier, Desert)

        val animalSpawns = AnimalSet().apply {
            add(1f, NPC.Tag.GATOR)
        }
    }


    var bounds = Rect(0, 0, 0, 0)

    override fun doDig() {
        if (Dice.chance(0.15f)) {
            // Big lake!
            val width = Dice.range(40,56)
            val height = Dice.range(40,56)
            val x = x0 + Dice.range(3, 6)
            val y = y0 + Dice.range(3, 6)
            carto.addTrailBlock(x, y, x+width-1, y+height-1)
            digLakeBlobAt(x, y, width, height)
            if (Dice.chance(0.5f)) {
                // Big lake island
                printGrid(growBlob(Dice.range(4,12), Dice.range(4,12)),
                    x0 + Dice.range(10, 45), y0 + Dice.range(10, 45), meta.biome.baseTerrain)
            }
        } else {
            val width = Dice.range(12, 31)
            val height = Dice.range(12, 31)
            val x = x0 + Dice.range(width, CHUNK_SIZE - width) - width / 2
            val y = y0 + Dice.range(height, CHUNK_SIZE - height) - height / 2
            bounds = Rect(x, y, x + width - 1, y + height - 1)
            carto.addTrailBlock(x, y, x + width - 1, y + height - 1)
            digLakeBlobAt(x, y, width, height)
            if (Dice.chance(0.4f)) {
                // Double lake
                val ox = x + Dice.range(-(width / 2), width / 2)
                val oy = y + Dice.range(-(height / 2), height / 2)
                val owidth = (width * Dice.float(0.3f, 0.7f)).toInt()
                val oheight = (height * Dice.float(0.3f, 0.7f)).toInt()
                digLakeBlobAt(ox, oy, owidth, oheight)
            }
            if (width > 17 && height > 17 && Dice.chance(0.5f)) {
                // Island
                printGrid(
                    growBlob((width * Dice.float(0.1f, 0.7f)).toInt(), (height * Dice.float(0.1f, 0.7f)).toInt()),
                    x + Dice.range(3, 15), y + Dice.range(3, 15), meta.biome.baseTerrain
                )
            }
        }
        fringeTerrain(Terrain.Type.TEMP1, Terrain.Type.TEMP2, 1f)
        repeat (3) { varianceFuzzTerrain(Terrain.Type.TEMP2, Terrain.Type.TEMP1) }
        swapTerrain(Terrain.Type.TEMP1, Terrain.Type.GENERIC_WATER)
        forEachCell { x,y ->
            if (getTerrain(x,y) == Terrain.Type.TEMP2) {
                setTerrain(x,y,meta.biome.riverBankTerrain(x,y))
            }
        }
    }

    private fun digLakeBlobAt(x: Int, y: Int, width: Int, height: Int) {
        printGrid(growBlob(width, height), x, y, Terrain.Type.TEMP1)
    }

    override fun animalSet(habitat: Habitat) = animalSpawns
    override fun animalSpawnPoint(chunk: Chunk, animalType: NPC.Tag): XY? =
        findSpawnPointForNPCType(chunk, animalType, bounds)
}

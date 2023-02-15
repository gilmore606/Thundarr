package world.gen.features

import kotlinx.serialization.Serializable
import util.Dice
import world.level.CHUNK_SIZE
import world.terrains.Terrain

@Serializable
class Lake : ChunkFeature(
    1, Stage.TERRAIN
) {
    override fun doDig() {
        if (Dice.chance(0.15f)) {
            // Big lake!
            val width = Dice.range(40,56)
            val height = Dice.range(40,56)
            digLakeBlobAt(Dice.range(3, 6), Dice.range(3,6), width, height)
            if (Dice.chance(0.5f)) {
                // Big lake island
                printGrid(growBlob(Dice.range(4,12), Dice.range(4,12)), Dice.range(10, 45), Dice.range(10, 45), meta.biome.baseTerrain)
            }
            return
        }
        val width = Dice.range(12,31)
        val height = Dice.range(12,31)
        val x = x0 + Dice.range(width, CHUNK_SIZE - width) - width / 2
        val y = y0 + Dice.range(height, CHUNK_SIZE - height) - height / 2
        digLakeBlobAt(x, y, width, height)
        if (Dice.chance(0.4f)) {
            // Double lake
            val ox = x + Dice.range(-(width / 2), width / 2)
            val oy = y + Dice.range(-(height / 2), height / 2)
            val owidth = (width * Dice.float(0.3f,0.7f)).toInt()
            val oheight = (height * Dice.float(0.3f, 0.7f)).toInt()
            digLakeBlobAt(ox, oy, owidth, oheight)
        }
        if (width > 17 && height > 17 && Dice.chance(0.5f)) {
            // Island
            printGrid(growBlob((width * Dice.float(0.1f, 0.7f)).toInt(), (height * Dice.float(0.1f, 0.7f)).toInt()),
                x + Dice.range(3,15), y + Dice.range(3, 15), meta.biome.baseTerrain)
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
}

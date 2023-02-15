package world.gen.features

import kotlinx.serialization.Serializable
import things.Bonepile
import things.ModernDoor
import things.Trunk
import util.*
import world.Chunk
import world.gen.NoisePatches
import world.gen.cartos.WorldCarto
import world.level.CHUNK_SIZE
import world.terrains.Terrain

@Serializable
class RuinedBuildings(
    val buildingCount: Int,
) : ChunkFeature(
    1, Stage.BUILD
) {
    private val ruinTreasureChance = 0.4f
    private val ruinIntactChance = 0.25f

    override fun doDig() {
        repeat (buildingCount) {
            buildRandomRuin()
        }
    }

    private fun buildRandomRuin() {
        val isIntact = Dice.chance(ruinIntactChance)
        val mid = CHUNK_SIZE /2
        if (meta.roadExits.isNotEmpty()) {
            val offset = Dice.range(3, 8) * Dice.sign()
            val along = Dice.range(2, CHUNK_SIZE /2-2)
            val width = Dice.range(4, 10)
            val height = Dice.range(4, 10)
            when (meta.roadExits.random().edge) {
                NORTH -> buildRuin(mid + offset + width * (if (offset<0) -1 else 0), along, width, height, isIntact)
                SOUTH -> buildRuin(mid + offset + width * (if (offset<0) -1 else 0), CHUNK_SIZE -along, width, height, isIntact)
                WEST -> buildRuin(along, mid + offset + width * (if (offset<0) -1 else 0), width, height, isIntact)
                EAST -> buildRuin(CHUNK_SIZE -along, mid + offset + width * (if (offset<0) -1 else 0), width, height, isIntact)
            }
        } else {
            buildRuin(Dice.range(1, CHUNK_SIZE -10), Dice.range(1, CHUNK_SIZE -10), Dice.range(4, 10), Dice.range(4, 10), isIntact)
        }
    }

    private fun buildRuin(x: Int, y: Int, width: Int, height: Int, isIntact: Boolean = false) {
        for (ix in x until x+width) {
            for (iy in y until y+height) {
                if (ix>=0 && iy>=0 && ix<CHUNK_SIZE && iy<CHUNK_SIZE
                    && carto.flagsMap[ix][iy].contains(WorldCarto.CellFlag.NO_BUILDINGS)) return
            }
        }
        for (ix in x until x + width) {
            for (iy in y until y + height) {
                if (boundsCheck(ix + x0, iy + y0)) {
                    if (ix == x || ix == x + width - 1 || iy == y || iy == y + height - 1) {
                        val terrain = if (isIntact || Dice.chance(0.9f)) Terrain.Type.TERRAIN_BRICKWALL else null
                        setRuinTerrain(ix + x0, iy + y0, 0.34f, terrain)
                    } else {
                        val terrain =
                            if (!isIntact && Dice.chance(NoisePatches.get("ruinWear", ix + x0, iy + y0).toFloat()))
                                null else Terrain.Type.TERRAIN_STONEFLOOR
                        setRuinTerrain(ix + x0, iy + y0, 0.34f, terrain)
                    }
                    if (isIntact) chunk.setRoofed(ix + x0, iy + y0, Chunk.Roofed.INDOOR)
                }
            }
        }
        val doorDir = CARDINALS.random()
        val doorx = if (doorDir == NORTH || doorDir == SOUTH) {
            Dice.range(x+1, x+width-2)
        } else {
            if (doorDir == EAST) x+width-1 else x
        } + x0
        val doory = if (doorDir == EAST || doorDir == WEST) {
            Dice.range(y+1, y+height-2)
        } else {
            if (doorDir == SOUTH) y+height-1 else y
        } + y0
        if (boundsCheck(doorx, doory)) {
            setTerrain(doorx, doory, Terrain.Type.TERRAIN_STONEFLOOR)
            if (isIntact || Dice.chance(0.2f)) {
                if (isIntact) chunk.setRoofed(doorx, doory, Chunk.Roofed.WINDOW)
                spawnThing(doorx, doory, ModernDoor())
            }
        }
        if (Dice.chance(ruinTreasureChance)) {
            var placed = false
            var tries = 0
            while (!placed && tries < 200) {
                val tx = Dice.range(x-2, x+2) + x0
                val ty = Dice.range(y-2, y+2) + y0
                if (boundsCheck(tx,ty) && isWalkableAt(tx,ty)) {
                    val treasure = if (Dice.chance(0.25f)) Trunk() else Bonepile()
                    spawnThing(tx, ty, treasure)
                    placed = true
                }
                tries++
            }
        }
    }

    private fun setRuinTerrain(x: Int, y: Int, wear: Float, terrain: Terrain.Type?) {
        if (terrain == null) return
        if (NoisePatches.get("ruinWear", x, y) < wear) {
            if (boundsCheck(x, y)) {
                setTerrain(x, y, terrain)
            }
        }
    }
}

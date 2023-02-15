package world.gen.features

import audio.Speaker
import kotlinx.serialization.Serializable
import util.*
import world.gen.cartos.WorldCarto
import world.level.CHUNK_SIZE
import world.terrains.Terrain

@Serializable
class Coastlines(
    val exits: MutableList<XY>,
) : ChunkFeature(
    1, Stage.TERRAIN
) {

    override fun doDig() {
        val cornerWater = carto.growOblong(8, 8)
        exits.forEach { edge ->
            if (edge in CARDINALS) {
                for (i in 0 until CHUNK_SIZE) {
                    when (edge) {
                        NORTH -> setTerrain(x0+i,y0, Terrain.Type.GENERIC_WATER)
                        SOUTH -> setTerrain(x0+i,y1, Terrain.Type.GENERIC_WATER)
                        WEST -> setTerrain(x0, y0+i, Terrain.Type.GENERIC_WATER)
                        EAST -> setTerrain(x1, y0+i, Terrain.Type.GENERIC_WATER)
                    }
                }
            } else {
                when (edge) {
                    NORTHWEST -> {
                        printGrid(cornerWater, x0, y0, Terrain.Type.GENERIC_WATER)
                        setTerrain(x0,y0, Terrain.Type.GENERIC_WATER)
                        setTerrain(x0+1,y0+1, Terrain.Type.GENERIC_WATER)
                    }
                    NORTHEAST -> {
                        printGrid(cornerWater, x1-7, y0, Terrain.Type.GENERIC_WATER)
                        setTerrain(x1,y0, Terrain.Type.GENERIC_WATER)
                        setTerrain(x1-1,y0+1, Terrain.Type.GENERIC_WATER)
                    }
                    SOUTHWEST -> {
                        printGrid(cornerWater, x0, y1-7, Terrain.Type.GENERIC_WATER)
                        setTerrain(x0,y1, Terrain.Type.GENERIC_WATER)
                        setTerrain(x0+1,y1-1, Terrain.Type.GENERIC_WATER)
                    }
                    SOUTHEAST -> {
                        printGrid(cornerWater, x1-7, y1-7, Terrain.Type.GENERIC_WATER)
                        setTerrain(x1,y1, Terrain.Type.GENERIC_WATER)
                        setTerrain(x1-1,y1-1, Terrain.Type.GENERIC_WATER)
                    }
                }
            }
        }
        repeat (8) { fuzzTerrain(Terrain.Type.GENERIC_WATER, 0.3f) }
        fringeTerrain(Terrain.Type.GENERIC_WATER, Terrain.Type.TERRAIN_BEACH, 1f)
        repeat ((2 + 3 * meta.variance).toInt()) { fuzzTerrain(
            Terrain.Type.TERRAIN_BEACH, meta.variance,
            Terrain.Type.GENERIC_WATER
        ) }
        forEachCell { x,y ->
            if (getTerrain(x,y) == Terrain.Type.GENERIC_WATER) {
                flagsAt(x,y).add(WorldCarto.CellFlag.OCEAN)
                flagsAt(x,y).add(WorldCarto.CellFlag.NO_PLANTS)
                if (neighborCount(x-x0,y-y0, Terrain.Type.GENERIC_WATER) < 8) chunk.setSound(x,y, Speaker.PointAmbience(
                    Speaker.Ambience.OCEAN))
            } else if (getTerrain(x,y) == Terrain.Type.TERRAIN_BEACH) {
                flagsAt(x,y).add(WorldCarto.CellFlag.BEACH)
                flagsAt(x,y).add(WorldCarto.CellFlag.NO_PLANTS)
            }
        }
    }

}

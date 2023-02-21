package world.gen.features

import kotlinx.serialization.Serializable
import world.ChunkScratch
import world.gen.cartos.WorldCarto.CellFlag.*
import world.terrains.Terrain

@Serializable
class Bridge : ChunkFeature(
    4, Stage.BUILD
) {
    companion object {
        fun canBuildOn(meta: ChunkScratch) = meta.hasFeature(Rivers::class) && meta.hasFeature(Trails::class)
    }

    override fun doDig() {
        forEachCell { x,y ->
            if (flagsAt(x,y).contains(TRAIL) && flagsAt(x,y).contains(RIVER)) {
                setTerrain(x, y, Terrain.Type.TEMP1)
            }
        }
        swapTerrain(Terrain.Type.TEMP1, Terrain.Type.TERRAIN_WOODFLOOR)
    }
}

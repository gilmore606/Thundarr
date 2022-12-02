package world.cartos

import App
import world.Chunk
import world.level.Level
import world.terrains.Terrain

class NuWorldKarto(
    val x0: Int,
    val y0: Int,
    level: Level,
    chunk: Chunk
) : Nukarto(level, chunk, Terrain.Type.TERRAIN_DIRT, Terrain.Type.TERRAIN_CAVEWALL) {

    override suspend fun doCarveLevel() {
        App.save.getWorldMeta(x0, y0)?.also { meta ->

        }
    }

}

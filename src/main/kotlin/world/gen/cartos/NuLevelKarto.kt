package world.gen.cartos

import world.Chunk
import world.level.Level
import world.terrains.Terrain

class NuLevelKarto(
    level: Level,
    chunk: Chunk,
    floorType: Terrain.Type,
    wallType: Terrain.Type
) : Nukarto(level, chunk, floorType, wallType) {

    override suspend fun doCarveLevel() {
        fillBox(0, 0, chunk.width - 1, chunk.height - 1, wallType)
    }

}

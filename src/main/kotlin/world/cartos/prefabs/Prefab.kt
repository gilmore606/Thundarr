package world.cartos.prefabs

import kotlinx.serialization.Serializable
import world.terrains.Terrain

class Prefab(
    tiledFile: TiledFile
) {

    val height = tiledFile.height
    val width = tiledFile.width
    val terrain = Array(width) { Array<Terrain.Type?>(height) { null } }

    init {
        for (x in 0 until width) {
            for (y in 0 until height) {
                val code = tiledFile.layers[0].data[x + y * width]
                terrain[x][y] = Terrain.getTiled(code)
            }
        }
    }

}

@Serializable
data class TiledLayer(
    val data: List<Int>,
    val height: Int,
    val width: Int
)

@Serializable
data class TiledFile(
    val height: Int,
    val width: Int,
    val layers: List<TiledLayer>
)

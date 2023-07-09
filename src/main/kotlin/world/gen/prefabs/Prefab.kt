package world.gen.prefabs

import kotlinx.serialization.Serializable
import util.forXY
import world.terrains.Terrain

class Prefab(
    tiledFile: TiledFile
) {

    val height = tiledFile.height
    val width = tiledFile.width
    val terrain = Array(width) { Array<Terrain.Type?>(height) { null } }

    init {
        forXY(0,0, width-1,height-1) { x,y ->
            val code = tiledFile.layers[0].data[x + y * width]
            terrain[x][y] = Terrain.getTiled(code)
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

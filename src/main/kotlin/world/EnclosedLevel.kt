package world

import kotlinx.serialization.Serializable
import util.StepMap
import util.XY
import world.cartos.RoomyMaze

@Serializable
class EnclosedLevel(
    val width: Int,
    val height: Int
    ) : Level() {

    private val chunk = Chunk(width, height).generateLevel {
        RoomyMaze.carveLevel(0, 0, width - 1, height - 1, { x, y ->
            it.getTerrain(x, y)
        }, { x, y, type ->
            it.setTerrain(x, y, type)
        })
    }

    init {
        stepMap = makeStepMap()
    }

    // Temporary!
    override fun tempPlayerStart(): XY = chunk.tempPlayerStart()
    override fun debugText() = "enclosed level"

    override fun chunkAt(x: Int, y: Int) =
        if (!(x < 0 || y < 0 || x >= width || y >= height)) { chunk } else null

    override fun updateVisibility() {
        chunk.clearVisibility()
        shadowCaster.cast(pov, 12f)
    }

    override fun makeStepMap() = StepMap(width, height,
        { x, y -> chunk.isWalkableAt(x, y) },
        { 0 },
        { 0 }
    )

}

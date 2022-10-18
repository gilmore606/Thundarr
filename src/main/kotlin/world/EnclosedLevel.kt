package world

import kotlinx.serialization.Serializable
import util.StepMap
import util.XY
import world.cartos.RoomyMaze

@Serializable
class EnclosedLevel(
    val levelId: String
    ) : Level() {

    private val chunk: Chunk
    private val width: Int
    private val height: Int
    private val allChunks: Set<Chunk>
    private var isReady = false

    init {
        // TODO: get width/height/layout info from sqlite
        width = 50
        height = 50
        chunk = Chunk(width, height).generateLevel {
            it.onCreate(this, 0, 0, forWorld = false)
            RoomyMaze.carveLevel(0, 0, width - 1, height - 1, it)
        }
        allChunks = setOf(chunk)
        stepMap = makeStepMap()
        isReady = true
    }

    override fun debugText() = "level $levelId"

    override fun allChunks() = allChunks

    override fun receiveChunk(chunk: Chunk) {

    }

    override fun isReady() = isReady

    override fun tempPlayerStart(): XY? = if (isReady) chunk.tempPlayerStart() else null

    override fun chunkAt(x: Int, y: Int) =
        if (!(x < 0 || y < 0 || x >= width || y >= height)) { chunk } else null

    override fun makeStepMap() = StepMap(width, height,
        { x, y -> chunk.isWalkableAt(x, y) },
        { 0 },
        { 0 }
    )

}

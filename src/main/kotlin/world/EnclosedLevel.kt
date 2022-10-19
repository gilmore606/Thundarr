package world

import kotlinx.serialization.Serializable
import util.StepMap
import util.XY
import world.cartos.RoomyMaze

@Serializable
class EnclosedLevel(
    private val levelId: String
    ) : Level() {

    private var chunk: Chunk? = null
    private var allChunks = setOf<Chunk>()
    private var isReady = false

    private val width: Int
        get() = chunk?.width ?: 1
    private val height: Int
        get() = chunk?.height ?: 1


    init {
        ChunkLoader.getLevelChunk(this, levelId) { receiveChunk(it) }
    }

    override fun receiveChunk(chunk: Chunk) {
        this.chunk = chunk
        allChunks = setOf(chunk)
        stepMap = makeStepMap()
        isReady = true
    }

    override fun debugText() = "level $levelId"

    override fun allChunks() = allChunks

    override fun isReady() = isReady

    override fun tempPlayerStart(): XY? = if (isReady && chunk != null) chunk!!.tempPlayerStart() else null

    override fun chunkAt(x: Int, y: Int) =
        if (!(x < 0 || y < 0 || x >= width || y >= height)) { chunk } else null

    override fun makeStepMap() = StepMap(width, height,
        { x, y -> chunk?.isWalkableAt(x, y) ?: false },
        { 0 },
        { 0 }
    )

}

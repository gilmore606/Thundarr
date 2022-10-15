package world

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import util.*
import java.io.File

const val CHUNK_SIZE = 64
const val CHUNKS_AHEAD = 3
const val STEP_CHUNKS_AHEAD = 1

@Serializable
class WorldLevel() : Level() {

    @Transient val CHUNKS_WIDE = CHUNKS_AHEAD * 2 + 1

    @Transient val chunks = Array(CHUNKS_WIDE) { Array(CHUNKS_WIDE) { Chunk(1, 1) } }

    private val loadedChunks = mutableSetOf<Chunk>()

    @Transient private val lastPovChunk = XY(-999,  -999)  // upper-left corner of the last chunk POV was in, to check chunk crossings

    init {
        stepMap = makeStepMap()
    }

    override fun allChunks() = loadedChunks

    // Temporary
    override fun tempPlayerStart(): XY {
        setPov(200, 200)
        loadedChunks.forEach { it.clearSeen() }
        return chunks[CHUNKS_AHEAD][CHUNKS_AHEAD].tempPlayerStart()
    }

    override fun debugText(): String {
        val ourChunk = chunks[CHUNKS_AHEAD][CHUNKS_AHEAD]
        return "chunk ${ourChunk.x}x${ourChunk.y}"
    }

    override fun onSetPov() {
        populateChunks()
    }

    override fun onRestore() {
        populateChunks()
        loadedChunks.forEach { it.onRestore(this) }
        updateStepMap()
    }

    private fun xToChunkX(x: Int) = (x / CHUNK_SIZE) * CHUNK_SIZE + if (x < 0) -CHUNK_SIZE else 0
    private fun yToChunkY(y: Int) = (y / CHUNK_SIZE) * CHUNK_SIZE + if (y < 0) -CHUNK_SIZE else 0

    override fun chunkAt(x: Int, y: Int): Chunk? {
        if (x >= chunks[0][0].x && y >= chunks[0][0].y) {
            val cx = (x - chunks[0][0].x) / CHUNK_SIZE
            val cy = (y - chunks[0][0].y) / CHUNK_SIZE
            if (cx >= 0 && cy >= 0 && cx < CHUNKS_WIDE && cy < CHUNKS_WIDE) {
                return chunks[cx][cy]
            }
        }
        return null
    }

    private fun populateChunks() {
        val chunkX = xToChunkX(pov.x)
        val chunkY = yToChunkY(pov.y)
        if (chunkX == lastPovChunk.x && chunkY == lastPovChunk.y) {
            return
        }
        lastPovChunk.x = chunkX
        lastPovChunk.y = chunkY

        val activeChunks = mutableSetOf<Chunk>()
        for (cx in -CHUNKS_AHEAD..CHUNKS_AHEAD) {
            for (cy in -CHUNKS_AHEAD..CHUNKS_AHEAD) {
                val chunk = getChunkAt(chunkX + cx * CHUNK_SIZE, chunkY + cy * CHUNK_SIZE)
                activeChunks.add(chunk)
                chunks[cx+ CHUNKS_AHEAD][cy+ CHUNKS_AHEAD] = chunk
            }
        }
        activeChunks.filter { !loadedChunks.contains(it) }
            .map { dirtyLightsAroundChunk(it) ; loadedChunks.add(it) }
        loadedChunks.filter { !activeChunks.contains(it) }
            .map { unloadChunk(it) }
    }

    private fun getChunkAt(x: Int, y: Int): Chunk =
        loadedChunks.firstOrNull { it.x == x && it.y == y } ?: loadChunkAt(x, y)

    private fun loadChunkAt(x: Int, y: Int): Chunk {
        val filename = Chunk.filepathAt(x, y)
        val chunk: Chunk
        if (File(filename).exists()) {
            log.debug("Loading chunk at $x $y")
            chunk = Json.decodeFromString(File(filename).readBytes().gzipDecompress())
            chunk.onRestore(this)
        } else {
            log.debug("Creating chunk at $x $y")
            chunk = Chunk(CHUNK_SIZE, CHUNK_SIZE)
            chunk.onCreate(this, x, y, forWorld = true)
        }
        return chunk
    }

    private fun unloadChunk(chunk: Chunk) {
        chunk.unload(
            director.removeActorsInArea(chunk.x, chunk.y, chunk.x + CHUNK_SIZE - 1, chunk.y + CHUNK_SIZE - 1)
        )
        loadedChunks.remove(chunk)
    }

    private fun dirtyLightsAroundChunk(chunk: Chunk) {
        log.debug("Reprojecting for chunk ${chunk.x} ${chunk.y}")
        for (x in -1 .. chunk.width) {
            dirtyLightsTouching(x + chunk.x, chunk.y - 1)
            dirtyLightsTouching(x + chunk.x, chunk.y + chunk.height)
        }
        for (y in -1 .. chunk.height) {
            dirtyLightsTouching(chunk.x - 1, y + chunk.y)
            dirtyLightsTouching(chunk.x + chunk.width, y + chunk.y)
        }
    }

    override fun makeStepMap() = StepMap(CHUNK_SIZE * (STEP_CHUNKS_AHEAD * 2 + 1), CHUNK_SIZE * (STEP_CHUNKS_AHEAD * 2 + 1),
        { x, y -> isWalkableAt(x, y) },
        { chunks[CHUNKS_AHEAD- STEP_CHUNKS_AHEAD][CHUNKS_AHEAD- STEP_CHUNKS_AHEAD].x },
        { chunks[CHUNKS_AHEAD- STEP_CHUNKS_AHEAD][CHUNKS_AHEAD- STEP_CHUNKS_AHEAD].y }
    )

}

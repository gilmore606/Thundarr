package world

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ktx.async.KtxAsync
import util.*
import util.hasOneWhere
import java.io.File

const val CHUNK_SIZE = 64
const val CHUNKS_AHEAD = 3
const val STEP_CHUNKS_AHEAD = 1

@Serializable
class WorldLevel() : Level() {

    @Transient val CHUNKS_WIDE = CHUNKS_AHEAD * 2 + 1

    @Transient val chunks = Array(CHUNKS_WIDE) { Array<Chunk?>(CHUNKS_WIDE) { null } }

    @Transient private val loadedChunks = mutableSetOf<Chunk>()

    @Transient private val lastPovChunk = XY(-999,  -999)  // upper-left corner of the last chunk POV was in, to check chunk crossings

    init {
        stepMap = makeStepMap()
    }

    override fun allChunks() = loadedChunks

    override fun isReady() = loadedChunks.size >= CHUNKS_WIDE * CHUNKS_WIDE

    override fun tempPlayerStart(): XY? {
        val start = chunks[CHUNKS_AHEAD][CHUNKS_AHEAD]?.tempPlayerStart()
        if (start != null) {
            loadedChunks.forEach { it.clearSeen() }
            return start
        }
        return null
    }

    override fun debugText(): String = chunks[CHUNKS_AHEAD][CHUNKS_AHEAD]?.let { "chunk ${it.x}x${it.y}" } ?: "???"

    override fun onSetPov() {
        populateChunks()
    }

    override fun unload() {
        super.unload()
        log.info("Unloading ${loadedChunks.size} chunks")
        mutableSetOf<Chunk>().apply { addAll(loadedChunks) }.forEach { unloadChunk(it) }
    }

    override fun onRestore() {
        populateChunks()
        updateStepMap()
    }

    private fun xToChunkX(x: Int) = (x / CHUNK_SIZE) * CHUNK_SIZE + if (x < 0) -CHUNK_SIZE else 0
    private fun yToChunkY(y: Int) = (y / CHUNK_SIZE) * CHUNK_SIZE + if (y < 0) -CHUNK_SIZE else 0

    override fun chunkAt(x: Int, y: Int): Chunk? {
        chunks[0][0]?.also { originChunk ->
            if (x >= originChunk.x && y >= originChunk.y) {
                val cx = (x - originChunk.x) / CHUNK_SIZE
                val cy = (y - originChunk.y) / CHUNK_SIZE
                if (cx >= 0 && cy >= 0 && cx < CHUNKS_WIDE && cy < CHUNKS_WIDE) {
                    return chunks[cx][cy]
                }
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
        log.info("Entered chunk $chunkX $chunkY")
        lastPovChunk.x = chunkX
        lastPovChunk.y = chunkY

        for (cx in -CHUNKS_AHEAD..CHUNKS_AHEAD) {
            for (cy in -CHUNKS_AHEAD..CHUNKS_AHEAD) {
                getExistingChunkAt(chunkX + cx * CHUNK_SIZE, chunkY + cy * CHUNK_SIZE)?.also { existing ->
                    chunks[cx + CHUNKS_AHEAD][cy + CHUNKS_AHEAD] = existing
                } ?: run {
                    loadChunkAt(chunkX + cx * CHUNK_SIZE, chunkY + cy * CHUNK_SIZE)
                }
            }
        }
    }

    private fun getExistingChunkAt(x: Int, y: Int): Chunk? =
        loadedChunks.firstOrNull { it.x == x && it.y == y }

    private fun loadChunkAt(x: Int, y: Int) {
        ChunkLoader.getChunkAt(this, x, y) { chunk ->
            receiveChunk(chunk)
        }
    }

    override fun receiveChunk(chunk: Chunk) {
        val originCx = xToChunkX(pov.x) - CHUNK_SIZE * CHUNKS_AHEAD
        val originCy = yToChunkY(pov.y) - CHUNK_SIZE * CHUNKS_AHEAD
        val cx = (chunk.x - originCx) / CHUNK_SIZE
        val cy = (chunk.y - originCy) / CHUNK_SIZE
        if (cx < 0 || cy < 0 || cx >= CHUNKS_WIDE || cy >= CHUNKS_WIDE) {
            log.error("Received outdated chunk intended for cx $cx cy $cy !")
            return
        }
        log.debug("Received hot chunk $cx $cy !")
        val oldChunk = chunks[cx][cy]
        chunks[cx][cy] = chunk
        loadedChunks.add(chunk)
        shadowDirty = true
        dirtyLightsAroundChunk(chunk)
        oldChunk?.also { if (!hasAttachedChunk(it)) unloadChunk(it) }
    }

    private fun hasAttachedChunk(chunk: Chunk): Boolean {
        var inUse = false
        for (x in 0 until CHUNKS_WIDE) {
            for (y in 0 until CHUNKS_WIDE) {
                if (chunks[x][y] == chunk) {
                    inUse = true
                }
            }
        }
        return inUse
    }

    private fun unloadChunk(chunk: Chunk) {
        chunk.unload(
            director.unloadActorsFromArea(chunk.x, chunk.y, chunk.x + CHUNK_SIZE - 1, chunk.y + CHUNK_SIZE - 1)
        )
        loadedChunks.remove(chunk)
    }

    private fun dirtyLightsAroundChunk(chunk: Chunk) {
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
        { chunks[CHUNKS_AHEAD- STEP_CHUNKS_AHEAD][CHUNKS_AHEAD- STEP_CHUNKS_AHEAD]?.x ?: 0 },
        { chunks[CHUNKS_AHEAD- STEP_CHUNKS_AHEAD][CHUNKS_AHEAD- STEP_CHUNKS_AHEAD]?.y ?: 0 }
    )

}

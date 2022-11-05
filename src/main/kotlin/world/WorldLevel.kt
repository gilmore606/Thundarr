package world

import util.*

const val CHUNK_SIZE = 64
const val CHUNKS_AHEAD = 3
const val STEP_CHUNKS_AHEAD = 1

class WorldLevel() : Level() {

    private val chunksWide = CHUNKS_AHEAD * 2 + 1

    private val chunks = Array(chunksWide) { Array<Chunk?>(chunksWide) { null } }
    private val loadedChunks = mutableSetOf<Chunk>()
    private val originChunkLocation = XY(0,0)   // upper-left corner of the upper-left corner chunk

    private val lastPovChunk = XY(-999,  -999)  // upper-left corner of the last chunk POV was in, to check chunk crossings


    override fun allChunks() = loadedChunks
    override fun levelId() = "world"

    override fun isReady() = loadedChunks.size >= chunksWide * chunksWide

    override fun getPlayerEntranceFrom(fromLevelId: String): XY? {
        if (fromLevelId == "world") {
            // Brand new start.
            val start = chunks[CHUNKS_AHEAD][CHUNKS_AHEAD]?.randomPlayerStart()
            if (start != null) {
                loadedChunks.forEach { it.clearSeen() }
                return start
            }
        } else {

        }
        return null
    }

    override fun debugText(): String = chunks[CHUNKS_AHEAD][CHUNKS_AHEAD]?.let { "chunk ${it.x}x${it.y}" } ?: "???"
    override fun statusText(): String = "the wilderness"
    override fun timeScale() = 3.0f

    override fun onSetPov() {
        populateChunks()
    }

    // Save all live chunks and remove their actors.
    override fun unload() {
        log.info("Unloading ${loadedChunks.size} chunks")
        mutableSetOf<Chunk>().apply { addAll(loadedChunks) }.forEach { unloadChunk(it) }
    }

    override fun unloadChunk(chunk: Chunk, levelId: String) {
        loadedChunks.remove(chunk)
        super.unloadChunk(chunk, levelId)
    }

    override fun onRestore() {
        super.onRestore()
        populateChunks()
        log.info("restored world with weather $weather ${weather.weatherIntensity}")
    }

    private fun xToChunkX(x: Int) = (x / CHUNK_SIZE) * CHUNK_SIZE + if (x < 0) -CHUNK_SIZE else 0
    private fun yToChunkY(y: Int) = (y / CHUNK_SIZE) * CHUNK_SIZE + if (y < 0) -CHUNK_SIZE else 0

    override fun chunkAt(x: Int, y: Int): Chunk? {
        if (x >= originChunkLocation.x && y >= originChunkLocation.y) {
            val cx = (x - originChunkLocation.x) / CHUNK_SIZE
            val cy = (y - originChunkLocation.y) / CHUNK_SIZE
            if (cx >= 0 && cy >= 0 && cx < chunksWide && cy < chunksWide) {
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
        log.info("Entered chunk $chunkX $chunkY")
        lastPovChunk.x = chunkX
        lastPovChunk.y = chunkY
        originChunkLocation.x = chunkX - (CHUNK_SIZE * CHUNKS_AHEAD)
        originChunkLocation.y = chunkY - (CHUNK_SIZE * CHUNKS_AHEAD)

        val oldChunks = mutableSetOf<Chunk>().apply { addAll(loadedChunks) }
        for (cx in -CHUNKS_AHEAD..CHUNKS_AHEAD) {
            for (cy in -CHUNKS_AHEAD..CHUNKS_AHEAD) {
                getExistingChunkAt(chunkX + cx * CHUNK_SIZE, chunkY + cy * CHUNK_SIZE)?.also { existing ->
                    oldChunks.remove(existing)
                    chunks[cx + CHUNKS_AHEAD][cy + CHUNKS_AHEAD] = existing
                } ?: run {
                    loadChunkAt(chunkX + cx * CHUNK_SIZE, chunkY + cy * CHUNK_SIZE)
                }
            }
        }
        oldChunks.forEach { unloadChunk(it) }
    }

    private fun getExistingChunkAt(x: Int, y: Int): Chunk? =
        loadedChunks.firstOrNull { it.x == x && it.y == y }

    private fun loadChunkAt(x: Int, y: Int) {
        ChunkLoader.getWorldChunkAt(this, x, y) { chunk ->
            receiveChunk(chunk)
        }
    }

    override fun receiveChunk(chunk: Chunk) {
        val originCx = xToChunkX(pov.x) - CHUNK_SIZE * CHUNKS_AHEAD
        val originCy = yToChunkY(pov.y) - CHUNK_SIZE * CHUNKS_AHEAD
        val cx = (chunk.x - originCx) / CHUNK_SIZE
        val cy = (chunk.y - originCy) / CHUNK_SIZE
        if (cx < 0 || cy < 0 || cx >= chunksWide || cy >= chunksWide) {
            log.error("**********WTF***********")
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
        for (x in 0 until chunksWide) {
            for (y in 0 until chunksWide) {
                if (chunks[x][y] == chunk) {
                    inUse = true
                }
            }
        }
        return inUse
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

}

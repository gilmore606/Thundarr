package world

import App.saveFileFolder
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import render.tilesets.Glyph
import util.*
import world.terrains.Terrain
import java.io.File

const val CHUNKS_AHEAD = 2

@Serializable
class WorldLevel() : Level() {

    @Transient
    val CHUNKS_WIDE = CHUNKS_AHEAD * 2 + 1

    @Transient
    val chunks = Array(CHUNKS_WIDE) { Array(CHUNKS_WIDE) { Chunk() } }

    private val loadedChunks = mutableSetOf<Chunk>()

    @Transient
    override val stepMap = makeStepMap()

    @Transient
    private val shadowCaster = ShadowCaster(
        { x, y -> isOpaqueAt(x, y) },
        { x, y, vis -> setTileVisibility(x, y, vis) }
    )

    @Transient
    private val lastPovChunk = XY(-999,  -999)  // upper-left corner of the last chunk POV was in, to check chunk crossings

    // Temporary
    override fun tempPlayerStart(): XY {
        setPov(200, 200)
        return chunks[CHUNKS_AHEAD][CHUNKS_AHEAD].tempPlayerStart()
    }

    override fun onSetPov() {
        populateChunks()
    }

    override fun onRestore() {
        populateChunks()
        updateStepMap()
    }

    private inline fun xToChunkX(x: Int) = (x / CHUNK_SIZE) * CHUNK_SIZE
    private inline fun yToChunkY(y: Int) = (y / CHUNK_SIZE) * CHUNK_SIZE

    private inline fun chunkAt(x: Int, y: Int): Chunk? {
        val cx = (x - chunks[0][0].x) / CHUNK_SIZE
        val cy = (y - chunks[0][0].y) / CHUNK_SIZE
        if (cx >= 0 && cy >= 0 && cx < CHUNKS_WIDE && cy < CHUNKS_WIDE) {
            return chunks[cx][cy]
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

        log.info("Populating chunks...")

        val activeChunks = mutableSetOf<Chunk>()
        for (cx in -CHUNKS_AHEAD..CHUNKS_AHEAD) {
            for (cy in -CHUNKS_AHEAD..CHUNKS_AHEAD) {
                val chunk = getChunkAt(chunkX + cx * CHUNK_SIZE, chunkY + cy * CHUNK_SIZE)
                activeChunks.add(chunk)
                chunks[cx+ CHUNKS_AHEAD][cy+ CHUNKS_AHEAD] = chunk
            }
        }
        loadedChunks.filter { !activeChunks.contains(it) }
            .map { unloadChunk(it) }

        stepMap.setOrigin(chunks[0][0].x, chunks[0][0].y)
    }

    private fun getChunkAt(x: Int, y: Int): Chunk =
        loadedChunks.firstOrNull { it.x == x && it.y == y } ?: loadChunkAt(x, y)

    private fun loadChunkAt(x: Int, y: Int): Chunk {
        val filename = "$saveFileFolder/chunk$x=$y.json.gz"
        val chunk: Chunk
        if (File(filename).exists()) {
            //log.info("Loading chunk at $x $y")
            chunk = Json.decodeFromString(File(filename).readBytes().gzipDecompress())
            chunk.getSavedActors().forEach {
                director.attachActor(it)
            }
        } else {
            //log.info("Creating chunk at $x $y")
            chunk = Chunk()
            chunk.generateAtLocation(x, y)
        }
        loadedChunks.add(chunk)
        return chunk
    }

    private fun unloadChunk(chunk: Chunk) {
        // log.info("Unloading chunk at ${chunk.x} ${chunk.y}")
        chunk.unload(
            director.removeActorsInArea(chunk.x, chunk.y, chunk.x + CHUNK_SIZE - 1, chunk.y + CHUNK_SIZE - 1)
        )
        loadedChunks.remove(chunk)
    }

    override fun forEachCellToRender(doThis: (x: Int, y: Int, vis: Float, glyph: Glyph) -> Unit) {
        for (x in pov.x - 80 until pov.x + 80) {
            for (y in pov.y - 80 until pov.y + 80) {
                val vis = visibilityAt(x, y)
                if (vis > 0f) {
                    doThis(
                        x, y, vis,
                        Terrain.get(getTerrain(x,y)).glyph()
                    )
                }
            }
        }
    }

    override fun getTerrain(x: Int, y: Int): Terrain.Type = chunkAt(x,y)?.getTerrain(x,y) ?: Terrain.Type.TERRAIN_STONEFLOOR

    override fun setTerrain(x: Int, y: Int, type: Terrain.Type) = chunkAt(x,y)?.setTerrain(x,y,type) ?: Unit

    override fun getGlyph(x: Int, y: Int): Glyph = chunkAt(x,y)?.getGlyph(x,y) ?: Glyph.FLOOR

    override fun getPathToPOV(from: XY) = stepMap.pathFrom(from)

    override fun isSeenAt(x: Int, y: Int) = chunkAt(x,y)?.isSeenAt(x,y) ?: false

    override fun isWalkableAt(x: Int, y: Int) = chunkAt(x,y)?.isWalkableAt(x,y) ?: false

    override fun visibilityAt(x: Int, y: Int) = chunkAt(x,y)?.visibilityAt(x,y) ?: 0f

    override fun isOpaqueAt(x: Int, y: Int) = chunkAt(x,y)?.isOpaqueAt(x,y) ?: true

    override fun updateVisibility() {
        loadedChunks.forEach { it.clearVisibility() }
        shadowCaster.cast(pov, 12f)
    }

    override fun makeStepMap() = StepMap(CHUNK_SIZE * CHUNKS_WIDE, CHUNK_SIZE * CHUNKS_WIDE) { x, y ->
        isWalkableAt(x, y)
    }.apply {
        setOrigin(chunks[0][0].x, chunks[0][0].y)
    }

    override fun setTileVisibility(x: Int, y: Int, vis: Boolean) = chunkAt(x,y)?.setTileVisibility(x,y,vis) ?: Unit

}

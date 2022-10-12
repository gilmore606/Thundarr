package world

import App.saveFileFolder
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import render.tilesets.Glyph
import util.ShadowCaster
import util.XY
import util.gzipDecompress
import util.log
import world.terrains.Terrain
import java.io.File

@Serializable
class WorldLevel() : Level() {

    @Transient
    val chunks = Array(3) { Array(3) { Chunk() } }

    private val loadedChunks = mutableSetOf<Chunk>()

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
        return chunks[1][1].tempPlayerStart()
    }

    override fun onSetPov() {
        populateChunks()
    }

    override fun onRestore() {
        populateChunks()
    }

    private inline fun xToChunkX(x: Int) = (x / CHUNK_SIZE) * CHUNK_SIZE
    private inline fun yToChunkY(y: Int) = (y / CHUNK_SIZE) * CHUNK_SIZE
    private inline fun chunkAt(x: Int, y: Int) =
        chunks[(x - chunks[0][0].x) / CHUNK_SIZE][(y - chunks[0][0].y) / CHUNK_SIZE]

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
        for (cx in -1..1) {
            for (cy in -1..1) {
                val chunk = getChunkAt(chunkX + cx * CHUNK_SIZE, chunkY + cy * CHUNK_SIZE)
                activeChunks.add(chunk)
                chunks[cx+1][cy+1] = chunk
            }
        }
        loadedChunks.filter { !activeChunks.contains(it) }
            .map { unloadChunk(it) }
    }

    private fun getChunkAt(x: Int, y: Int): Chunk =
        loadedChunks.firstOrNull { it.x == x && it.y == y } ?: loadChunkAt(x, y)

    private fun loadChunkAt(x: Int, y: Int): Chunk {
        val filename = "$saveFileFolder/chunk$x=$y.json.gz"
        val chunk: Chunk
        if (File(filename).exists()) {
            log.info("Loading chunk at $x $y")
            chunk = Json.decodeFromString(File(filename).readBytes().gzipDecompress())
        } else {
            log.info("Creating chunk at $x $y")
            chunk = Chunk()
            chunk.generateAtLocation(x, y)
        }
        loadedChunks.add(chunk)
        return chunk
    }

    private fun unloadChunk(chunk: Chunk) {
        log.info("Unloading chunk at ${chunk.x} ${chunk.y}")
        chunk.unload()
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

    override fun getTerrain(x: Int, y: Int): Terrain.Type = chunkAt(x,y).getTerrain(x,y)

    override fun setTerrain(x: Int, y: Int, type: Terrain.Type) = chunkAt(x,y).setTerrain(x,y,type)

    override fun getGlyph(x: Int, y: Int): Glyph = try {
        chunkAt(x,y).getGlyph(x,y)
    } catch (e: ArrayIndexOutOfBoundsException) { Glyph.FLOOR }

    override fun getPathToPOV(from: XY): List<XY> {
        // TODO: actually write this
        return listOf(XY(0,0))
    }

    override fun isSeenAt(x: Int, y: Int): Boolean = try {
        chunkAt(x,y).isSeenAt(x,y)
    } catch (e: ArrayIndexOutOfBoundsException) { false }

    override fun isWalkableAt(x: Int, y: Int): Boolean = try {
        chunkAt(x,y).isWalkableAt(x,y)
    } catch (e: ArrayIndexOutOfBoundsException) { false }

    override fun visibilityAt(x: Int, y: Int): Float = try {
        chunkAt(x,y).visibilityAt(x,y)
    } catch (e: ArrayIndexOutOfBoundsException) { 0f }

    override fun isOpaqueAt(x: Int, y: Int): Boolean = try {
        chunkAt(x,y).isOpaqueAt(x,y)
    }catch (e: ArrayIndexOutOfBoundsException) { true }

    override fun updateVisibility() {
        loadedChunks.forEach { it.clearVisibility() }
        shadowCaster.cast(pov, 12f)
    }

    override fun updateStepMaps() {
        // TODO: actually write this
    }

    override fun setTileVisibility(x: Int, y: Int, vis: Boolean)  = chunkAt(x,y).setTileVisibility(x,y,vis)

}

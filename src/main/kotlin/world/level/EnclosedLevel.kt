package world.level

import audio.Speaker
import util.*
import world.Chunk
import world.persist.ChunkLoader
import world.terrains.PortalDoor
import world.terrains.Terrain

open class EnclosedLevel(
    val levelId: String
    ) : Level() {

    protected var chunk: Chunk? = null
    private var allChunks = setOf<Chunk>()
    private var ready = false

    protected val width: Int
        get() = chunk?.width ?: 1
    protected val height: Int
        get() = chunk?.height ?: 1

    init {
        ChunkLoader.getLevelChunk(this, levelId) { receiveChunk(it) }
    }

    override fun receiveChunk(chunk: Chunk) {
        connectChunk(chunk)
        ready = true
    }

    fun connectChunk(chunk: Chunk) {
        this.chunk = chunk
        allChunks = setOf(chunk)
    }

    override fun unload() {
        chunk?.also { unloadChunk(it, levelId) }
    }

    override fun debugText() = "level $levelId"
    override fun statusText() = "level $levelId"

    override fun allChunks() = allChunks

    override fun levelId() = levelId

    override fun isReady() = ready

    // Put the player next to the door he came in.
    // TODO: find a better solution for this
    override fun getPlayerEntranceFrom(fromLevelId: String): XY? {
        if (isReady()) {
            for (x in 0 until width) {
                for (y in 0 until height) {
                    if (getTerrain(x, y) == Terrain.Type.TERRAIN_PORTAL_DOOR) {
                        val doorData = getTerrainData(x, y) as PortalDoor.Data
                        if (doorData.levelId == fromLevelId) {
                            // TODO: this changes when doors aren't all on north edge!
                            return XY(x, y + 1)
                        }
                    }
                }
            }
            log.info("Failed to find world exit in level chunk -- dropping player in randomly!")
            return chunk!!.randomPlayerStart()
        } else {
            return null
        }
    }

    override fun getNewPlayerEntranceFrom(): XY? {
        chunk?.also {
            if (!it.generating) {
                return it.randomPlayerStart()
            }
        }
        return null
    }

    override fun onPlayerEntered() {
        Speaker.clearMusic()
        Speaker.requestSong(Speaker.Song.DUNGEON)
        Speaker.clearAmbience()
        Speaker.requestAmbience(Speaker.Ambience.INDUSTRIAL)
        Speaker.adjustAmbience(Speaker.Ambience.INDUSTRIAL, 1f)
    }

    override fun chunkAt(x: Int, y: Int) =
        if (!(x < 0 || y < 0 || x >= (chunk?.width ?: 0) || y >= (chunk?.height ?: 0))) { chunk } else null

}

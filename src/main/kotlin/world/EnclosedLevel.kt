package world

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import util.*
import world.terrains.PortalDoor
import world.terrains.Terrain

class EnclosedLevel(
    val levelId: String
    ) : Level() {

    private var chunk: Chunk? = null
    private var allChunks = setOf<Chunk>()
    private var ready = false

    private val width: Int
        get() = chunk?.width ?: 1
    private val height: Int
        get() = chunk?.height ?: 1

    override val sunLightSteps = mutableMapOf<Int, LightColor>().apply {
        repeat (24) { n -> this[n] = LightColor(0.08f, 0.15f, 0.3f) }
    }

    init {
        ChunkLoader.getLevelChunk(this, levelId) { receiveChunk(it) }
    }

    override fun receiveChunk(chunk: Chunk) {
        this.chunk = chunk
        allChunks = setOf(chunk)
        stepMap = makeStepMap()
        ready = true
    }

    override fun unload() {
        chunk?.also { unloadChunk(it, levelId) }
    }

    override fun debugText() = "level $levelId"
    override fun statusText() = "level $levelId"

    override fun allChunks() = allChunks

    override fun levelId() = levelId

    override fun isReady() = ready

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

    override fun chunkAt(x: Int, y: Int) =
        if (!(x < 0 || y < 0 || x >= width || y >= height)) { chunk } else null

    override fun makeStepMap() = StepMap(width, height,
        { x, y -> chunk?.isWalkableAt(x, y) ?: false },
        { 0 },
        { 0 }
    )

}

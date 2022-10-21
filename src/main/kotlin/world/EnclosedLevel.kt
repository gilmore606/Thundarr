package world

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import util.LightColor
import util.StepMap
import util.XY
import util.log
import world.terrains.PortalDoor
import world.terrains.Terrain

class EnclosedLevel(
    val levelId: String
    ) : Level() {

    private var chunk: Chunk? = null
    private var allChunks = setOf<Chunk>()

    private val width: Int
        get() = chunk?.width ?: 1
    private val height: Int
        get() = chunk?.height ?: 1

    override val sunLightSteps = mutableMapOf<Int, LightColor>().apply {
        repeat (24) { n -> this[n] = LightColor(0.1f, 0.1f, 0.1f) }
    }

    init {
        ChunkLoader.getLevelChunk(this, levelId) { receiveChunk(it) }
    }

    override fun receiveChunk(chunk: Chunk) {
        this.chunk = chunk
        allChunks = setOf(chunk)
        stepMap = makeStepMap()
    }

    override fun unload() {
        chunk?.also { ChunkLoader.saveLevelChunk(it, levelId) }
    }

    override fun debugText() = "level $levelId"

    override fun allChunks() = allChunks

    override fun levelId() = levelId

    override fun isReady() = allChunks.isNotEmpty()

    override fun getPlayerEntranceFrom(level: Level): XY? {
        if (isReady()) {
            for (x in 0 until width) {
                for (y in 0 until height) {
                    if (getTerrain(x, y) == Terrain.Type.TERRAIN_PORTAL_DOOR) {
                        val doorData = Json.decodeFromString<PortalDoor.Data>(getTerrainData(x, y))
                        if (doorData.levelId == level.levelId()) {
                            log.debug("Found world exit at $x $y in level chunk")
                            // TODO: this changes when doors aren't all on north edge!
                            return XY(x, y + 1)
                        }
                    }
                }
            }
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

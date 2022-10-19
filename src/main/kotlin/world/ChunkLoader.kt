package world

import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ktx.async.KtxAsync
import ktx.async.newSingleThreadAsyncContext
import util.UUID
import util.log
import world.terrains.PortalDoor

object ChunkLoader {

    private val coroutineContext = newSingleThreadAsyncContext("chunkLoader")
    private val coroutineScope = CoroutineScope(coroutineContext)
    private var jobs = mutableSetOf<Job>()

    fun isWorking(): Boolean {
        jobs = jobs.filter { it.isActive }.toMutableSet()
        return jobs.isNotEmpty()
    }

    fun getWorldChunkAt(level: Level, x: Int, y: Int, callback: (Chunk)->Unit) {
        jobs.add(coroutineScope.launch {
            val chunk = if (App.save.hasWorldChunk(x, y)) {
                log.debug("Loading chunk at $x $y")
                App.save.getWorldChunk(x, y).apply { onRestore(level) }
            } else {
                log.debug("Creating chunk at $x $y")
                Chunk(CHUNK_SIZE, CHUNK_SIZE).apply {
                    onCreate(x, y)
                    connectLevel(level)
                    generateWorld()
                }
            }
            KtxAsync.launch {
                callback(chunk)
            }
        })
    }

    fun saveWorldChunk(chunk: Chunk) {
        jobs.add(coroutineScope.launch {
            log.debug("ChunkLoader saving chunk ${chunk.x} ${chunk.y}")
            App.save.putWorldChunk(chunk)
        })
    }

    fun getLevelChunk(level: Level, levelId: String, callback: (Chunk)->Unit) {
        jobs.add(coroutineScope.launch {
            if (App.save.hasLevelChunk(levelId)) {
                log.debug("Loading chunk $levelId")
                App.save.getLevelChunk(levelId).apply {
                    onRestore(level)
                    val chunk = this
                    KtxAsync.launch {
                        callback(chunk)
                    }
                }
            } else {
                makeLevelChunk(levelId, callback)
            }
        })
    }

    private fun makeLevelChunk(levelId: String, callback: (Chunk) -> Unit) {
        log.debug("Creating level chunk $levelId")
        val building = App.save.getBuildingForLevel(levelId)
        Chunk(building.floorWidth, building.floorHeight).apply {
            onCreate(0, 0)
            generateLevel(building)
            App.save.putLevelChunk(this, levelId, building.id)
            val chunk = this
            KtxAsync.launch {
                callback(chunk)
            }
        }
    }

    fun makeBuilding(building: Building) {
        App.save.putBuilding(building)
        jobs.add(coroutineScope.launch {
            makeLevelChunk(building.firstLevelId) { log.info("Pre-generated level chunk ${building.firstLevelId} .")}
            KtxAsync.launch {
                App.worldLevel.setTerrainData(building.x, building.y, Json.encodeToString(PortalDoor.Data(
                    enterMsg = building.doorMsg,
                    levelId = building.firstLevelId
                )))
            }
        })
    }
}

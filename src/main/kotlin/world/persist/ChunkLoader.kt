package world.persist

import kotlinx.coroutines.*
import ktx.async.KtxAsync
import ktx.async.newSingleThreadAsyncContext
import util.XY
import util.hasOneWhere
import util.log
import world.level.AttractLevel
import world.level.CHUNK_SIZE
import world.Chunk
import world.gen.Metamap
import world.level.Level

object ChunkLoader {

    private val coroutineContext = newSingleThreadAsyncContext("ChunkLoader")
    private val coroutineScope = CoroutineScope(coroutineContext)
    private var jobs = mutableSetOf<Job>()
    private val openRequests = mutableSetOf<XY>()
    private var locked = false
    private var working = false

    init {
        coroutineScope.launch {
            while (!App.isExiting) {
                delay(250L)
                if (jobs.isNotEmpty()) {
                    jobs = jobs.filter { it.isActive }.toMutableSet()
                }
                working = jobs.isNotEmpty()
            }
        }
    }
    
    fun isWorking() = working

    fun getWorldChunkAt(level: Level, x: Int, y: Int, callback: (Chunk)->Unit) {
        if (openRequests.hasOneWhere { it.x == x && it.y == y }) {
            //log.info("Ignoring request for already-loading chunk at $x $y")
            return
        }
        openRequests.add(XY(x,y))
        locked = true
        jobs.add(coroutineScope.launch {
            if (App.save.hasWorldChunk(x, y)) {
                log.debug("Loading chunk at $x $y")
                val chunk = App.save.getWorldChunk(x, y)
                deliverChunk(chunk, callback)
            } else {
                makeWorldChunk(level, x, y, callback)
            }
        })
        locked = false
    }

    private suspend fun makeWorldChunk(level: Level, x: Int, y: Int, callback: (Chunk)->Unit) {
        log.debug("Creating chunk at $x $y")
        Chunk(CHUNK_SIZE, CHUNK_SIZE).apply {
            onCreate(x, y)
            connectLevel(level)
            generateWorld(level)
            val chunk = this
            deliverChunk(chunk, callback)
        }
    }

    private fun deliverChunk(chunk: Chunk, callback: (Chunk)->Unit) {
        KtxAsync.launch {
            openRequests.removeIf { it.x == chunk.x && it.y == chunk.y }
            callback(chunk)
        }
    }

    fun saveWorldChunk(chunk: Chunk, callback: ()->Unit) {
        locked = true
        jobs.add(coroutineScope.launch {
            log.debug("ChunkLoader saving chunk ${chunk.x} ${chunk.y}")
            App.save.putWorldChunk(chunk, callback)
            App.save.updateWorldMeta(Metamap.metaAtWorld(chunk.x, chunk.y))
        })
        locked = false
    }

    fun saveLevelChunk(chunk: Chunk, levelId: String, callback: ()->Unit) {
        locked = true
        jobs.add(coroutineScope.launch {
            log.debug("ChunkLoader saving level chunk $levelId")
            App.save.updateLevelChunk(chunk, levelId, callback)
        })
        locked = false
    }

    fun getLevelChunk(level: Level, levelId: String, callback: (Chunk)->Unit) {
        locked = true
        jobs.add(coroutineScope.launch {
            if (App.save.hasLevelChunk(levelId)) {
                log.debug("Loading chunk $levelId")
                val chunk = App.save.getLevelChunk(levelId).apply { onRestore(level) }
                KtxAsync.launch { callback(chunk) }
            } else {
                makeLevelChunk(level, levelId, callback)
            }
        })
        locked = false
    }

    private fun makeLevelChunk(level: Level, levelId: String, callback: (Chunk) -> Unit) {
        log.debug("Creating level chunk $levelId")

        if (levelId == "attract") {
            Chunk(AttractLevel.dimension, AttractLevel.dimension).apply {
                onCreate(0, 0)
                connectLevel(level)
                generateAttractLevel(level)
                val chunk = this
                KtxAsync.launch { callback(chunk) }
            }
            return
        }

        val building = App.save.getBuildingForLevel(levelId) ?: throw RuntimeException("No building found for level $levelId !")
        Chunk(building.floorWidth(), building.floorHeight()).apply {
            onCreate(0, 0)
            generateLevel(level, building)
            App.save.putLevelChunk(this, levelId, building.id)
            val chunk = this
            KtxAsync.launch { callback(chunk) }
        }
    }

}

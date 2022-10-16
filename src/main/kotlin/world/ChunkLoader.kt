package world

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ktx.async.KtxAsync
import ktx.async.newSingleThreadAsyncContext
import util.XY
import util.gzipDecompress
import util.log
import java.io.File

object ChunkLoader {

    class Request(
        val worldXY: XY? = null,
        val levelID: String? = null
    )

    class ChunkInProgress(
        val worldXY: XY? = null,
        val levelID: String? = null,
        val job: Job
    )

    private val listeners = mutableMapOf<Level, Job>()
    private val channels = mutableMapOf<Level, MutableStateFlow<Chunk?>>()
    private val inProgress = mutableMapOf<Level, MutableSet<ChunkInProgress>>()

    private val coroutineScope = CoroutineScope(newSingleThreadAsyncContext("chunkLoader"))

    fun linkLevel(level: Level): MutableStateFlow<Chunk?> {
        inProgress[level] = mutableSetOf()
        val listener = coroutineScope.launch {
            level.chunkRequests.collect {
                it?.worldXY?.also { xy ->
                    inProgress[level]?.add(ChunkInProgress(worldXY = xy, job = coroutineScope.launch {
                        getWorldChunk(xy, level)
                    }))
                }
            }
        }
        listeners[level] = listener
        val channel = MutableStateFlow<Chunk?>(null)
        channels[level] = channel
        return channel
    }

    fun unlinkLevel(level: Level) {
        listeners[level]?.cancel()
        listeners.remove(level)
        channels.remove(level)
    }

    private fun getWorldChunk(xy: XY, level: Level) {
        val filename = Chunk.filepathAt(xy.x, xy.y)
        val chunk: Chunk
        if (File(filename).exists()) {
            log.info("Loading chunk at $xy")
            chunk = Json.decodeFromString(File(filename).readBytes().gzipDecompress())
            chunk.onRestore(level)
        } else {
            log.info("Creating chunk at $xy")
            chunk = Chunk(CHUNK_SIZE, CHUNK_SIZE)
            chunk.onCreate(level, xy.x, xy.y, forWorld = true)
            log.info("Created chunk $xy !")
        }
        KtxAsync.launch {
            channels[level]?.emit(chunk)
            inProgress[level]?.removeIf { it.worldXY == xy }
        }
    }

}

package world

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ktx.async.KTX
import ktx.async.KtxAsync
import ktx.async.newSingleThreadAsyncContext
import util.XY
import util.gzipDecompress
import util.log
import java.io.File
import kotlin.coroutines.CoroutineContext

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

    val coroutineContext = newSingleThreadAsyncContext("chunkLoader")
    val coroutineScope = CoroutineScope(coroutineContext)

    fun linkLevel(level: Level): MutableStateFlow<Chunk?> {
        inProgress[level] = mutableSetOf()
        val listener = KtxAsync.launch {
            level.chunkRequests.collect {
                it?.worldXY?.also { xy ->
                    coroutineScope.launch {
                        log.info("loader collected $xy")
                        val chunk = getWorldChunk(xy, level)
                        log.info("loader emitting $chunk.x $chunk.y")
                        //channels[level]?.emit(chunk)
                        channels[level]?.value = chunk
                    }
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

    private fun getWorldChunk(xy: XY, level: Level): Chunk {
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
            //inProgress[level]?.removeIf { it.worldXY == xy }
        return chunk
    }

}

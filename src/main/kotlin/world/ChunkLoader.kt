package world

import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ktx.async.KtxAsync
import ktx.async.newSingleThreadAsyncContext
import util.XY
import util.gzipDecompress
import util.log
import java.io.File

object ChunkLoader {

    private val coroutineContext = newSingleThreadAsyncContext("chunkLoader")
    private val coroutineScope = CoroutineScope(coroutineContext)

    fun getChunkAt(level: Level, x: Int, y: Int, callback: (chunk: Chunk)->Unit ) {
        coroutineScope.launch {
            val chunk = getWorldChunk(XY(x, y), level)
            KtxAsync.launch {
                callback(chunk)
            }
        }
    }

    private fun getWorldChunk(xy: XY, level: Level): Chunk {
        val filename = Chunk.filepathAt(xy.x, xy.y)
        val chunk: Chunk
        if (File(filename).exists()) {
            log.debug("Loading chunk at $xy")
            chunk = Json.decodeFromString(File(filename).readBytes().gzipDecompress())
            chunk.onRestore(level)
        } else {
            log.debug("Creating chunk at $xy")
            chunk = Chunk(CHUNK_SIZE, CHUNK_SIZE)
            chunk.onCreate(level, xy.x, xy.y, forWorld = true)
        }
        return chunk
    }

}

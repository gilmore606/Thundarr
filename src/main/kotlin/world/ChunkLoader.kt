package world

import kotlinx.coroutines.*
import ktx.async.KtxAsync
import ktx.async.newSingleThreadAsyncContext
import util.XY
import util.log

object ChunkLoader {

    private val coroutineContext = newSingleThreadAsyncContext("chunkLoader")
    private val coroutineScope = CoroutineScope(coroutineContext)

    fun isWorking() = coroutineScope.isActive

    fun getChunkAt(level: Level, x: Int, y: Int, callback: (chunk: Chunk)->Unit ) {
        coroutineScope.launch {
            val chunk = App.saveState.getWorldChunk(x, y, level)
            KtxAsync.launch {
                callback(chunk)
            }
        }
    }

    fun saveChunk(chunk: Chunk) {
        coroutineScope.launch {
            log.debug("ChunkLoader saving chunk ${chunk.x} ${chunk.y}")
            App.saveState.putWorldChunk(chunk)
        }
    }

}

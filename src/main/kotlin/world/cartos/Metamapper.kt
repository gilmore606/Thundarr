package world.cartos

import App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ktx.async.KtxAsync
import ktx.async.newSingleThreadAsyncContext
import ui.panels.Console
import util.log
import world.ChunkMeta
import world.level.CHUNK_SIZE

object Metamapper {

    private val chunkRadius = 200

    private val coroutineContext = newSingleThreadAsyncContext("Metamapper")
    private val coroutineScope = CoroutineScope(coroutineContext)

    var isWorking = false

    fun buildWorld() {
        isWorking = true
        coroutineScope.launch {
            log.info("Metamapper building world...")

            val metas = Array(chunkRadius * 2) { Array(chunkRadius * 2) { ChunkMeta() } }

            for (ix in -chunkRadius until chunkRadius) {
                for (iy in -chunkRadius until chunkRadius) {
                    val chunkX = ix * CHUNK_SIZE
                    val chunkY = iy * CHUNK_SIZE
                    metas[ix + chunkRadius][iy + chunkRadius].apply {
                        x = chunkX
                        y = chunkY
                    }
                }
            }


            for (ix in -chunkRadius until chunkRadius) {
                App.save.putWorldMetas(metas[ix + chunkRadius])
                if (ix % 20 == 0) {
                    KtxAsync.launch {
                        Console.say("wrote latitude $ix")
                    }
                }
            }

            log.info("Metamapper completed!")
            isWorking = false
        }
    }

}

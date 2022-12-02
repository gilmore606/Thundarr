package ui.modals

import render.Screen
import render.batches.QuadBatch
import render.tilesets.UITileSet
import util.log
import world.cartos.Metamapper
import world.persist.ChunkLoader

class LoadingModal(text: String) : SplashModal(text) {

    companion object {
        val boxBatch = QuadBatch(UITileSet())
    }
    override fun openSound() = null
    override fun closeSound() = null

    override fun newBoxBatch() = LoadingModal.boxBatch
    override fun newThingBatch() = null
    override fun newActorBatch() = null

    val minDelayMs = 1000

    private var launchTime = System.currentTimeMillis()
    private var lastCheckTime = System.currentTimeMillis()
    private val checkMs = 100

    init {
        dismissible = false
    }

    override fun onRender(delta: Float) {
        super.onRender(delta)
        if (dismissed) return
        val time = Screen.timeMs
        if (time > launchTime + minDelayMs) {
            if (lastCheckTime < time + checkMs) {
                if (!isLoading()) {
                    dismiss()
                    log.info("Done loading.")
                }
                lastCheckTime = time
            }
        }
    }

    private fun isLoading() = ChunkLoader.isWorking() || Metamapper.isWorking

    override fun dispose() {
        textBatch.dispose()
    }
}

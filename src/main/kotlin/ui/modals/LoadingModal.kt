package ui.modals

import render.Screen
import render.batches.QuadBatch
import render.tilesets.UITileSet
import util.log
import world.gen.Metamap
import world.persist.ChunkLoader
import java.lang.Float.min

class LoadingModal(text: String, val withProgress: Boolean = false) :
    SplashModal(text, forceHeight = if (withProgress) 120 else 60) {

    companion object {
        val boxBatch = QuadBatch(UITileSet())
    }
    override fun openSound() = null
    override fun closeSound() = null

    override fun newThingBatch() = null
    override fun newActorBatch() = null

    val minDelayMs = 1000

    private var launchTime = System.currentTimeMillis()
    private var lastCheckTime = System.currentTimeMillis()
    private val checkMs = 100

    private var progress = 0f

    init {
        dismissible = false
    }

    fun addProgress(more: Float) { progress = min(1f, progress + more) }

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

    override fun drawBackground() {
        super.drawBackground()
        if (!isAnimating()) {
            if (!withProgress) return
            val padding = 30
            boxBatch.addHealthBar(
                x + padding, y + 80, x + width - (padding), y + 100,
                (progress * 100f).toInt(), 101, allGreen = true
            )
        }
    }

    private fun isLoading() = ChunkLoader.isWorking() || Metamap.isWorking

    override fun dispose() {
        textBatch.dispose()
    }
}

package ui.modals

import render.Screen
import render.batches.QuadBatch
import render.tilesets.Glyph
import render.tilesets.UITileSet
import util.log
import world.gen.Metamap
import world.persist.ChunkLoader
import java.lang.Float.min

class LoadingModal(
    text: String,
    val withProgress: Boolean = false,
    val forceUp: Boolean = false,
    val withMoon: Boolean = false,
) : SplashModal(text, forceHeight = if (withProgress) 100 else 60) {

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
    fun setProgress(total: Float) { progress = total }

    fun abort() {
        dismissed = true
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

    override fun drawBackground() {
        super.drawBackground()
        if (!isAnimating()) {
            if (!withProgress) return
            val padding = 30
            boxBatch.addHealthBar(
                x + padding, y + 65, x + width - (padding), y + 85,
                (progress * 100f).toInt(), 101, allGreen = true
            )
        }
        if (withMoon) {
            var a = progress * 2f
            if (a > 1f) a += ((a - 1f) * -2f)
            val moony = y / 2 - 64
            boxBatch.addPixelQuad(
                (Screen.width / 2) - 128, moony, Screen.width / 2, moony + 128,
                boxBatch.getTextureIndex(Glyph.MOON_BANNER_L), alpha = a
            )
            boxBatch.addPixelQuad(
                (Screen.width / 2), moony, (Screen.width / 2) + 129, moony + 128,
                boxBatch.getTextureIndex(Glyph.MOON_BANNER_R), alpha = a
            )
        }
    }

    private fun isLoading() = forceUp || ChunkLoader.isWorking() || Metamap.isWorking

    override fun dispose() {
        textBatch.dispose()
    }
}

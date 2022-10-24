package ui.modals

import util.log
import world.ChunkLoader

class LoadingModal(text: String) : SplashModal(text) {

    val minDelayMs = 600

    private var launchTime = System.currentTimeMillis()
    private var lastCheckTime = System.currentTimeMillis()
    private val checkMs = 100

    init {
        dismissible = false
    }

    override fun onRender(delta: Float) {
        super.onRender(delta)
        if (dismissed) return
        val time = System.currentTimeMillis()
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

    private fun isLoading() = ChunkLoader.isWorking()
}

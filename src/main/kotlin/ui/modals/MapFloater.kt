package ui.modals

import render.Screen
import render.batches.QuadBatch
import util.wrapText
import world.journal.GameTime

class MapFloater(
    val mapModal: MapModal,
    val screenX: Int,
    val screenY: Int,
    val name: String,
    val description: String,
    val time: Double,
): Modal(250, 120, null, Position.CURSOR) {

    companion object {
        val boxBatch = QuadBatch(Screen.uiTileSet)
    }

    val wrappedDescription = wrapText(description, 250, 10, Screen.smallFont).toMutableList().apply {
        add("\n")
        add("found: " + GameTime(time).dateString)
    }

    init {
        this.x = screenX
        this.y = screenY
        this.animTime = 1f
        this.shadowOffset = 6
        this.borderWidth = 1

        this.height = wrappedDescription.size * 20 + 55

        this.darkenUnder = false
    }


    override fun newBoxBatch() = MapFloater.boxBatch
    override fun newThingBatch() = null
    override fun newActorBatch() = null

    override fun openSound() = null
    override fun closeSound() = null

    override fun onResize(width: Int, height: Int) { }

    fun remoteDismiss() {
        dismissible = true
        dismiss()
    }

    override fun onMouseMovedTo(screenX: Int, screenY: Int) {
        mapModal.onMouseMovedTo(screenX, screenY)
    }

    override fun drawModalText() {
        super.drawModalText()
        drawString(name, 15, 15, Screen.fontColorBold, Screen.font)
        drawWrappedText(wrappedDescription, 15, 45, 20, Screen.smallFont, Screen.fontColorDull)
    }

    override fun dispose() {
        textBatch.dispose()
    }
}

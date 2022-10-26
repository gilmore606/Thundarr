package ui.modals

import render.Screen
import ui.input.Mouse
import world.Entity

class ExamineModal(
    val entity: Entity
) : Modal(400, 400, entity.name()) {

    private val padding = 22

    private val wrappedDesc = wrapText(entity.description(), width - 64, padding, Screen.font)
    private val wrappedInfo = wrapText("You don't know anything interesting about " + entity.iname() + ".", width, padding, Screen.font)

    override fun mouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        dismiss()
        return true
    }

    override fun keyDown(keycode: Int) {
        super.keyDown(keycode)
        dismiss()
    }

    override fun drawText() {
        super.drawText()
        if (isAnimating()) return
        drawWrappedText(wrappedDesc, padding, padding + 60, 24, Screen.font)
        drawWrappedText(wrappedInfo, padding, padding + 80 + 24 * wrappedDesc.size, 24, Screen.font)
    }

    override fun drawEntities() {
        if (isAnimating()) return
        val x0 = x + width - padding - 64
        val y0 = y + padding
        entity.uiBatch().addPixelQuad(x0, y0, x0 + 64, y0 + 64,
            entity.uiBatch().getTextureIndex(entity.glyph(), entity.level(), entity.xy()?.x ?: 0, entity.xy()?.y ?: 0))
    }
}

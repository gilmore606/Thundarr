package ui.modals

import actors.Actor
import render.Screen
import things.Thing
import ui.input.Mouse
import world.Entity

class ExamineModal(
    val entity: Entity,
    position: Position = Position.LEFT
) : Modal(400, 400, entity.name(), position) {

    private val padding = 22

    private val wrappedDesc = wrapText(entity.examineDescription(), width - 64, padding, Screen.font)
    private val wrappedInfo = wrapText(entity.examineInfo(), width, padding, Screen.font)

    override fun onMouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        dismiss()
        return true
    }

    override fun onKeyDown(keycode: Int) {
        super.onKeyDown(keycode)
        dismiss()
    }

    override fun drawText() {
        super.drawText()
        if (isAnimating()) return
        drawWrappedText(wrappedDesc, padding, padding + 60, 24, Screen.font)
        drawWrappedText(wrappedInfo, padding, padding + 80 + 24 * wrappedDesc.size, 24, Screen.font)

        if (entity is Actor) {
            val effects = entity.statuses
            if (effects.isNotEmpty()) {
                var m = ""
                effects.forEachIndexed { n, status ->
                    m += status.name()
                    if (n < effects.lastIndex) { m += ", " }
                }
                val ty = padding + 100 + 24 * (wrappedDesc.size + wrappedInfo.size)
                drawString("Affected by: ", padding, ty)
                drawString(m, padding + 120, ty, Screen.fontColorBold)
            }
        }
    }

    override fun drawEntities() {
        if (isAnimating()) return
        val x0 = x + width - padding - 64
        val y0 = y + padding
        val batch = if (entity is Thing) myThingBatch() else myActorBatch()
        batch.addPixelQuad(x0, y0, x0 + 64, y0 + 64,
            batch.getTextureIndex(entity.glyph(), entity.level(), entity.xy()?.x ?: 0, entity.xy()?.y ?: 0),
            hue = entity.hue())
    }
}

package ui.modals.widgets

import render.Screen
import render.tilesets.Glyph
import ui.input.Mouse
import ui.modals.WidgetModal
import java.lang.Math.abs

class Slider(
    val label: String,
    valInit: Double,
    val valMin: Double,
    val valMax: Double,
    modal: WidgetModal, x: Int, y: Int, width: Int, height: Int,
    val onChange: (Double)->Unit
) : Widget(modal, x, y, width, height) {

    var value = valInit
    var held = false

    val thumbWidth = 12

    override fun draw() {
        drawQuad(0, 40, width, 4, Glyph.BOX_BORDER)
        drawQuad(thumbX() - thumbWidth, 28, thumbWidth * 2, thumbWidth * 2, Glyph.BUTTON_SYSTEM)
    }

    override fun drawText() {
        drawString(label, 0, 0, Screen.fontColorDull, Screen.smallFont)
    }

    override fun onMouseClicked(x: Int, y: Int, button: Mouse.Button) {
        if (abs(thumbX() - x) < thumbWidth) {
            held = true
        }
    }

    override fun onMouseUp(x: Int, y: Int, button: Mouse.Button) {
        held = false
    }

    override fun onMouseExited() {
        held = false
    }

    override fun onMouseMovedTo(x: Int, y: Int) {
        if (!held) return
        value = (x.toDouble() / width.toDouble()) * (valMax - valMin) + valMin
        onChange(value)
    }

    fun thumbX() =(((value - valMin) / (valMax - valMin)) * width).toInt()
}

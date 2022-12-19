package ui.modals.widgets

import render.Screen
import render.tilesets.Glyph
import ui.input.Mouse
import ui.modals.WidgetModal
import java.lang.Double.max
import java.lang.Double.min
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
    val labelWidth = 80
    val numberWidth = 65
    val valueWidth = width - (labelWidth + numberWidth)

    override fun draw() {
        drawQuad(labelWidth, 10, valueWidth, 4, Glyph.BOX_BORDER)
        drawQuad(thumbX() - thumbWidth, 0, thumbWidth * 2, thumbWidth * 2, Glyph.BUTTON_SYSTEM)
    }

    override fun drawText() {
        drawString(label, 0, 0, Screen.fontColorDull, Screen.smallFont)
        drawString("%.3f".format(value), width - numberWidth + 15, 0, Screen.fontColorBold, Screen.font)
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
        value = max(valMin, min(valMax, ((x - labelWidth).toDouble() / valueWidth.toDouble()) * (valMax - valMin) + valMin))
        onChange(value)
    }

    fun thumbX() =(((value - valMin) / (valMax - valMin)) * valueWidth).toInt() + labelWidth
}

package ui.modals.widgets

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import render.Screen
import render.tilesets.Glyph
import ui.input.Mouse
import ui.modals.WidgetModal

abstract class Widget(
    val modal: WidgetModal,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
) {

    open fun draw() { }
    open fun drawText() { }
    open fun onMouseClicked(x: Int, y: Int, button: Mouse.Button) { }
    open fun onMouseMovedTo(x: Int, y: Int) { }
    open fun onMouseUp(x: Int, y: Int, button: Mouse.Button) { }
    open fun onMouseExited() { }

    fun drawString(text: String, x: Int, y: Int, color: Color = Screen.fontColor, font: BitmapFont = Screen.font) {
        modal.drawString(text, x + this.x, y + this.y, color, font)
    }

    fun drawQuad(x: Int, y: Int, width: Int, height: Int, glyph: Glyph) {
        modal.drawQuad(this.x + x, this.y + y, width, height, glyph)
    }

}

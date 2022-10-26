package ui.input

import com.badlogic.gdx.Input
import ktx.app.KtxInputAdapter
import render.Screen

object Mouse : KtxInputAdapter {

    enum class Button { LEFT, MIDDLE, RIGHT }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        Screen.mouseMovedTo(screenX, screenY)
        return super.mouseMoved(screenX, screenY)
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        Screen.mouseMovedTo(screenX, screenY)
        return true
    }

    override fun scrolled(amountX: Float, amountY: Float): Boolean {
        Screen.mouseScrolled(amountY)
        return true
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val ourButton = when (button) {
            Input.Buttons.LEFT -> Button.LEFT
            Input.Buttons.MIDDLE -> Button.MIDDLE
            Input.Buttons.RIGHT -> Button.RIGHT
            else -> Button.RIGHT
        }
        if (Screen.mouseDown(screenX, screenY, ourButton)) return true
        return super.touchDown(screenX, screenY, pointer, button)
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val ourButton = when (button) {
            Input.Buttons.LEFT -> Button.LEFT
            Input.Buttons.MIDDLE -> Button.MIDDLE
            Input.Buttons.RIGHT -> Button.RIGHT
            else -> Button.RIGHT
        }
        if (Screen.mouseUp(screenX, screenY, ourButton)) return true
        return super.touchUp(screenX, screenY, pointer, button)
    }
}

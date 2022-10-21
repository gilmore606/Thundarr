package ui.modals

import com.badlogic.gdx.Input
import render.GameScreen

abstract class SelectionModal(
    width: Int, height: Int, title: String? = null, position: Modal.Position = Position.LEFT,
    default: Int =  -1
) : Modal(width, height, title, position) {

    protected var selection = default
    protected var maxSelection = 1

    protected var headerPad = 70
    protected var padding = 15
    protected var spacing = 22

    protected fun selectNext() {
        selection++
        if (selection > maxSelection) selection = 0
    }

    protected fun selectPrevious() {
        selection--
        if (selection < 0) selection = maxSelection
    }

    abstract fun doSelect()

    protected fun drawOptionText(text: String, index: Int) {
        drawString(text, padding, headerPad + spacing * index,
            if (index == selection) GameScreen.fontColorBold else GameScreen.fontColor)
    }

    override fun keyDown(keycode: Int) {
        super.keyDown(keycode)
        when (keycode) {
            Input.Keys.NUMPAD_2, Input.Keys.DOWN -> { selectNext() }
            Input.Keys.NUMPAD_8, Input.Keys.UP -> { selectPrevious() }
            Input.Keys.SPACE, Input.Keys.NUMPAD_5, Input.Keys.ENTER, Input.Keys.NUMPAD_ENTER -> {
                doSelect()
            }
        }
    }

}

package ui.modals.widgets

import render.Screen
import ui.input.Mouse
import ui.modals.WidgetModal

class ButtonStrip(
    val buttons: List<String>,
    valInit: Int,
    modal: WidgetModal, x: Int, y: Int, width: Int, height: Int,
    val onChange: (Int)->Unit
) : Widget(modal, x, y, width, height) {

    var value = valInit

    val buttonWidth = width / buttons.size

    override fun draw() {

    }

    override fun drawText() {
        buttons.forEachIndexed { n, label ->
            drawCenterText(label, n * buttonWidth, 0, buttonWidth, if (n == value) Screen.fontColorBold else Screen.fontColorDull)
        }
    }

    override fun onMouseClicked(x: Int, y: Int, button: Mouse.Button) {
        value = (x / buttonWidth)
        onChange(value)
    }
}

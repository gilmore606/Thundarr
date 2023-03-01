package ui.modals

import com.badlogic.gdx.Input
import render.Screen
import ui.input.Keyboard
import ui.input.Keydef
import ui.panels.Console

class ControlsRebindModal(
    val forKey: Keydef,
    val currentDesc: String,
    val parent: ControlsModal,
) : Modal(360, 80, null, Position.CENTER_LOW) {

    private val padding = 24

    init {
        adjustWidth()
    }

    private fun adjustWidth() {
        this.width = padding * 2 + measure("Hit a new key for: " + forKey.description)
        onResize(Screen.width, Screen.height)
    }

    override fun drawModalText() {
        drawString("Hit a new key for: ", padding, padding, Screen.fontColor, Screen.font)
        drawString(forKey.description, padding + 160, padding, Screen.fontColorBold, Screen.font)
        drawCenterText("(currently bound to : $currentDesc)", 0, padding + 24, width,
            Screen.fontColorDull, Screen.smallFont)
    }

    override fun receiveRawKeys() = true

    override fun onRawKeyDown(keyCode: Int) {
        if (keyCode == Input.Keys.ESCAPE) parent.abortChild()

        parent.codeToName(keyCode)?.also { keyName ->
            Keyboard.binds[keyCode]?.also { oldBind ->
                if (!oldBind.remappable) {
                    Console.say(keyName + " is already bound to " + oldBind.description + " which can't be rebound.")
                    return
                } else {
                    parent.unbind(keyCode, oldBind)
                }
            }
            val oldCode = Keyboard.codeForBind(forKey)
            if (oldCode > -1) parent.unbind(oldCode, forKey)
            parent.bind(keyCode, forKey)
        }
    }

    fun remoteClose() {
        dismissible = true
        dismiss()
    }
}

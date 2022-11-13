package ui.modals

import App
import com.badlogic.gdx.Input
import render.Screen
import ui.input.Keyboard
import ui.input.Mouse
import util.NO_DIRECTION
import util.wrapText
import world.journal.JournalEntry

class BigSplashModal(
    title: String,
    private val text: String,
    private val buttonLabel: String,
    private val zoomIn: Boolean = false,
    private val isJournal: Boolean = false
) : Modal(400, 100, title){

    private val padding = 24
    private val spacing = 24
    private val header = 70
    private val buttonSpace = 70

    private val wrappedText = wrapText(text, width, padding, Screen.font)
    private val buttonWidth = measure(buttonLabel)

    private var isHovered = false

    init {
        zoomWhenOpen = 1.3f
        dismissible = false
    }

    override fun onResize(width: Int, height: Int) {
        this.height = wrappedText.size * spacing + header + padding * 2 + buttonSpace
        super.onResize(width, height)
    }

    override fun drawModalText() {
        super.drawModalText()
        drawWrappedText(wrappedText, padding, header + padding, spacing, Screen.font, Screen.fontColor)
        drawCenterText(buttonLabel, 0, header + padding + wrappedText.size * spacing + 38, width,
            if (isHovered) Screen.fontColorBold else Screen.fontColorDull)
    }

    override fun drawBackground() {
        super.drawBackground()
        if (!isAnimating() && isHovered) {
            drawSelectionBox((width / 2) - (buttonWidth / 2) - 8, header + padding + wrappedText.size * spacing + 40,
                buttonWidth + 16, 24)
        }
    }

    override fun onKeyDown(keycode: Int) {
        if (Keyboard.moveKeys[keycode] != null && Keyboard.moveKeys[keycode] != NO_DIRECTION) isHovered = !isHovered
        if (Keyboard.moveKeys[keycode] == NO_DIRECTION) doSelect()
        if (keycode == Input.Keys.ENTER) doSelect()
    }

    override fun onMouseMovedTo(screenX: Int, screenY: Int) {
        val lx = screenX - x
        val ly = screenY - y
        isHovered = ly > (header + padding + wrappedText.size * spacing + 10) && ly < height && lx > padding && lx < width - padding
    }

    override fun onMouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        doSelect()
        return true
    }

    private fun doSelect() {
        if (!isHovered) return
        if (isJournal) {
            App.player.journal.addEntry(JournalEntry(title!!, text))
        }
        dismissible = true
        dismiss()
    }
}

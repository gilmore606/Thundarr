package ui.modals

import App
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import render.Screen
import render.batches.QuadBatch
import render.tilesets.Glyph
import ui.input.Keyboard
import ui.input.Keydef
import ui.input.Mouse
import util.NO_DIRECTION
import util.wrapText
import world.journal.JournalEntry

class BigSplashModal(
    title: String,
    private val text: String,
    private val buttonLabel: String,
    private val zoomIn: Float = 1f,
    private val isJournal: Boolean = false,
    private val portrait: Glyph? = null,
    splashPosition: Modal.Position = Modal.Position.LEFT,
    private val afterDismiss: (()->Unit)? = null
) : Modal(400, 100, title, position = splashPosition){

    val portraitBatch = QuadBatch(Screen.portraitTileSet, maxQuads = 100)

    private val padding = 24
    private val spacing = 24
    private val header = 70
    private val buttonSpace = 70
    private val portraitSize = 64

    private val wrappedText = wrapText(text, width, padding, Screen.font)
    private val buttonWidth = measure(buttonLabel)

    private var isHovered = true

    init {
        zoomWhenOpen = zoomIn
        dismissible = false
    }

    override fun drawEverything() {
        super.drawEverything()
        portraitBatch.clear()
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        Gdx.gl.glEnable(GL20.GL_BLEND)
        if (!isAnimating()) drawPortrait()
        portraitBatch.draw()
    }

    private fun drawPortrait() {
        val shadePad = 8
        portrait?.also { portrait ->
            val px0 = x + width - portraitSize - padding
            val px1 = px0 + portraitSize
            val py0 = y + padding + header
            val py1 = py0 + portraitSize
            portraitBatch.addPixelQuad(px0 - shadePad, py0 - shadePad, px1 + shadePad, py1 + shadePad,
                portraitBatch.getTextureIndex(Glyph.PORTRAIT_SHADE))
            portraitBatch.addPixelQuad(px0, py0, px1, py1, portraitBatch.getTextureIndex(portrait))
        }
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

    override fun onKeyDown(key: Keydef) {
        if (key in Keyboard.moveKeys.keys && key != Keydef.INTERACT) isHovered = !isHovered
        if (key == Keydef.INTERACT) doSelect()
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
        afterDismiss?.invoke()
    }
}

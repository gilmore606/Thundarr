package ui.panels

import kotlinx.coroutines.launch
import ktx.async.KtxAsync
import render.Screen
import render.tilesets.Glyph
import ui.input.Mouse
import ui.modals.ContextMenu
import java.lang.Integer.max
import java.lang.Integer.min

object LeftButtons : Panel() {

    class Button(
        val glyph: Glyph,
        val tip: String,
        val onPress: ()->Unit
    )

    private val xPadding = 36
    private var yPadding = 200
    private val iconSize = 36
    private val iconSizeHovered = 48
    private val slop = 18
    private val wakeSlop = 60
    private var spacing = 0

    private var contraction = width
    private val speed = 18

    private var hovered = -1
    private var mouseInside = false

    private val buttons = listOf(
        Button(Glyph.BUTTON_SYSTEM, "system") {
            App.openSystemMenu()
        },
        Button(Glyph.BUTTON_INVENTORY, "backpack") {
            App.openInventory()
        },
        Button(Glyph.BUTTON_MAP, "world map") {
            App.openMap()
        },
        Button(Glyph.BUTTON_JOURNAL, "journal") {
            App.openJournal()
        }
    )

    private fun shouldShow(): Boolean {
        if (!mouseInside) return false
        if (Screen.topModal == null) return true
        if (Screen.topModal is ContextMenu) return true
        return false
    }

    override fun onResize(width: Int, height: Int) {
        super.onResize(width, height)
        yPadding = (height / 4)
        this.x = xPadding + xMargin
        this.y = yPadding
        this.height = height / 2
        this.width = iconSize * 2
        this.spacing = (LeftButtons.height - iconSize * buttons.size) / (buttons.size - 1)
        this.contraction = this.width
    }

    override fun drawText() {
        if (!shouldShow()) return
        if (hovered < 0) return
        if (contraction > 0) return
        buttons.forEachIndexed { n, button ->
            if (hovered == n) {
                drawString(button.tip, iconSizeHovered + 16, n * spacing + iconSizeHovered / 2 - 11,
                    Screen.fontColorBold, Screen.subTitleFont)
            }
        }

    }

    override fun drawBackground() {
        contraction = if (shouldShow()) {
            max(0, contraction - speed)
        } else {
            min(width + 50, contraction + speed)
        }
        val cx = 0 - contraction
        var cy = 0
        buttons.forEachIndexed { n, button ->
            if (hovered == n) {
                boxBatch.addPixelQuad(
                    cx + this.x - (iconSizeHovered - iconSize) / 2,
                    cy + this.y - (iconSizeHovered - iconSize) / 2,
                    cx + this.x + iconSizeHovered,
                    cy + this.y + iconSizeHovered,
                    boxBatch.getTextureIndex(button.glyph))
            } else {
                boxBatch.addPixelQuad(
                    cx + this.x,
                    cy + this.y,
                    cx + this.x + iconSize,
                    cy + this.y + iconSize,
                    boxBatch.getTextureIndex(button.glyph)
                )
            }
            cy += spacing
        }
    }

    override fun mouseMovedTo(screenX: Int, screenY: Int) {
        val lx = screenX - this.x
        val ly = screenY - this.y
        if (lx < this.width + 50 && ly >= 0 && ly < this.height && !Screen.scrollDragging) {
            mouseInside = true
            if (!shouldShow()) return
            var newHover = -1
            for (i in 0..buttons.lastIndex) {
                val by = (i * spacing)
                if (ly >= by - slop && ly <= by + iconSize + slop && lx < this.width + slop) {
                    newHover = i
                }
            }
            hovered = newHover
        } else {
            mouseInside = false
        }
    }

    override fun mouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        mouseMovedTo(screenX, screenY)
        val lx = screenX - this.x
        val ly = screenY - this.y
        if (lx < this.width + wakeSlop && ly >= 0 && ly < this.height) {
            if (button == Mouse.Button.LEFT) {
                if (hovered >= 0) {
                    mouseInside = false
                    KtxAsync.launch {
                        buttons[hovered].onPress()
                    }
                }
            }
            return true
        }
        return false
    }
}

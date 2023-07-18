package ui.panels

import audio.Speaker
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
        val onPress: ()->Unit,
        val notificationGlyph: (()->Glyph?)? = null,
    ) {
        var size = 36.0
        var targetSize = 36.0
        val growSpeed = 260.0
        fun onRender(delta: Float) {
            if (size < targetSize) size = kotlin.math.min((size + growSpeed * delta), targetSize)
            else if (size > targetSize) size = kotlin.math.max((size - growSpeed * delta), targetSize)
        }
    }

    private val xPadding = 36
    private var yPadding = 200
    private val iconSize = 36
    private val iconSizeHovered = 48
    private val slop = 18
    private val wakeSlop = 70
    private var spacing = 0

    private var contraction = width
    private val speed = 18

    private var hovered = -1
    var mouseInside = false

    private val buttons = listOf(

        Button(Glyph.BUTTON_SYSTEM, "system", {
            App.openSystemMenu()
        }),

        Button(Glyph.BUTTON_INVENTORY, "backpack", {
            App.openInventory()
        }),

        Button(Glyph.BUTTON_GEAR, "gear", {
            App.openGear()
        }),

        Button(Glyph.BUTTON_SKILLS, "skills", {
            App.openSkills()
        }, {
            if (App.player.skillPoints > 0) Glyph.PLUS_ICON_BLUE else null
        }),

        Button(Glyph.BUTTON_MAP, "world map", {
            App.openMap()
        }),

        Button(Glyph.BUTTON_JOURNAL, "journal", {
            App.openJournal()
        })
    )

    fun isShown() = (this.contraction < 1)

    private fun shouldShow(): Boolean {
        if (!mouseInside) return false
        if (Screen.topModal == null) return true
        if (Screen.topModal is ContextMenu) return true
        return false
    }

    override fun onResize(width: Int, height: Int) {
        super.onResize(width, height)
        this.height = min(700, max((height / 1.8).toInt(), 500))
        yPadding = (height - this.height) / 2
        this.x = xPadding + xMargin
        this.y = yPadding
        this.width = iconSize * 2
        this.spacing = (LeftButtons.height - iconSize * buttons.size) / (buttons.size - 1)
        this.contraction = this.width
    }

    override fun onRender(delta: Float) {
        super.onRender(delta)
        buttons.forEach { it.onRender(delta) }
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
            val x0 = cx + this.x - (button.size.toInt()) / 2 + 16
            val y0 = cy + this.y - (button.size.toInt()) / 2 + 24
            val x1 = cx + this.x + button.size.toInt() / 2 + 16
            val y1 = cy + this.y + button.size.toInt() / 2 + 24

            Screen.uiBatch.addPixelQuad(x0, y0, x1, y1, Screen.uiBatch.getTextureIndex(button.glyph))

            button.notificationGlyph?.invoke()?.also { notif ->
                Screen.uiBatch.addPixelQuad(x0, y0, x1, y1, Screen.uiBatch.getTextureIndex(notif))
            }

            cy += spacing
        }
    }

    override fun mouseMovedTo(screenX: Int, screenY: Int) {
        val lx = screenX - this.x
        val ly = screenY - this.y
        if ((!mouseInside && lx < 5 && ly >= 0 && ly < this.height && !Screen.scrollDragging) ||
            (mouseInside && lx < this.width + 50 && ly >= 0 && ly < this.height && !Screen.scrollDragging)) {
            mouseInside = true
            if (!shouldShow()) return
            var newHover = -1
            for (i in 0..buttons.lastIndex) {
                val by = (i * spacing)
                if (ly >= by - slop && ly <= by + iconSize + slop && lx < this.width + slop) {
                    newHover = i
                }
            }
            if (newHover != hovered) Speaker.ui(Speaker.SFX.UIMOVE)
            hovered = newHover
            buttons.forEachIndexed { n, button ->
                button.targetSize = if (n == hovered) 56.0 else 36.0
            }
        } else {
            mouseInside = false
            buttons.forEach { it.targetSize = 36.0 }
        }
    }

    override fun mouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        mouseMovedTo(screenX, screenY)
        if (mouseInside) {
            if (button == Mouse.Button.LEFT) {
                if (hovered >= 0) {
                    mouseInside = false
                    KtxAsync.launch {
                        Speaker.ui(Speaker.SFX.UISELECT)
                        buttons[hovered].onPress()
                    }
                }
            }
            return true
        }
        return false
    }
}

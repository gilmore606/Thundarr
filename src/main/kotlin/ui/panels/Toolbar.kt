package ui.panels

import audio.Speaker
import render.Screen
import render.tilesets.Glyph
import things.Thing
import ui.input.Mouse
import ui.modals.ContextMenu
import ui.modals.ToolbarAddModal
import java.lang.Float.max

object Toolbar : Panel() {

    class Button(
        val number: Int,
        var thingTag: Thing.Tag? = null,
        ) {
        var nextThing: Thing? = null
        var text: String? = null
        var size = 36.0
        var targetSize = 36.0
        val growSpeed = 260.0
        fun onRender(delta: Float) {
            if (size < targetSize) size = kotlin.math.min((size + growSpeed * delta), targetSize)
            else if (size > targetSize) size = kotlin.math.max((size - growSpeed * delta), targetSize)
        }
        fun updateNextThing() {
            nextThing = null
            App.player.contents.forEach {
                if (it.tag == thingTag) {
                    nextThing = it
                    text = it.toolbarName()
                }
            }
            if (nextThing == null) {
                thingTag = null
                text = null
            }
        }
    }

    val buttons = listOf(Button(1), Button(2), Button(3), Button(4),
        Button(5), Button(6), Button(7), Button(8))

    private var yPadding = 36
    private const val iconSize = 36
    private const val iconSizeHovered = 48
    private const val slop = 18
    private const val wakeSlop = 50
    private var spacing = 65

    private var contraction = height
    private var speed = 10
    private var hovered = -1
    private var mouseInside = false
    private var closeDelay = 0f

    fun isShown() = (this.contraction < 1)

    fun refresh() = buttons.forEach { it.updateNextThing() }

    private fun shouldShow(): Boolean {
        if (closeDelay > 0f) return true
        if (Screen.topModal is ToolbarAddModal) return true
        if (!mouseInside) return false
        if (Screen.topModal == null) return true
        if (Screen.topModal is ContextMenu) return true
        return false
    }

    override fun onResize(width: Int, height: Int) {
        super.onResize(width, height)
        this.width = spacing * (buttons.size)
        this.height = yPadding + iconSizeHovered
        this.x = (width - this.width) / 2
        this.y = yPadding
        this.contraction = this.height
    }

    override fun onRender(delta: Float) {
        super.onRender(delta)
        buttons.forEach { it.onRender(delta) }
        closeDelay = max(0f, closeDelay - delta)
    }

    override fun drawText() {
        if (contraction > 0) return
        var cx = 0
        var cy = 0
        buttons.forEachIndexed { n, button ->
            drawString(button.number.toString(),cx + (iconSize / 2) - 8, cy + (iconSize / 2) + 36,
                Screen.fontColorBold, Screen.subTitleFont)
            if (n == hovered) {
                val text = if (inSelectMode()) (Screen.topModal as ToolbarAddModal).sampleText else buttons[n].text
                text?.also { text ->
                    drawString(text, cx + (iconSize / 2) - (measure(text, Screen.smallFont) / 2), cy + (iconSize / 2) + 64,
                    if (inSelectMode()) Screen.fontColorBold else Screen.fontColor, Screen.smallFont)
                }
            }
            cx += spacing
        }
    }

    override fun drawBackground() {
        contraction = if (shouldShow()) {
            Integer.max(0, contraction - speed)
        } else {
            Integer.min(height + 50, contraction + speed)
        }
        var cx = 0
        val cy = 0 - contraction
        buttons.forEachIndexed { n, button ->
            Screen.uiBatch.addPixelQuad(
                cx + this.x - (button.size.toInt()) / 2 + 16,
                cy + this.y - (button.size.toInt()) / 2 + 24,
                cx + this.x + button.size.toInt() / 2 + 16,
                cy + this.y + button.size.toInt() / 2 + 24,
                Screen.uiBatch.getTextureIndex(Glyph.BUTTON_BLANK))
            cx += spacing
        }
    }

    override fun drawEntities() {
        var cx = 0
        var cy = 0 - contraction
        val batch = Screen.uiThingBatch
        buttons.forEachIndexed { n, button ->
            val half = (button.size / 2).toInt()
            val thing = if (n == hovered && inSelectMode()) (Screen.topModal as ToolbarAddModal).newThing else button.nextThing
            thing?.also { nextThing ->
                batch.addPixelQuad(x + cx - half + 16, y + cy - half + 21, x + cx + half + 16, y + cy + half + 21,
                    batch.getTextureIndex(nextThing.glyph()), hue = nextThing.hue())
            }
            cx += spacing
        }
    }

    override fun mouseMovedTo(screenX: Int, screenY: Int) {
        val lx = screenX - this.x
        val ly = screenY - this.y
        if (ly < this.height + slop && lx >= 0 && lx < this.width && !Screen.scrollDragging) {
            mouseInside = true
            if (!shouldShow()) return
            var newHover = -1
            for (i in 0..buttons.lastIndex) {
                val bx = (i * spacing)
                if (lx >= bx - slop && lx <= bx + iconSize + slop && ly < this.height + slop) {
                    newHover = i
                }
            }
            if (newHover != hovered) Speaker.ui(Speaker.SFX.UIMOVE)
            hovered = newHover
            buttons.forEachIndexed { n, button ->
                button.targetSize = if (n == hovered) iconSizeHovered.toDouble() else iconSize.toDouble()
            }
        } else {
            mouseInside = false
            buttons.forEach { it.targetSize = iconSize.toDouble() }
            hovered = -1
        }
    }

    override fun mouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        mouseMovedTo(screenX, screenY)
        val lx = screenX - this.x
        val ly = screenY - this.y
        if (ly < this.height + wakeSlop && lx >=0 && lx < this.width) {
            if (button == Mouse.Button.LEFT) {
                if (hovered >= 0) {
                    doSelect(hovered)
                }
            }
            return true
        }
        return false
    }

    fun onKey(keyNum: Int) {
        if (!inSelectMode() && (keyNum < 1 || buttons[keyNum-1].nextThing == null)) {
            contraction = 0
            closeDelay = 1.5f
            return
        }
        if (inSelectMode() && keyNum < 1) return
        contraction = 0
        closeDelay = 0.75f
        hovered = keyNum - 1
        buttons[keyNum -1].size = iconSizeHovered.toDouble()
        doSelect(keyNum - 1)
    }

    private fun doSelect(buttonNumber: Int) {
        if (buttons[buttonNumber].nextThing == null && !inSelectMode()) return

        Speaker.ui(Speaker.SFX.UISELECT)
        if (inSelectMode()) {
            replaceButton(buttons[buttonNumber], (Screen.topModal as ToolbarAddModal).newThing)
            (Screen.topModal as ToolbarAddModal).remoteClose()
        } else {
            val button = buttons[buttonNumber]
            button.nextThing?.toolbarAction(button.nextThing!!)
        }
    }

    private fun inSelectMode() = Screen.topModal is ToolbarAddModal

    private fun replaceButton(button: Button, newThing: Thing) {
        buttons.forEach { old ->
            if (old.thingTag == newThing.tag) {
                old.thingTag = null
                old.text = null
                old.nextThing = null
            }
        }
        button.thingTag = newThing.tag
        button.text = newThing.toolbarName()
        button.updateNextThing()
    }

    fun beginAdd(newThing: Thing) {
        Screen.addModal(ToolbarAddModal(
            newThing,
            "Pick a toolbar slot for '" + newThing.toolbarName() + "'.",
            newThing.toolbarName() ?: "???"
        ))
    }

    fun getTagsForSave(): List<Thing.Tag?> = mutableListOf<Thing.Tag?>().apply {
        buttons.forEach { add(it.thingTag) }
    }
    fun loadTagsFromSave(newTags: List<Thing.Tag?>) {
        buttons.forEachIndexed { n, button ->
            button.thingTag = newTags[n]
            button.updateNextThing()
        }
    }
}

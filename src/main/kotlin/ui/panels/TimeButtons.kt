package ui.panels

import actors.actions.Wait
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ktx.async.KtxAsync
import render.Screen
import render.tilesets.Glyph
import ui.input.Mouse
import ui.modals.ContextMenu
import java.lang.Integer.max
import java.lang.Integer.min

object TimeButtons : Panel() {

    private val ffwdDelay = 30L
    private val playDivider = 6

    enum class State { PAUSE, PLAY, FFWD }

    var state: State = State.PAUSE

    class Button(
        val glyph: Glyph,
        val toState: State
    ) {
        var size = 24.0
        var targetSize = 24.0
        val growSpeed = 260.0
        fun onRender(delta: Float) {
            if (size < targetSize) size = kotlin.math.min((size + growSpeed * delta), targetSize)
            else if (size > targetSize && state != toState) size = kotlin.math.max((size - growSpeed * delta), targetSize)
        }
    }

    private var yPadding = 36
    private const val iconSize = 24
    private const val iconSizeHovered = 36
    private const val slop = 18
    private const val wakeSlop = 50
    private var spacing = 48

    private var contraction = height
    private var speed = 12
    private var hovered = -1
    private var mouseInside = false
    private var playCount = 0

    private val buttons = listOf(
        Button(Glyph.BUTTON_PAUSE, State.PAUSE),
        Button(Glyph.BUTTON_PLAY, State.PLAY),
        Button(Glyph.BUTTON_FFWD, State.FFWD)
    )

    init {
        KtxAsync.launch {
            while (true) {
                delay(ffwdDelay)
                if (state == State.FFWD && canAdvance()) {
                    App.player.queue(Wait(1f))
                }
                playCount++
                if (playCount >= playDivider) {
                    if (state == State.PLAY && canAdvance()) {
                        App.player.queue(Wait(1f))
                    }
                    playCount = 0
                }
            }
        }
    }

    private fun canAdvance() = App.player.queuedActions.isEmpty() && Screen.topModal == null &&
            !LeftButtons.mouseInside && !this.mouseInside

    private fun shouldShow(): Boolean {
        if (state != State.PAUSE) return true
        if (!mouseInside) return false
        if (Screen.topModal == null) return true
        if (Screen.topModal is ContextMenu) return true
        return false
    }

    override fun onResize(width: Int, height: Int) {
        super.onResize(width, height)
        this.width = spacing * buttons.size
        this.height = yPadding + iconSizeHovered
        this.x = width - this.width - 200 - xMargin
        this.y = yPadding
        this.contraction = this.height
    }

    override fun onRender(delta: Float) {
        super.onRender(delta)
        buttons.forEach { it.onRender(delta) }
    }

    override fun drawText() { }

    override fun drawBackground() {
        if (App.attractMode) return
        contraction = if (shouldShow()) {
            max(0, contraction - speed)
        } else {
            min(height + 50, contraction + speed)
        }
        var cx = 0
        val cy = 0 - contraction
        buttons.forEachIndexed { n, button ->
            Screen.uiBatch.addPixelQuad(
                cx + this.x - (button.size.toInt()) / 2 + 16,
                cy + this.y - (button.size.toInt()) / 2 + 24,
                cx + this.x + button.size.toInt() / 2 + 16,
                cy + this.y + button.size.toInt() / 2 + 24,
                Screen.uiBatch.getTextureIndex(button.glyph))
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
            hovered = newHover
            buttons.forEachIndexed { n, button ->
                button.targetSize = if (n == hovered) iconSizeHovered.toDouble() else iconSize.toDouble()
            }
        } else {
            mouseInside = false
            buttons.forEach { it.targetSize = iconSize.toDouble() }
        }
    }

    override fun mouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        mouseMovedTo(screenX, screenY)
        val lx = screenX - this.x
        val ly = screenY - this.y
        if (ly < this.height + wakeSlop && lx >=0 && lx < this.width) {
            if (button == Mouse.Button.LEFT) {
                if (hovered >= 0) {
                    changeState(buttons[hovered].toState)
                }
            }
            return true
        }
        return false
    }

    private fun changeState(newState: State) {
        if (newState == state) return

        state = newState
    }

}
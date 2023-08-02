package ui.modals

import actors.actions.Drop
import actors.actions.Get
import actors.actions.Use
import actors.statuses.Status
import audio.Speaker
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import render.batches.QuadBatch
import render.Screen
import render.tilesets.ActorTileSet
import ui.panels.Panel
import render.tilesets.Glyph
import render.tilesets.ThingTileSet
import render.tilesets.UITileSet
import things.Container
import things.Thing
import ui.input.Keydef
import ui.input.Mouse
import ui.panels.Toolbar
import util.groundAtPlayer
import util.plural
import java.lang.Float.min

abstract class Modal(
    width: Int,
    height: Int,
    val title: String? = null,
    val position: Position = Position.LEFT
) : Panel() {
    enum class Position { LEFT, TOP, RIGHT, BOTTOM, CENTER_LOW, CURSOR, SIDECAR }

    var dismissible = true
    protected val launchTimeMs = System.currentTimeMillis()
    protected var animTime = 80f
    protected fun isAnimating() = (Screen.timeMs - launchTimeMs) < animTime
    var dismissOnClickOutside = true

    protected val boxBatch = newBoxBatch()
    protected val textBatch = SpriteBatch()
    protected val thingBatch: QuadBatch? = newThingBatch()
    protected val actorBatch: QuadBatch? = newActorBatch()
    protected open fun newBoxBatch(): QuadBatch = QuadBatch(UITileSet(), maxQuads = 1000)
    protected open fun newThingBatch(): QuadBatch? = QuadBatch(ThingTileSet(), maxQuads = 1000)
    protected open fun newActorBatch(): QuadBatch? = QuadBatch(ActorTileSet(), maxQuads = 1000)

    open fun openSound(): Speaker.SFX? = Speaker.SFX.UIOPEN
    open fun closeSound(): Speaker.SFX? = Speaker.SFX.UICLOSE

    var zoomWhenOpen: Float = 1f
    var sidecar: Modal? = null
    var isSidecar = false
    var isInSidecar = false

    init {
        this.width = width
        this.height = height
    }

    open fun getTitleForDisplay() = title

    open fun myXmargin() = xMargin

    open fun moveToSidecar() { sidecar?.also { isInSidecar = true } }
    open fun returnFromSidecar() { isInSidecar = false }

    open fun receiveRawKeys() = false
    open fun onRawKeyDown(keyCode: Int) { }

    open fun replaceWith(nextModal: Modal) {
        dismiss()
        Screen.addModal(nextModal)
    }

    var bgAlpha = 1f
    var darkenUnder = true
    var darkenUnderSidecar = true

    override fun myTextBatch() = textBatch
    override fun myBoxBatch() = boxBatch
    override fun myThingBatch() = thingBatch
    override fun myActorBatch() = actorBatch

    override fun onResize(width: Int, height: Int) {
        val myXmargin = myXmargin()
        if (position == Position.LEFT || position == Position.SIDECAR) {
            this.x = 40 + myXmargin
        } else if (position == Position.RIGHT) {
            this.x = width - xMargin - 40
        } else {
            this.x = (width - this.width) / 2
        }
        if (position == Position.TOP) {
            this.y = 40 + yMargin
        } else if (position == Position.BOTTOM) {
            this.y = height - yMargin - 40
        } else if (position == Position.CENTER_LOW) {
            this.y = (height - this.height) / 2 + 100
        } else {
            this.y = (height - this.height) / 2
        }
        sidecar?.onResize(width, height)
    }

    override fun drawBackground() {
        if (darkenUnder && Screen.topModal == this) {
            Screen.underModal?.also { under ->
                myBoxBatch().addPixelQuad(under.x, under.y, under.x + under.width, under.y + under.height,
                    myBoxBatch().getTextureIndex(Glyph.BOX_SHADOW), alpha = 0.4f)
                if (darkenUnderSidecar) {
                    under.sidecar?.also {
                        myBoxBatch().addPixelQuad(
                            it.x, it.y, it.x + it.width, it.y + it.height,
                            myBoxBatch().getTextureIndex(Glyph.BOX_SHADOW), alpha = 0.4f
                        )
                    }
                }
            }
        }

        val anim = min(1f, (Screen.timeMs - launchTimeMs) / animTime)
        val xSquish = ((1f - anim) * width / 2f).toInt()
        val ySquish = ((1f - anim) * height / 2f).toInt()
        drawBox(x - xSquish, y + ySquish, width - xSquish * 2, height - ySquish * 2, alpha = bgAlpha)
        if (anim == 1f) {
            drawShade(x + 4, y + 4, width - 8, height - 8, alpha = bgAlpha)
        }

        sidecar?.drawBackground()
    }

    override fun drawText() {
        if (!isAnimating()) {
            getTitleForDisplay()?.also { title ->
                drawTitle(title)
            }
            drawModalText()
        }
        sidecar?.drawText()
    }

    override fun drawEntities() {
        super.drawEntities()
        sidecar?.drawEntities()
    }

    open fun drawModalText() { }

    fun keyDown(key: Keydef) {
        if (dismissible && key == Keydef.CANCEL) {
            dismiss()
            return
        }
        if (isInSidecar) sidecar?.also { it.onKeyDown(key) } ?: run { onKeyDown(key) }
        else onKeyDown(key)
    }
    open fun onKeyDown(key: Keydef) { }

    fun keyUp(key: Keydef) {
        if (isInSidecar) sidecar?.also { it.onKeyUp(key) } ?: run { onKeyUp(key) }
        else onKeyUp(key)
    }
    open fun onKeyUp(key: Keydef) { }

    final override fun mouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        return if (sidecar?.isInBounds(screenX, screenY) == true) {
            sidecar?.onMouseClicked(screenX, screenY, button) ?: false
        } else {
            onMouseClicked(screenX, screenY, button)
        }
    }

    open fun onMouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        if (!isInBounds(screenX, screenY) && sidecar?.isInBounds(screenX, screenY) != true) {
            if (dismissOnClickOutside && dismissible) {
                dismiss()
                return true
            }
        }
        return false
    }

    protected fun isOutside(tx: Int, ty: Int) = tx !in x..x+width-1 || ty !in y..y+height-1

    open fun onMouseScrolled(amount: Float) { }

    fun mouseUp(screenX: Int, screenY: Int, button: Mouse.Button) {
        onMouseUp(screenX, screenY, button)
        sidecar?.onMouseUp(screenX, screenY, button)
    }
    open fun onMouseUp(screenX: Int, screenY: Int, button: Mouse.Button) { }

    final override fun mouseMovedTo(screenX: Int, screenY: Int) {
        onMouseMovedTo(screenX, screenY)
        sidecar?.onMouseMovedTo(screenX, screenY)
    }
    open fun onMouseMovedTo(screenX: Int, screenY: Int) { }

    protected open fun dismiss() {
        dismissed = true
        sidecar?.dismiss()
        onDismiss()
    }
    open fun onDismiss() { }
    open fun onAdd() { }

    protected fun drawSelectionBox(x0: Int, y0: Int, width: Int, height: Int) {
        myBoxBatch().addPixelQuad(this.x + x0 - 6, this.y + y0 - (7 + height / 4),
            this.x + x0 + width + 12, this.y + y0 + height,
            myBoxBatch().getTextureIndex(Glyph.BOX_SHADOW))
    }

    override fun advanceTime(delta: Float) { sidecar?.advanceTime(delta) }

    override fun drawsGrouped() = false
    override fun drawsSeparate() = true
    override fun drawEverything() {
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        Gdx.gl.glEnable(GL20.GL_BLEND)

        clearBoxBatch()
        renderBackground()
        drawBoxBatch()

        clearThingBatch()
        clearActorBatch()
        renderEntities()
        drawThingBatch()
        drawActorBatch()

        beginTextBatch()
        renderText()
        endTextBatch()
    }

    fun clearBoxBatch() {
        boxBatch.clear()
        sidecar?.clearBoxBatch()
    }
    fun drawBoxBatch() {
        boxBatch.draw()
        sidecar?.drawBoxBatch()
    }
    fun beginTextBatch() {
        textBatch.apply {
            projectionMatrix = Screen.textCamera.combined
            enableBlending()
            begin()
        }
        sidecar?.beginTextBatch()
    }
    fun endTextBatch() {
        textBatch.end()
        sidecar?.endTextBatch()
    }
    fun clearThingBatch() {
        thingBatch?.clear()
        sidecar?.clearThingBatch()
    }
    fun drawThingBatch() {
        thingBatch?.draw()
        sidecar?.drawThingBatch()
    }
    fun clearActorBatch() {
        actorBatch?.clear()
        sidecar?.clearActorBatch()
    }
    fun drawActorBatch() {
        actorBatch?.draw()
        sidecar?.drawActorBatch()
    }

    override fun dispose() {
        textBatch.dispose()
        boxBatch.dispose()
        thingBatch?.dispose()
        actorBatch?.dispose()
    }
}

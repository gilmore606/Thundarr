package render

import App
import actors.actions.processes.WalkTo
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20.*
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import ktx.app.KtxScreen
import render.shaders.tileFragShader
import render.shaders.tileVertShader
import render.tilesets.*
import ui.input.Mouse
import ui.modals.ContextMenu
import ui.panels.Panel
import ui.modals.Modal
import util.LightColor
import util.XY
import util.log
import java.lang.Double.max
import java.lang.Double.min
import kotlin.math.abs

const val RENDER_WIDTH = 160
const val RENDER_HEIGHT = 100

object GameScreen : KtxScreen {

    var zoom = 0.4
        set(value) {
            field = value
            updateSurfaceParams()
        }
    private var zoomTarget = 0.4
    private val zoomLevels = listOf(0.15, 0.17, 0.2, 0.25, 0.3, 0.4, 0.5, 0.6, 0.73, 0.85, 1.0, 1.5)
    private var zoomIndex = 3.0

    var width = 0
    var height = 0
    private var aspectRatio = 1.0
    private var tileStride: Double = 0.01

    private val terrainTileSet = TerrainTileSet()
    private val terrainBatch = QuadBatch(tileVertShader(), tileFragShader(), terrainTileSet)
    private val overlapBatch = QuadBatch(tileVertShader(), tileFragShader(), terrainTileSet)
    private val mobBatch = QuadBatch(tileVertShader(), tileFragShader(), MobTileSet())
    private val uiBatch = QuadBatch(tileVertShader(), tileFragShader(), UITileSet(), isScrolling = false)
    private val uiWorldBatch = QuadBatch(tileVertShader(), tileFragShader(), UITileSet(), isScrolling = true)
    private val thingBatch = QuadBatch(tileVertShader(), tileFragShader(), ThingTileSet())
    private val textBatch = SpriteBatch()
    private var textCamera = OrthographicCamera(100f, 100f)
    private val lightCache = Array(RENDER_WIDTH * 2 + 1) { Array(RENDER_WIDTH * 2 + 1) { LightColor(1f, 0f, 0f) } }
    private val fullLight = LightColor(1f, 1f, 1f)

    const val fontSize = 16
    const val titleFontSize = 24
    val fontColorDull = Color(0.7f, 0.7f, 0.4f, 0.6f)
    val fontColor = Color(0.9f, 0.9f, 0.7f, 0.8f)
    val fontColorBold = Color(1f, 1f, 1f, 1f)
    val font: BitmapFont = FreeTypeFontGenerator(Gdx.files.internal("src/main/resources/font/alegreyaSans.ttf"))
        .generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = fontSize
            borderWidth = 1.5f
            color = Color(1f, 1f, 0.8f, 0.9f)
            borderColor = Color(0f, 0f, 0f, 0.5f)
        })
    val titleFont: BitmapFont = FreeTypeFontGenerator(Gdx.files.internal("src/main/resources/font/worldOfWater.ttf"))
        .generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = titleFontSize
            borderWidth = 3f
            spaceX = -2
            color = Color(1f, 0.9f, 0.2f, 1f)
            borderColor = Color(0f, 0f, 0f, 0.8f)
        })

    private val panels: MutableList<Panel> = mutableListOf()
    var topModal: Modal? = null

    private var cursorPosition: XY? = null
    private var cursorLine: MutableList<XY> = mutableListOf()

    private val pov
        get() = App.level.pov
    val lastPov = XY(0,0)

    private const val cameraPull = 0.35f
    private const val cameraAccel = 20f

    var scrollX = 0f
    var scrollY = 0f
    var scrollXtarget = 0f
    var scrollYtarget = 0f
    var scrollLatch = false
    var scrollDragging = false
    private val dragOrigin = XY(0, 0)

    override fun show() {
        super.show()

        updateSurfaceParams()
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        if (width == 0 || height == 0) return  // Windows does this on a hide/show, ignore it

        this.width = width
        this.height = height
        textCamera = OrthographicCamera(width.toFloat(), height.toFloat())

        panels.forEach { it.onResize(width, height) }

        updateSurfaceParams()
    }

    override fun render(delta: Float) {
        animateZoom(delta)
        animateCamera(delta)
        App.level.updateForRender()
        drawEverything()

        App.level.director.runQueue(App.level)
    }

    private fun animateZoom(delta: Float) {
        if (zoom < zoomTarget) {
            zoom = min(zoomTarget, zoom + 1.8 * delta * zoom)
        } else if (zoom > zoomTarget) {
            zoom = max(zoomTarget, zoom - 1.8 * delta * zoom)
        }
    }

    private fun animateCamera(delta: Float) {
        if (scrollLatch) return
        val step = 0.1f
        if (pov.x != lastPov.x || pov.y != lastPov.y) {
            scrollX += ((lastPov.x - pov.x) * step / aspectRatio).toFloat()
            scrollY += ((lastPov.y - pov.y) * step).toFloat()
            lastPov.x = pov.x
            lastPov.y = pov.y
        }
        val accX = 1f + abs(scrollX - scrollXtarget) * cameraAccel / zoom.toFloat()
        val accY = 1f + abs(scrollY - scrollYtarget) * cameraAccel / zoom.toFloat()
        scrollX = if (scrollX > scrollXtarget) {
            kotlin.math.max(scrollXtarget,(scrollX - cameraPull * delta * accX * zoom.toFloat() ))
        } else {
            kotlin.math.min(scrollXtarget,(scrollX + cameraPull * delta * accX * zoom.toFloat() ))
        }
        scrollY = if (scrollY > scrollYtarget) {
            kotlin.math.max(scrollYtarget,(scrollY - cameraPull * delta * accY * zoom.toFloat() ))
        } else {
            kotlin.math.min(scrollYtarget,(scrollY + cameraPull * delta * accY * zoom.toFloat() ))
        }
    }

    fun mouseMovedTo(screenX: Int, screenY: Int) {
        topModal?.also { modal ->
            modal.mouseMovedTo(screenX, screenY)
        } ?: run {
            if (scrollDragging) {
                scrollX = pixelToScrollX(dragOrigin.x - screenX)
                scrollY = pixelToScrollY(dragOrigin.y - screenY)
            } else {
                val col = screenXtoTileX(screenX + pixelScrollX())
                val row = screenYtoTileY(screenY + pixelScrollY())
                if (col != cursorPosition?.x || row != cursorPosition?.y) {
                    if (App.level.isSeenAt(col, row)) {
                        if (App.level.isWalkableAt(col, row) && App.player.queuedActions.isEmpty()) {
                            val newCursor = XY(col, row)
                            cursorPosition = newCursor
                            cursorLine = App.level.getPathToPOV(newCursor).toMutableList()
                        } else {
                            clearCursor()
                        }
                    }
                }
            }
        }
    }

    private fun clearCursor() {
        cursorPosition = null
        cursorLine.clear()
    }

    fun mouseScrolled(amount: Float) {
        zoomIndex = max(0.0, min(zoomLevels.lastIndex.toDouble(), zoomIndex - amount.toDouble() * 0.6))
        zoomTarget = zoomLevels[zoomIndex.toInt()]
    }

    fun mouseDown(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        topModal?.also { modal ->
            modal.mouseClicked(screenX + scrollX.toInt(), screenY + scrollY.toInt(), button)
        } ?: run {
            when (button) {
                Mouse.Button.LEFT -> {
                    scrollDragging = true
                    scrollLatch = true
                    dragOrigin.x = screenX + pixelScrollX()
                    dragOrigin.y = screenY + pixelScrollY()
                }
                Mouse.Button.RIGHT -> {
                    val x = screenXtoTileX(screenX)
                    val y = screenYtoTileY(screenY)
                    addModal(ContextMenu(screenX + 4, screenY + 4).apply {
                        App.level.makeContextMenu(x, y, this)
                    })
                }
                else -> { return false }
            }
        }
        return true
    }

    private const val scrollScale = 450.0  // magic from experimentation, should figure out how this is derived, i'm so dumb
    private fun pixelToScrollX(px: Int) = ((px.toDouble() / zoom) / scrollScale / aspectRatio).toFloat()
    private fun pixelToScrollY(py: Int) = ((py.toDouble() / zoom) / scrollScale).toFloat()
    private fun pixelScrollX() = ((scrollX * zoom.toFloat()) * scrollScale * aspectRatio.toFloat()).toInt()
    private fun pixelScrollY() = ((scrollY * zoom.toFloat()) * scrollScale).toInt()

    fun mouseUp(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        when (button) {
            Mouse.Button.LEFT -> {
                scrollDragging = false
                return true
            }
            Mouse.Button.RIGHT -> {

            }
            else -> { return false }
        }
        return false
    }

    fun povMoved() {
        clearCursor()
    }

    fun addPanel(panel: Panel) {
        panels.add(panel)
    }

    fun removePanel(panel: Panel) {
        panels.remove(panel)
    }

    fun addModal(modal: Modal) {
        if (modal !is ContextMenu) clearCursor()
        modal.onResize(this.width, this.height)
        addPanel(modal)
        if (modal.position == Modal.Position.LEFT) {
            this.scrollXtarget = 0f - (modal.width / 1000f / zoom).toFloat()
        }
        topModal = modal
    }

    fun dismissModal(modal: Modal) {
        panels.remove(modal)
        if (topModal == modal) {
            topModal = null
            panels.forEach { panel ->
                if (panel is Modal) {
                    topModal = panel
                }
            }
        }
        if (topModal == null) {
            this.scrollXtarget = 0f
        }
    }

    private fun drawEverything() {

        Gdx.gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        Gdx.gl.glEnable(GL_BLEND)

        overlapBatch.clear()
        terrainBatch.apply {
            clear()
            App.level.forEachCellToRender(tileSet, { tx, ty, vis, glyph, light ->
                val textureIndex = getTextureIndex(glyph, App.level, tx, ty)
                addTileQuad(
                    tx - pov.x, ty - pov.y, tileStride,
                    textureIndex, vis, light, aspectRatio
                )
                val lx = tx - pov.x + RENDER_WIDTH
                val ly = ty - pov.y + RENDER_HEIGHT
                lightCache[lx][ly].r = light.r
                lightCache[lx][ly].g = light.g
                lightCache[lx][ly].b = light.b
            }, { tx, ty, vis, glyph, edge, light ->
                val textureIndex = getTextureIndex(glyph, App.level, tx, ty)
                overlapBatch.addOverlapQuad(
                    tx - pov.x - edge.x, ty - pov.y - edge.y, tileStride, edge,
                    textureIndex, vis, light, aspectRatio
                )
            })
            draw()
        }

        overlapBatch.apply {
            draw()
        }

        thingBatch.apply {
            clear()
            App.level.forEachThingToRender { tx, ty, vis, glyph ->
                val lx = tx - pov.x + RENDER_WIDTH
                val ly = ty - pov.y + RENDER_HEIGHT
                addTileQuad(
                    tx - pov.x, ty - pov.y, tileStride,
                    getTextureIndex(glyph, null, tx, ty), vis, lightCache[lx][ly], aspectRatio)
            }
            draw()
        }

        mobBatch.apply {
            clear()
            App.level.forEachActorToRender { tx, ty, glyph ->
                val lx = tx - pov.x + RENDER_WIDTH
                val ly = ty - pov.y + RENDER_HEIGHT
                addTileQuad(
                    tx - pov.x, ty - pov.y, tileStride,
                    getTextureIndex(glyph), 1f, lightCache[lx][ly], aspectRatio)
            }
            draw()
        }

        uiWorldBatch.apply {
            clear()
            cursorPosition?.also { cursorPosition ->
                addTileQuad(cursorPosition.x - pov.x, cursorPosition.y - pov.y, tileStride,
                    getTextureIndex(Glyph.CURSOR), 1f, fullLight, aspectRatio)
                cursorLine.forEach { xy ->
                    addTileQuad(xy.x - pov.x, xy.y - pov.y, tileStride,
                        getTextureIndex(Glyph.CURSOR), 1f, fullLight, aspectRatio)
                }
            }
            draw()
        }

        uiBatch.apply {
            clear()
            panels.forEach { panel ->
                panel.renderBackground(this)
            }
            draw()
        }

        textBatch.apply {
            projectionMatrix = textCamera.combined
            enableBlending()
            begin()
            panels.forEach { panel ->
                panel.renderText(this)
            }
            end()
        }
    }

    private fun screenXtoTileX(screenX: Int) = (((((screenX.toFloat() / width) * 2.0 - 1.0) * aspectRatio) + tileStride * 0.5) / tileStride + pov.x).toInt()

    private fun screenYtoTileY(screenY: Int) = (((screenY.toFloat() / height) * 2.0 - 1.0 + tileStride * 0.5) / tileStride + pov.y).toInt()

    private fun updateSurfaceParams() {
        aspectRatio = width.toDouble() / height.toDouble()
        tileStride = 1.0 / (height.coerceAtLeast(400).toDouble() * 0.01f) * zoom
        log.debug("Surface params updated to $width x $height (ratio $aspectRatio tileStride $tileStride)")
    }

    override fun dispose() {
        terrainBatch.dispose()
        overlapBatch.dispose()
        mobBatch.dispose()
        thingBatch.dispose()
        uiBatch.dispose()
        uiWorldBatch.dispose()
    }
}

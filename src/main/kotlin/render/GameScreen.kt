package render

import App
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
import util.*
import java.lang.Double.max
import java.lang.Double.min
import kotlin.math.abs

object GameScreen : KtxScreen {

    var RENDER_WIDTH = 160
    var RENDER_HEIGHT = 100
    var zoom = 0.4
        set(value) {
            field = value
            updateSurfaceParams()
        }
    private var zoomTarget = 0.4
    private val zoomLevels = listOf(0.25, 0.4, 0.5, 0.6, 0.85)
    var zoomIndex = 2.0

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
    private var scrollTargetX = 0f
    private var scrollTargetY = 0f
    var scrollLatch = false
    var scrollDragging = false
    private val dragOrigin = XY(0, 0)

    var drawTime: Int = 0
    private val lastDrawTimes = arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
    private var drawTimeIndex = 0

    private val renderTile: (Int, Int, Float, Glyph, LightColor)->Unit = { tx, ty, vis, glyph, light ->
        val textureIndex = terrainBatch.getTextureIndex(glyph, App.level, tx, ty)
        terrainBatch.addTileQuad(
            tx - pov.x, ty - pov.y, tileStride,
            textureIndex, vis, light, aspectRatio
        )
        val lx = tx - pov.x + RENDER_WIDTH
        val ly = ty - pov.y + RENDER_HEIGHT
        lightCache[lx][ly].r = light.r
        lightCache[lx][ly].g = light.g
        lightCache[lx][ly].b = light.b
    }

    private val renderOverlap: (Int, Int, Float, Glyph, XY, LightColor)->Unit = { tx, ty, vis, glyph, edge, light ->
        val textureIndex = terrainBatch.getTextureIndex(glyph, App.level, tx, ty)
        overlapBatch.addOverlapQuad(
            tx - pov.x - edge.x, ty - pov.y - edge.y, tileStride, edge,
            textureIndex, vis, light, aspectRatio
        )
    }

    private val renderOcclude: (Int, Int, XY)->Unit = { tx, ty, edge ->
        val textureIndex = terrainBatch.getTextureIndex(
            if (edge == NORTH || edge == SOUTH) Glyph.OCCLUSION_SHADOWS_H else Glyph.OCCLUSION_SHADOWS_V
        )
        overlapBatch.addOccludeQuad(
            tx - pov.x, ty - pov.y, tileStride, edge,
            textureIndex, 1f, fullLight, aspectRatio
        )
    }

    private val renderSurf: (Int, Int, Float, LightColor, XY)->Unit = { tx, ty, vis, light, edge ->
        val textureIndex = terrainBatch.getTextureIndex(
            if (edge == NORTH || edge == SOUTH) Glyph.SURF_H else Glyph.SURF_V
        )
        overlapBatch.addOccludeQuad(
            tx - pov.x, ty - pov.y, tileStride, edge,
            textureIndex, vis, light, aspectRatio
        )
    }

    private val renderThing: (Int, Int, Float, Glyph)->Unit = { tx, ty, vis, glyph ->
        val lx = tx - pov.x + RENDER_WIDTH
        val ly = ty - pov.y + RENDER_HEIGHT
        thingBatch.addTileQuad(
            tx - pov.x, ty - pov.y, tileStride,
            thingBatch.getTextureIndex(glyph, null, tx, ty), vis, lightCache[lx][ly], aspectRatio)
    }

    private val renderActor: (Int, Int, Glyph)->Unit = { tx, ty, glyph ->
        val lx = tx - pov.x + RENDER_WIDTH
        val ly = ty - pov.y + RENDER_HEIGHT
        mobBatch.addTileQuad(
            tx - pov.x, ty - pov.y, tileStride,
            mobBatch.getTextureIndex(glyph), 1f, lightCache[lx][ly], aspectRatio)
    }

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
        panels.forEach { it.onRender(delta) }

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

    fun restoreZoomIndex(index: Double) {
        zoomIndex = index
        zoomTarget = zoomLevels[zoomIndex.toInt()]
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
        val accX = 1f + abs(scrollX - scrollTargetX) * cameraAccel / zoom.toFloat()
        val accY = 1f + abs(scrollY - scrollTargetY) * cameraAccel / zoom.toFloat()
        scrollX = if (scrollX > scrollTargetX) {
            kotlin.math.max(scrollTargetX,(scrollX - cameraPull * delta * accX * zoom.toFloat() ))
        } else {
            kotlin.math.min(scrollTargetX,(scrollX + cameraPull * delta * accX * zoom.toFloat() ))
        }
        scrollY = if (scrollY > scrollTargetY) {
            kotlin.math.max(scrollTargetY,(scrollY - cameraPull * delta * accY * zoom.toFloat() ))
        } else {
            kotlin.math.min(scrollTargetY,(scrollY + cameraPull * delta * accY * zoom.toFloat() ))
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
        panel.onResize(this.width, this.height)
        panels.add(panel)
    }

    fun removePanel(panel: Panel) {
        panels.remove(panel)
    }

    fun addModal(modal: Modal) {
        if (modal !is ContextMenu) clearCursor()
        addPanel(modal)
        if (modal.position == Modal.Position.LEFT) {
            this.scrollTargetX = 0f - (modal.width / 1200f / zoom).toFloat()
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
            this.scrollTargetX = 0f
        }
    }

    private fun drawEverything() {

        val startTime = System.currentTimeMillis()
        Gdx.gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        Gdx.gl.glEnable(GL_BLEND)

        overlapBatch.clear()
        terrainBatch.apply {
            clear()
            App.level.forEachCellToRender(tileSet,
                doTile = renderTile,
                doOverlap = renderOverlap,
                doOcclude = renderOcclude,
                doSurf = renderSurf
            )
            draw()
        }

        overlapBatch.apply {
            draw()
        }

        thingBatch.apply {
            clear()
            App.level.forEachThingToRender(renderThing)
            draw()
        }

        mobBatch.apply {
            clear()
            App.level.forEachActorToRender(renderActor)
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

        lastDrawTimes[drawTimeIndex] = (System.currentTimeMillis() - startTime).toInt()
        drawTimeIndex = if (drawTimeIndex == 9) 0 else drawTimeIndex + 1
        drawTime = 0
        lastDrawTimes.forEach { drawTime += it }
        drawTime /= 10
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

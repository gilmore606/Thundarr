package render

import App
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20.*
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import ktx.app.KtxScreen
import render.shaders.tileFragShader
import render.shaders.tileVertShader
import render.tilesets.*
import ui.input.Keyboard
import ui.input.Mouse
import ui.modals.ContextMenu
import ui.panels.Panel
import ui.modals.Modal
import ui.panels.ActorPanel
import util.*
import world.LevelKeeper
import world.WorldLevel
import java.lang.Double.max
import java.lang.Double.min
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.sign

object Screen : KtxScreen {

    private const val WORLD_ZOOM = 0.85
    private const val ZOOM_SPEED = 4.0
    private const val CAMERA_SPRING = 0.3
    var RENDER_WIDTH = 160
    var RENDER_HEIGHT = 100
    var zoom = 0.5
        set(value) {
            field = value
            updateSurfaceParams()
        }
    private var zoomTarget = 0.75
    private val zoomLevels = listOf(0.42, 0.5, 0.6, 0.75, 0.85, 1.0)
    var zoomIndex = 3.0

    var width = 0
    var height = 0
    var FULLSCREEN = false
    private var savedWindowSize  = XY(0, 0)
    fun savedWindowSize() = if (savedWindowSize.x == 0) XY(width, height) else savedWindowSize

    private var aspectRatio = 1.0
    private var tileStride: Double = 0.01

    private val terrainTileSet = TerrainTileSet()
    private val thingTileSet = ThingTileSet()
    private val actorTileSet = ActorTileSet()
    private val uiTileSet = UITileSet()
    private val tileSets = listOf(terrainTileSet, thingTileSet, actorTileSet, uiTileSet)
    private val terrainBatch = QuadBatch(tileVertShader(), tileFragShader(), terrainTileSet)
    private val overlapBatch = QuadBatch(tileVertShader(), tileFragShader(), terrainTileSet)
    val actorBatch = QuadBatch(tileVertShader(), tileFragShader(), actorTileSet)
    val thingBatch = QuadBatch(tileVertShader(), tileFragShader(), thingTileSet)
    private val uiWorldBatch = QuadBatch(tileVertShader(), tileFragShader(), uiTileSet)
    val uiBatch = QuadBatch(tileVertShader(), tileFragShader(), uiTileSet)
    val uiThingBatch = QuadBatch(tileVertShader(), tileFragShader(), thingTileSet)
    val uiActorBatch = QuadBatch(tileVertShader(), tileFragShader(), actorTileSet)
    private val worldBatches = listOf(terrainBatch, actorBatch, thingBatch, uiWorldBatch)
    private val allBatches = listOf(terrainBatch, overlapBatch, thingBatch, actorBatch, uiWorldBatch, uiBatch, uiThingBatch, uiActorBatch)
    private val textBatch = SpriteBatch()
    private var textCamera = OrthographicCamera(100f, 100f)

    private val lightCache = Array(RENDER_WIDTH * 2 + 1) { Array(RENDER_WIDTH * 2 + 1) { LightColor(1f, 0f, 0f) } }
    val fullLight = LightColor(1f, 1f, 1f)
    val fullDark = LightColor(0f, 0f, 0f)

    const val fontSize = 16
    const val fontSizeSmall = 14
    const val titleFontSize = 24
    const val subTitleFontSize = 20
    val fontColorDull = Color(0.75f, 0.75f, 0.45f, 0.8f)
    val fontColor = Color(0.9f, 0.9f, 0.7f, 0.8f)
    val fontColorBold = Color(1f, 1f, 1f, 1f)
    val font: BitmapFont = FreeTypeFontGenerator(Gdx.files.internal("src/main/resources/font/amstrad.ttf"))
        .generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = fontSize
            borderWidth = 2.3f
            spaceX = -1
            kerning = true
            genMipMaps = true
            minFilter = TextureFilter.MipMapNearestNearest
            magFilter = TextureFilter.MipMapNearestNearest
            color = Color(1f, 1f, 0.8f, 0.9f)
            borderColor = Color(0f, 0f, 0f, 0.5f)
        })
    val smallFont: BitmapFont = FreeTypeFontGenerator(Gdx.files.internal("src/main/resources/font/amstrad.ttf"))
        .generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = fontSizeSmall
            borderWidth = 2f
            spaceX = -1
            kerning = true
            genMipMaps = true
            minFilter = TextureFilter.MipMapNearestNearest
            magFilter = TextureFilter.MipMapNearestNearest
            color = Color(1f, 1f, 0.8f, 0.9f)
            borderColor = Color(0f, 0f, 0f, 0.5f)
        })
    val titleFont: BitmapFont = FreeTypeFontGenerator(Gdx.files.internal("src/main/resources/font/worldOfWater.ttf"))
        .generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = titleFontSize
            borderWidth = 3f
            spaceX = -2
            kerning = true
            genMipMaps = true
            minFilter = TextureFilter.MipMapNearestNearest
            magFilter = TextureFilter.MipMapNearestNearest
            color = Color(1f, 0.9f, 0.2f, 1f)
            borderColor = Color(0f, 0f, 0f, 0.8f)
        })
    val subTitleFont: BitmapFont = FreeTypeFontGenerator(Gdx.files.internal("src/main/resources/font/worldOfWater.ttf"))
        .generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = subTitleFontSize
            borderWidth = 2f
            spaceX = -2
            kerning = true
            genMipMaps = true
            minFilter = TextureFilter.MipMapNearestNearest
            magFilter = TextureFilter.MipMapNearestNearest
            color = Color(1f, 0.9f, 0.2f, 1f)
            borderColor = Color(0f, 0f, 0f, 0.8f)
        })

    val panels: ArrayList<Panel> = ArrayList()
    var topModal: Modal? = null

    var cursorPosition: XY? = null
    private var cursorLine: MutableList<XY> = mutableListOf()

    private val pov
        get() = App.level.pov
    private val lastPov = XY(0,0)

    private var cameraPovX = 0.0
    private var cameraPovY = 0.0
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
            tx, ty, textureIndex, vis, light
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
            tx - edge.x, ty - edge.y, edge,
            textureIndex, vis, light
        )
    }

    private val renderOcclude: (Int, Int, XY)->Unit = { tx, ty, edge ->
        val textureIndex = terrainBatch.getTextureIndex(
            if (edge == NORTH || edge == SOUTH) Glyph.OCCLUSION_SHADOWS_H else Glyph.OCCLUSION_SHADOWS_V
        )
        overlapBatch.addOccludeQuad(
            tx, ty, edge, textureIndex, 1f, fullLight
        )
    }

    private val renderSurf: (Int, Int, Float, LightColor, XY)->Unit = { tx, ty, vis, light, edge ->
        val textureIndex = terrainBatch.getTextureIndex(
            if (edge == NORTH || edge == SOUTH) Glyph.SURF_H else Glyph.SURF_V
        )
        overlapBatch.addOccludeQuad(
            tx, ty, edge, textureIndex, vis, light
        )
    }

    private val renderThing: (Int, Int, Float, Glyph)->Unit = { tx, ty, vis, glyph ->
        val lx = tx - pov.x + RENDER_WIDTH
        val ly = ty - pov.y + RENDER_HEIGHT
        thingBatch.addTileQuad(
            tx, ty,
            thingBatch.getTextureIndex(glyph, App.level, tx, ty), vis, lightCache[lx][ly])
    }

    private val renderActor: (Int, Int, Glyph)->Unit = { tx, ty, glyph ->
        val lx = tx - pov.x + RENDER_WIDTH
        val ly = ty - pov.y + RENDER_HEIGHT
        val light = if (lx < lightCache.size && ly < lightCache[0].size && lx >= 0 && ly >= 0) lightCache[lx][ly] else fullDark
        actorBatch.addTileQuad(
            tx, ty,
            actorBatch.getTextureIndex(glyph, App.level, tx, ty), 1f, light)
    }

    private val renderSpark: (Int, Int, Glyph, LightColor, Float, Float, Float, Float)->Unit = { tx, ty, glyph, light, offsetX, offsetY, scale, alpha ->
        worldBatches.firstOrNull { it.tileSet.hasGlyph(glyph) }?.also { batch ->
            batch.addTileQuad(
                tx, ty,
                batch.getTextureIndex(glyph, App.level, tx, ty), 1f, light,
                offsetX, offsetY, scale, alpha
            )
        }
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

    fun toggleFullscreen() = toggleFullscreen(!FULLSCREEN)
    fun toggleFullscreen(newValue: Boolean) {
        if (newValue && !FULLSCREEN) {
            savedWindowSize = XY(width, height)
            FULLSCREEN = true
            Gdx.graphics.setFullscreenMode(Gdx.graphics.displayMode)
        } else if (!newValue && FULLSCREEN) {
            FULLSCREEN = false
            Gdx.graphics.setWindowedMode(savedWindowSize.x, savedWindowSize.y)
        }
    }

    override fun render(delta: Float) {
        animateCamera(delta)
        App.level.onRender(delta)
        var dismissedPanel: Panel? = null
        var topModalFound: Modal? = null
        panels.forEach {
            it.onRender(delta)
            if (it.dismissed) dismissedPanel = it
            else if (it is Modal) topModalFound = it
        }
        dismissedPanel?.also {
            panels.remove(it)
            topModal = topModalFound
            if (topModal == null) {
                this@Screen.scrollTargetX = 0f
            }
        }

        drawEverything(delta)

        LevelKeeper.runActorQueues()
    }

    fun restoreZoomIndex(index: Double) {
        zoomIndex = index
        zoomTarget = zoomLevels[zoomIndex.toInt()]
    }

    private fun animateCamera(delta: Float) {
        val diff = min(3.0, max(0.04, abs(zoom - zoomTarget)))
        if (zoom < zoomTarget) {
            zoom = min(zoomTarget, zoom + diff * delta * ZOOM_SPEED)
        } else if (zoom > zoomTarget) {
            zoom = max(zoomTarget, zoom - diff * delta * ZOOM_SPEED)
        }
        if (scrollLatch) return

        val xdist = (pov.x - cameraPovX)
        val ydist = (pov.y - cameraPovY)
        val xinc = max(0.5, min(1000.0, (xdist.absoluteValue / CAMERA_SPRING))) * xdist.sign
        val yinc = max(0.5, min(1000.0, (ydist.absoluteValue / CAMERA_SPRING))) * ydist.sign
        if (cameraPovX < pov.x) {
            cameraPovX = min(pov.x.toDouble(), cameraPovX + xinc * delta)
        } else if (cameraPovX > pov.x) {
            cameraPovX = max(pov.x.toDouble(),  cameraPovX + xinc * delta)
        }
        if (cameraPovY < pov.y) {
            cameraPovY = min(pov.y.toDouble(), cameraPovY + yinc * delta)
        } else if (cameraPovY > pov.y) {
            cameraPovY = max(pov.y.toDouble(), cameraPovY + yinc * delta)
        }
    }

    fun recenterCamera() {
        lastPov.x = pov.x
        lastPov.y = pov.y
        scrollX = 0f
        scrollY = 0f
    }

    fun mouseMovedTo(screenX: Int, screenY: Int) {
        topModal?.also { modal ->
            modal.mouseMovedTo(screenX, screenY)
        } ?: run {
            panels.forEach {
                it.mouseMovedTo(screenX, screenY)
            }
            if (scrollDragging) {
                scrollX = pixelToScrollX(dragOrigin.x - screenX)
                scrollY = pixelToScrollY(dragOrigin.y - screenY)
            } else {
                val col = screenXtoTileX(screenX + pixelScrollX())
                val row = screenYtoTileY(screenY + pixelScrollY())
                if (col != cursorPosition?.x || row != cursorPosition?.y) {
                    if (App.level.isSeenAt(col, row)) {
                        if (App.player.queuedActions.isEmpty()) {
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

    fun clearCursor() {
        cursorPosition = null
        cursorLine.clear()
        Keyboard.CURSOR_MODE = false
    }

    fun moveCursor(dir: XY) {
        if (cursorPosition == null) cursorPosition = XY(App.player.xy.x, App.player.xy.y)
        cursorPosition?.also {
            setCursorPosition(it.x + dir.x, it.y + dir.y)
        }
    }

    fun setCursorPosition(x: Int, y: Int) {
        if (cursorPosition == null) cursorPosition = XY(App.player.xy.x, App.player.xy.y)
        cursorPosition?.also { it.x = x ; it.y = y }
    }

    fun cursorNextActor(dir: Int) {
        cursorPosition?.also { cursor ->
            App.level.actorAt(cursor.x, cursor.y)?.also { actor ->
                ActorPanel.actorAfter(actor, dir)?.also { nextActor ->
                    cursor.x = nextActor.xy.x
                    cursor.y = nextActor.xy.y
                }
            } ?: run {
                ActorPanel.firstActor()?.also { actor ->
                    cursor.x = actor.xy.x
                    cursor.y = actor.xy.y
                }
            }
        } ?: run {
            ActorPanel.firstActor()?.also { actor ->
                cursorPosition = XY(actor.xy.x, actor.xy.y)
            }
        }
    }

    fun mouseScrolled(amount: Float) {
        zoomIndex = max(0.0, min(zoomLevels.lastIndex.toDouble(), zoomIndex - amount.toDouble() * 0.7))
        zoomTarget = zoomLevels[zoomIndex.toInt()] * (if (App.level is WorldLevel) WORLD_ZOOM else 1.0)
    }

    fun mouseDown(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        topModal?.also { modal ->
            modal.mouseClicked(screenX + scrollX.toInt(), screenY + scrollY.toInt(), button)
        } ?: run {
            mutableListOf<Panel>().apply { addAll(panels) }.forEach {
                if (screenX >= it.x && screenX <= it.x + it.width && screenY >= it.y && screenY <= it.y + it.height) {
                    if (it.mouseClicked(screenX, screenY, button)) return true
                }
            }
            when (button) {
                Mouse.Button.LEFT -> {
                    scrollDragging = true
                    scrollLatch = true
                    dragOrigin.x = screenX + pixelScrollX()
                    dragOrigin.y = screenY + pixelScrollY()
                    return true
                }
                Mouse.Button.RIGHT -> {
                    val x = screenXtoTileX(screenX)
                    val y = screenYtoTileY(screenY)
                    setCursorPosition(x,y)
                    rightClickCursorTile()
                    return true
                }
                else -> { return false }
            }
        }
        return false
    }

    fun rightClickCursorTile() {
        if (cursorPosition == null) cursorPosition = XY(App.player.xy.x, App.player.xy.y)
        val offset = (8.0 * zoom).toInt()
        val menu = ContextMenu(tileXtoScreenX(cursorPosition!!.x) - offset, tileYtoScreenY(cursorPosition!!.y) - offset).apply {
            App.level.makeContextMenu(cursorPosition!!.x, cursorPosition!!.y, this)
        }
        if (menu.options.isNotEmpty()) addModal(menu)
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
            this.scrollTargetX = 0f - (modal.width / 2400f / zoom).toFloat()
        }
        topModal = modal
    }

    private fun drawEverything(delta: Float) {

        val startTime = System.currentTimeMillis()
        Gdx.gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        Gdx.gl.glEnable(GL_BLEND)

        allBatches.forEach { it.clear() }

        App.level.forEachCellToRender(
            terrainBatch.tileSet,
            doTile = renderTile,
            doOverlap = renderOverlap,
            doOcclude = renderOcclude,
            doSurf = renderSurf,
            delta = delta
        )
        if (terrainBatch.vertexCount < 1) { log.debug("Davey!  terrainBatch had 0 vertices") }

        App.level.forEachThingToRender(renderThing)
        App.level.forEachActorToRender(renderActor)
        App.level.forEachSparkToRender(renderSpark)

        uiWorldBatch.apply {
            cursorPosition?.also { cursorPosition ->
                addTileQuad(cursorPosition.x, cursorPosition.y,
                    getTextureIndex(Glyph.CURSOR), 1f, fullLight)
                cursorLine.forEach { xy ->
                    addTileQuad(xy.x, xy.y,
                        getTextureIndex(Glyph.CURSOR), 1f, fullLight)
                }
            }
        }

        panels.forEach { panel ->
            panel.renderBackground(uiBatch)
            panel.renderEntities()
        }

        allBatches.forEach { it.draw() }

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

    fun tileXtoGlx(col: Double) = ((col - (cameraPovX) - 0.5) * tileStride) / aspectRatio
    fun tileYtoGly(row: Double) = ((row - (cameraPovY) - 0.5) * tileStride)

    private fun screenXtoTileX(screenX: Int) = (((((screenX.toFloat() / width) * 2.0 - 1.0) * aspectRatio) + tileStride * 0.5) / tileStride + pov.x).toInt()
    private fun screenYtoTileY(screenY: Int) = (((screenY.toFloat() / height) * 2.0 - 1.0 + tileStride * 0.5) / tileStride + pov.y).toInt()
    private fun tileXtoScreenX(tileX: Int) = ((width / 2.0) + (tileX - pov.x + 0.5) / aspectRatio * 0.5 * tileStride * width.toDouble()).toInt()
    private fun tileYtoScreenY(tileY: Int) = ((height / 2.0) + (tileY - pov.y + 0.5) * 0.5 * tileStride * height.toDouble()).toInt()

    private fun updateSurfaceParams() {
        aspectRatio = width.toDouble() / height.toDouble()
        tileStride = 1.0 / (height.coerceAtLeast(400).toDouble() * 0.01) * zoom
    }

    override fun dispose() {
        allBatches.forEach { it.dispose() }
    }
}

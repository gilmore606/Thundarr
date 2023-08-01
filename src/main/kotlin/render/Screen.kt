package render

import App
import actors.actors.Actor
import audio.Speaker
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20.*
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.math.MathUtils.sin
import ktx.app.KtxScreen
import render.batches.*
import render.tilesets.*
import things.Thing
import ui.input.Keyboard
import ui.input.Mouse
import ui.modals.ContextMenu
import ui.modals.Modal
import ui.modals.ToolbarAddModal
import ui.panels.*
import util.*
import world.persist.LevelKeeper
import world.stains.Fire
import world.stains.Stain
import world.terrains.Terrain
import world.terrains.Water
import java.lang.Double.max
import java.lang.Double.min
import kotlin.math.abs

object Screen : KtxScreen {
    const val GLSL_VERSION = "120"
    var brightness = 0f
    var brightnessTarget = 1f
    val textureFilter = TextureFilter.MipMapLinearLinear
    var worldZoom = 1.3
    var cameraMenuShift = 0.8
    var uiHue = 0.0
    var showSeenAreas = true
    var showRadar = true
    private const val ZOOM_SPEED = 4.0
    private const val MAX_RENDER_WIDTH = 150
    private const val MAX_RENDER_HEIGHT = 150
    var zoom = 0.5
        set(value) {
            field = value
            updateSurfaceParams()
        }
    private var zoomTarget = 0.73
    private val zoomLevels = listOf(0.38, 0.5, 0.62, 0.73, 0.83, 1.0, 1.15, 1.3)
    var zoomIndex = 3.0

    var width = 0
    var height = 0
    var fullscreen = false
    private var savedWindowSize  = XY(0, 0)
    fun savedWindowSize() = if (savedWindowSize.x == 0) XY(width, height) else savedWindowSize

    var aspectRatio = 1.0
    private var tileStride: Double = 0.01
    var renderTilesWide = 120
    var renderTilesHigh = 50

    val terrainTileSet = TerrainTileSet()
    val thingTileSet = ThingTileSet()
    val actorTileSet = ActorTileSet()
    val uiTileSet = UITileSet()
    val portraitTileSet = PortraitTileSet()
    val mapTileSet = MapTileSet()
    val terrainBatch = QuadBatch(terrainTileSet)
    val thingBatch = QuadBatch(thingTileSet)
    val actorBatch = ActorBatch(actorTileSet)
    val gearBatch = QuadBatch(thingTileSet)
    val fireBatch = FireBatch()
    val uiWorldBatch = QuadBatch(uiTileSet)
    val uiBatch = QuadBatch(uiTileSet)
    val uiThingBatch = QuadBatch(thingTileSet)
    val uiActorBatch = QuadBatch(actorTileSet)
    val uiTerrainBatch = QuadBatch(terrainTileSet)
    val cloudBatch = CloudBatch()
    val rainBatch = RainBatch()
    private val worldBatches = listOf(terrainBatch, thingBatch,  actorBatch, gearBatch, uiWorldBatch)
    private val allBatches = listOf(terrainBatch, thingBatch, actorBatch, gearBatch, fireBatch,
        uiWorldBatch, uiBatch, uiThingBatch, uiActorBatch, uiTerrainBatch, cloudBatch, rainBatch)
    val textBatch = SpriteBatch()
    var textCamera = OrthographicCamera(100f, 100f)

    private val lightCache = Array(MAX_RENDER_WIDTH * 2 + 1) { Array(MAX_RENDER_HEIGHT * 2 + 1) { LightColor(1f, 0f, 0f) } }
    val fullLight = LightColor(1f, 1f, 1f)
    val halfLight = LightColor(0.3f, 0.3f, 0.3f)
    val fullDark = LightColor(0f, 0f, 0f)

    private const val fontSize = 16
    private const val fontSizeSmall = 14
    private const val titleFontSize = 24
    private const val subTitleFontSize = 20
    val fontColor = Color(0.95f, 0.92f, 0.8f, 1f)
    val fontColorDull = Color(0.73f, 0.73f, 0.6f, 1f)
    val fontColorBold = Color(1f, 1f, 1f, 1f)
    val fontColorRed = Color(1f, 0.1f, 0.1f, 1f)
    val fontColorGreen = Color(0.1f, 1f, 0.1f, 1f)
    val fontColorBlue = Color(0f, 0.7f, 1f, 1f)
    val font: BitmapFont = FreeTypeFontGenerator(Gdx.files.internal("res/font/amstrad.ttf"))
        .generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = fontSize
            borderWidth = 1.8f
            spaceX = 0
            kerning = true
            genMipMaps = true
            minFilter = TextureFilter.MipMapLinearLinear
            magFilter = TextureFilter.MipMapLinearLinear
            color = Color(1f, 1f, 0.8f, 0.9f)
            borderColor = Color(0f, 0f, 0f, 1f)
        })
    val smallFont: BitmapFont = FreeTypeFontGenerator(Gdx.files.internal("res/font/amstrad.ttf"))
        .generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = fontSizeSmall
            borderWidth = 0.7f
            spaceX = 0
            kerning = true
            genMipMaps = true
            minFilter = TextureFilter.MipMapLinearLinear
            magFilter = TextureFilter.MipMapLinearLinear
            color = Color(1f, 1f, 0.8f, 0.9f)
            borderColor = Color(0f, 0f, 0f, 1f)
        })
    val titleFont: BitmapFont = FreeTypeFontGenerator(Gdx.files.internal("res/font/worldOfWater.ttf"))
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
    val subTitleFont: BitmapFont = FreeTypeFontGenerator(Gdx.files.internal("res/font/worldOfWater.ttf"))
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
    var underModal: Modal? = null

    var cursorPosition: XY? = null

    private val pov
        get() = App.level.pov
    private val lastPov = XY(0,0)

    var cameraTargetOffset = XYd(0.0, 0.0)  // add to pov to get intended camera target pos
    var cameraPov = XYd(0.0, 0.0)  // actual camera pos
    var cameraVec = XYd(0.0, 0.0)  // current camera movement vector
    var cameraSpeed = 4.0
    var scrollLatch = false
    var scrollDragging = false
    val dragPixels = XYd(0.0, 0.0)
    private val lastDrag = XY(0, 0)
    private val dragInertia = XY(0, 0)
    private val dragInertiaLast = XY(0, 0)
    private var dragInertiaDelta = 0f
    private const val dragBraking = 0.95f
    private const val dragStartWeight = 0.6f

    private var lastDrawTime = 0L
    private var lastActionTime = 0L
    private const val minActionInterval = 80L

    var sinBob = 0f
    var timeMs = System.currentTimeMillis()

    private val renderTile: (Int, Int, Float, Glyph, LightColor)->Unit = { tx, ty, vis, glyph, light ->
        val textureIndex = terrainBatch.getTerrainTextureIndex(glyph, App.level, tx, ty)
        terrainBatch.addTileQuad(
            tx, ty, textureIndex, vis, light,
            waves = if (glyph == Glyph.SHALLOW_WATER || glyph == Glyph.DEEP_WATER) 1f else 0f
        )
        val lx = tx - pov.x + renderTilesWide
        val ly = ty - pov.y + renderTilesHigh
        if (lx in 0..lightCache.lastIndex && ly in 0..lightCache[0].lastIndex) {
            lightCache[lx][ly].r = light.r
            lightCache[lx][ly].g = light.g
            lightCache[lx][ly].b = light.b
        }
    }

    private val renderQuad: (Double, Double, Double, Double, Float, Float, Float, Float, Float, Glyph, LightColor, Boolean)->Unit =
        { x0, y0, x1, y1, tx0, ty0, tx1, ty1, vis, glyph, light, rotate ->
            val textureIndex = terrainBatch.getTextureIndex(glyph, App.level, x0.toInt(), y0.toInt())
            terrainBatch.addPartialQuad(x0, y0, x1, y1, textureIndex, vis, light, tx0, ty0, tx1, ty1, 1f, rotate = rotate)
    }

    private val renderWeather: (Int, Int, Float, Float, Float, Boolean)->Unit = { tx, ty, cloudAlpha, rainAlpha, snowAlpha, fadeUp ->
        cloudBatch.addTileQuad(tx, ty, cloudAlpha, fadeUp)
        rainBatch.addTileQuad(tx, ty, rainAlpha, snowAlpha, fadeUp)
    }

    private val renderStain: (Int, Int, Stain, LightColor)->Unit = { tx, ty, stain, light ->
        thingBatch.addTileQuad(
            tx, ty,
            thingBatch.getTextureIndex(stain.glyph(), App.level, tx, ty), 1f, light,
            offsetX = stain.offsetX, offsetY = stain.offsetY, scale = stain.scale, alpha = stain.alpha, hue = stain.hue()
        )
    }

    private val renderThing: (Int, Int, Thing, Float)->Unit = { tx, ty, thing, vis ->
        val lx = tx - pov.x + renderTilesWide
        val ly = ty - pov.y + renderTilesHigh
        val cachedLight = if (lx in 0..lightCache.lastIndex && ly in 0..lightCache[0].lastIndex) lightCache[lx][ly] else fullLight
        thingBatch.addTileQuad(
            tx, ty, thingBatch.getTextureIndex(thing.glyph(), App.level, tx, ty),
            vis, cachedLight, hue = thing.hue())
        if (vis == 1f) {
            thing.drawExtraGlyphs { glyph, hue, offX, offY ->
                thingBatch.addTileQuad(tx, ty, thingBatch.getTextureIndex(glyph, App.level, tx, ty),
                    vis, cachedLight, hue = hue, offsetX = offX, offsetY = offY)
            }
        }
    }

    private val renderFire: (Int, Int, Float, Float, Float, Float)->Unit = { tx, ty, offset, offx, offy, size ->
        fireBatch.addTileQuad(tx, ty, offset, offX = offx, offY = offy, size = size)
    }

    private val renderActor: (Int, Int, Actor, Float)->Unit = { tx, ty, actor, vis ->
        val lx = tx - pov.x + renderTilesWide
        val ly = ty - pov.y + renderTilesHigh
        val light = if(lx in 0..lightCache.lastIndex && ly in 0..lightCache[0].lastIndex) lightCache[lx][ly] else fullDark

        val shadow = if (Terrain.get(actor.level?.getTerrain(actor.xy.x, actor.xy.y) ?: Terrain.Type.BLANK) is Water)
            Glyph.MOB_WATER_SHADOW else Glyph.MOB_SHADOW
        if (vis == 1f && shadow == Glyph.MOB_SHADOW) {
            actor.renderShadow { x0, y0, x1, y1 ->
                actorBatch.addPartialActorQuad(
                    x0, y0, x1, y1, actorBatch.getTextureIndex(shadow),
                    1f, fullLight, 0f, 0f, 1f, 1f, 1f, hue = 0f
                )
            }
        }
        val glyph = actor.glyph()
        actorBatch.addActorQuad(
            tx, ty,
            actorBatch.getTextureIndex(glyph, App.level, tx, ty), vis, light,
            offsetX = if (vis == 1f) actor.animOffsetX() else 0f,
            offsetY = if (vis == 1f) actor.animOffsetY() + (if (actor.rotateGlyph) -0.2f else 0f) else 0f,
            hue = actor.hue(),
            mirror = if (vis == 1f) actor.mirrorGlyph else false,
            rotate = if (vis == 1f) actor.rotateGlyph else false,
            isTall = glyph.tall
        )
        actor.gearDrawList.forEach { gear ->
            val trans = gear.glyphTransform()
            gearBatch.addTileQuad(
                tx, ty,
                gearBatch.getTextureIndex(trans?.glyph ?: gear.glyph(), App.level, tx, ty), vis, light,
                offsetX = actor.animOffsetX() + (trans?.x ?: 0f), offsetY = actor.animOffsetY() + (trans?.y ?: 0f),
                hue = gear.hue(), rotate = (trans?.rotate ?: false) && actor.rotateGlyph, mirror = actor.mirrorGlyph
            )
        }
        if (vis == 1f) {
            if (shadow == Glyph.MOB_WATER_SHADOW) {
                actor.renderShadow { x0, y0, x1, y1 ->
                    actorBatch.addPartialActorQuad(
                        x0, y0, x1, y1, actorBatch.getTextureIndex(shadow),
                        1f, fullLight, 0f, 0f, 1f, 1f, 1f, hue = 0f
                    )
                }
            }
            actor.drawStatusGlyph { statusGlyph ->
                uiWorldBatch.addTileQuad(
                    tx, ty,
                    uiBatch.getTextureIndex(statusGlyph), 1f, fullLight,
                    offsetX = actor.animOffsetX(), offsetY = -0.4f + actor.animOffsetY() + sinBob * 0.13f
                )
            }
        }
    }

    private val renderSpark: (Int, Int, Glyph, LightColor, Float, Float, Float, Float, Float)->Unit =
        { tx, ty, glyph, light, offsetX, offsetY, scale, alpha, hue ->
        worldBatches.firstOrNull { it is QuadBatch && it.tileSet.hasGlyph(glyph) }?.also { batch ->
            (batch as QuadBatch).addTileQuad(
                tx, ty,
                batch.getTextureIndex(glyph, App.level, tx, ty), 1f, light,
                offsetX, offsetY, scale.toDouble(), alpha, hue = hue
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
        if (width == this.width && height == this.height) return
        this.width = width
        this.height = height
        log.info("Screen resized to $width by $height")
        textCamera = OrthographicCamera(width.toFloat(), height.toFloat())

        panels.forEach { it.onResize(width, height) }

        Speaker.onResize(width, height)

        updateSurfaceParams()
    }

    fun toggleFullscreen() = toggleFullscreen(!fullscreen)
    fun toggleFullscreen(newValue: Boolean) {
        if (newValue && !fullscreen) {
            savedWindowSize = XY(width, height)
            fullscreen = true
            Gdx.graphics.setFullscreenMode(Gdx.graphics.displayMode)
        } else if (!newValue && fullscreen) {
            fullscreen = false
            Gdx.graphics.setWindowedMode(savedWindowSize.x, savedWindowSize.y)
        }
    }

    override fun render(delta: Float) {
        timeMs = System.currentTimeMillis()
        if (timeMs - lastDrawTime > 32L) {
            log.warn("Davey!  frame time ${timeMs - lastDrawTime}")
        }
        lastDrawTime = timeMs

        animateCamera(delta)
        Speaker.onRender(delta)
        Fire.onRender(delta)
        App.level.onRender(delta)
        var dismissedPanel: Panel? = null
        var topModalFound: Modal? = null
        var underModalFound: Modal? = null
        for (i in 0 until panels.size) {
            panels[i].also {
                it.onRender(delta)
                if (it.dismissed) dismissedPanel = it
                else if (it is Modal) {
                    underModalFound = topModalFound
                    topModalFound = it
                }
            }
        }
        dismissedPanel?.also {
            if (it is Modal) it.closeSound()?.also { sound -> Speaker.ui(sound, screenX = it.x) }
            panels.remove(it)
            it.dispose()
        }
        topModal = topModalFound
        underModal = underModalFound
        val tileWidth = topModal?.let { ((it.width.toDouble() / (width + Panel.RIGHT_PANEL_WIDTH - 16)) * 2.0) * aspectRatio / tileStride } ?: 0.0
        val tileHeight = topModal?.let { ((it.height.toDouble() / (height)) * 2.0) / tileStride } ?: 0.0
        this.cameraTargetOffset.x = when {
            Console.mouseInside -> 0.0 - (5.0 * cameraMenuShift)
            topModal?.position == Modal.Position.LEFT -> 0.0 - (tileWidth / 2.0 * cameraMenuShift)
            topModal?.position == Modal.Position.RIGHT -> 0.0 + (tileWidth / 2.0 * cameraMenuShift)
            else -> 0.0
        }
        this.cameraTargetOffset.y = when {
            topModal?.position == Modal.Position.TOP -> 0.0 - (tileHeight / 2.0 * cameraMenuShift)
            topModal?.position == Modal.Position.BOTTOM -> 0.0 + (tileHeight / 2.0 * cameraMenuShift)
            else -> 0.0
        }

        drawEverything(delta)

        if (
            (timeMs - lastActionTime > minActionInterval) ||
            (Keyboard.lastKeyTime > timeMs + 20L) ||
            (App.level.director.actionsPending)
        ) {
            LevelKeeper.runActorQueues()
            lastActionTime = timeMs
        }
    }

    fun restoreZoomIndex(index: Double) {
        zoomIndex = index
        zoomTarget = zoomLevels[zoomIndex.toInt()]
    }

    private fun currentZoomTarget(): Double {
        var t = zoomTarget
        t *= topModal?.zoomWhenOpen ?: 1f
        return t
    }

    private fun animateCamera(delta: Float) {
        sinBob = sin(((timeMs % 1000L).toFloat() * 0.001f) * 6.283f)

        // Animate zoom to zoomtarget
        if (brightness < brightnessTarget) {
            brightness = kotlin.math.min(brightnessTarget, brightness + delta * 0.6f)
        } else if (brightness > brightnessTarget) {
            brightness = kotlin.math.max(brightnessTarget, brightness - delta * 1.6f)
        }
        val ztarget = currentZoomTarget()
        val zdiff = min(3.0, max(0.04, abs(zoom - ztarget)))
        var newZoom = zoom
        if (zoom < ztarget) {
            newZoom = min(ztarget, zoom + zdiff * delta * ZOOM_SPEED)
        } else if (zoom > ztarget) {
            newZoom = max(ztarget, zoom - zdiff * delta * ZOOM_SPEED)
        }
        if (scrollLatch && zoom != newZoom) {
            dragPixels.setTo((dragPixels / zoom) * newZoom)
        }
        zoom = newZoom

        // Animate scroll latch release of dragPixels
        if (scrollLatch) {
            if (scrollDragging) {
                dragInertiaDelta = delta
                dragInertia.moveToInt((dragPixels - dragInertiaLast) * dragStartWeight)
                dragInertiaLast.moveToInt(dragPixels)
            } else {
                dragPixels.add((dragInertia / dragInertiaDelta) * delta)
                val txdist = pxToTiles(dragPixels.x.toInt()).toDouble()
                val tydist = pyToTiles(dragPixels.y.toInt()).toDouble()
                cameraPov = XYd(txdist, tydist) + pov
                dragInertia.scaleBy(dragBraking)
            }
            return
        }

        // Animate camera to camera target
        val target = cameraTargetOffset + pov

        val prevSpeed = cameraVec.magnitude()
        val desiredVec = (target - cameraPov) * cameraSpeed
        val desiredSpeed = desiredVec.magnitude()
        val accelMax = (cameraSpeed + desiredSpeed * 0.2f) * delta
        if (desiredVec.magnitude() <= prevSpeed + accelMax) {
            cameraVec = desiredVec
        } else {
            cameraVec = desiredVec.toUnitVec() * (prevSpeed + accelMax)
        }

        if (cameraVec.x < 0) {
            cameraPov.x = max(target.x, cameraPov.x + cameraVec.x * delta.toDouble())
        } else {
            cameraPov.x = min(target.x, cameraPov.x + cameraVec.x * delta.toDouble())
        }
        if (cameraVec.y < 0) {
            cameraPov.y = max(target.y, cameraPov.y + cameraVec.y * delta.toDouble())
        } else {
            cameraPov.y = min(target.y, cameraPov.y + cameraVec.y * delta.toDouble())
        }
    }

    fun recenterCamera() {
        lastPov.setTo(pov)
        cameraPov.setTo(pov)
        cameraVec = XYd(0.0,0.0)
    }

    fun clearCursor() {
        cursorPosition = null
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
        cursorPosition?.setTo(x, y)
    }

    fun cursorNextTarget(dir: Int) {
        cursorPosition?.also { cursor ->
            App.level.actorAt(cursor.x, cursor.y)?.also { actor ->
                ActorPanel.targetAfter(actor, dir)?.also { nextActor ->
                    cursor.setTo(nextActor)
                } ?: run { clearCursor() }
            } ?: run {
                ActorPanel.firstActor()?.also { actor ->
                    cursor.setTo(actor)
                } ?: run { clearCursor() }
            }
        } ?: run {
            ActorPanel.firstActor()?.also { actor ->
                cursorPosition = XY(actor.xy.x, actor.xy.y)
            } ?: run { clearCursor() }
        }
    }

    fun mouseMovedTo(screenX: Int, screenY: Int) {
        if (topModal != null) {
            topModal!!.mouseMovedTo(screenX, screenY)
        }
        if (topModal is ToolbarAddModal) {
            Toolbar.mouseMovedTo(screenX, screenY)
        }
        if (topModal == null) {
            panels.forEach {
                it.mouseMovedTo(screenX, screenY)
            }
            if (scrollDragging) {
                dragPixels.add(lastDrag - XY(screenX, screenY))
                lastDrag.setTo(screenX, screenY)
                val txdist = pxToTiles(dragPixels.x.toInt()).toDouble()
                val tydist = pyToTiles(dragPixels.y.toInt()).toDouble()
                cameraPov.setTo(pov + XYd(txdist, tydist))
            } else {
                val col = screenXtoTileX(screenX + dragPixels.x.toInt())
                val row = screenYtoTileY(screenY + dragPixels.y.toInt())
                if (col != cursorPosition?.x || row != cursorPosition?.y) {
                    if (!buttonBarsShown() && App.level.isSeenAt(col, row)) {
                        if (App.player.queuedActions.isEmpty()) {
                            val newCursor = XY(col, row)
                            cursorPosition = newCursor
                            return
                        }
                    }
                    clearCursor()
                }
            }
        }
    }

    private fun buttonBarsShown() = LeftButtons.isShown() || TimeButtons.isShown() || Toolbar.isShown()

    fun mouseScrolled(amount: Float) {
        if (topModal != null) {
            topModal!!.onMouseScrolled(amount)
            return
        }
        zoomIndex = max(0.0, min(zoomLevels.lastIndex.toDouble(), zoomIndex - amount.toDouble()))
        updateZoomTarget()
    }

    fun updateZoomTarget() { zoomTarget = zoomLevels[zoomIndex.toInt()] *
            (if (!App.level.isRoofedAt(App.player.xy.x, App.player.xy.y)) (1.0 / worldZoom) else 1.0) }

    fun mouseDown(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        if (topModal != null) {
            topModal!!.mouseClicked(screenX, screenY, button)
        }
        if (topModal is ToolbarAddModal) {
            Toolbar.mouseClicked(screenX, screenY, button)
        }
        if (topModal == null) {
            mutableListOf<Panel>().apply { addAll(panels) }.forEach {
                if (screenX >= it.x && screenX <= it.x + it.width && screenY >= it.y && screenY <= it.y + it.height) {
                    if (it.mouseClicked(screenX, screenY, button)) return true
                }
            }
            when (button) {
                Mouse.Button.LEFT -> {
                    lastDrag.setTo(screenX, screenY)
                    scrollDragging = true
                    scrollLatch = true
                    return true
                }
                Mouse.Button.RIGHT -> {
                    val x = screenXtoTileX(screenX + dragPixels.x.toInt())
                    val y = screenYtoTileY(screenY + dragPixels.y.toInt())
                    setCursorPosition(x,y)
                    rightClickCursorTile()
                    return true
                }
                else -> { return false }
            }
        }
        return false
    }

    fun mouseUp(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        topModal?.also { modal ->
            modal.mouseUp(screenX, screenY, button)
            return true
        } ?: run {
            when (button) {
                Mouse.Button.LEFT -> {
                    scrollDragging = false
                    return true
                }

                Mouse.Button.RIGHT -> {

                }

                else -> {
                    return false
                }
            }
        }
        return false
    }

    fun releaseScrollLatch() {
        scrollLatch = false
        scrollDragging = false
        dragPixels.setTo(0.0, 0.0)
    }

    fun rightClickCursorTile() {
        if (cursorPosition == null) cursorPosition = XY(App.player.xy.x, App.player.xy.y)
        val offset = (8.0 * zoom).toInt()
        val menu = ContextMenu(
            tileXtoScreenX(cursorPosition!!.x) - dragPixels.x.toInt() - offset,
            tileYtoScreenY(cursorPosition!!.y) - dragPixels.y.toInt() - offset
        ).apply {
            App.level.makeContextMenu(cursorPosition!!.x, cursorPosition!!.y, this)
        }
        if (menu.options.isNotEmpty()) addModal(menu) else {
            clearCursor()
            Speaker.ui(Speaker.SFX.UIERROR)
        }
    }

    fun povMoved() {
        clearCursor()
        releaseScrollLatch()
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
        modal.onAdd()
        modal.openSound()?.also { Speaker.ui(it, screenX = modal.x) }
        topModal = modal
    }

    private fun drawEverything(delta: Float) {

        val startTime = timeMs
        Gdx.gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        Gdx.gl.glEnable(GL_BLEND)

        allBatches.forEach { it.clear() }

        if (App.DEBUG_PERLIN != null) {
            App.DEBUG_PERLIN!!.forEachCellToRender(
                doTile = renderTile
            )
        } else {
            App.level.forEachCellToRender(
                doTile = renderTile,
                doQuad = renderQuad,
                doStain = renderStain,
                doFire = renderFire,
                doWeather = renderWeather,
                delta = delta
            )
            App.level.forEachThingToRender(renderThing, delta)
            App.level.forEachActorToRender(renderActor, delta)
            App.level.forEachSparkToRender(renderSpark)
        }
        if (terrainBatch.vertexCount < 1) { log.debug("Davey!  terrainBatch had 0 vertices") }


        uiWorldBatch.apply {
            cursorPosition?.also { cursorPosition ->
                addTileQuad(cursorPosition.x, cursorPosition.y,
                    getTextureIndex(Glyph.CURSOR), 1f, fullLight)
            }
        }

        panels.forEach { panel ->
            if (panel.drawsGrouped()) {
                panel.renderBackground()
                panel.renderEntities()
            }
        }

        allBatches.forEach {
            it.draw()
        }

        textBatch.apply {
            projectionMatrix = textCamera.combined
            enableBlending()
            begin()
            panels.forEach { panel ->
                if (panel !is Modal) {
                    panel.renderText()
                }
            }
            end()
        }

        panels.forEach { panel ->
            if (panel.drawsSeparate()) {
                panel.drawEverything()
            }
        }

    }

    fun grayOutLevel() = if (dragPixels.x != 0.0 || dragPixels.y != 0.0) 0.5f else 0.8f

    fun tileXtoGlx(col: Double) = ((col - (cameraPov.x) - 0.5) * tileStride) / aspectRatio
    fun tileYtoGly(row: Double) = ((row - (cameraPov.y) - 0.5) * tileStride)
    private fun tileXtoScreenX(tileX: Int) = ((width / 2.0) + (tileX - pov.x + 0.5) / aspectRatio * 0.5 * tileStride * width.toDouble()).toInt()
    private fun tileYtoScreenY(tileY: Int) = ((height / 2.0) + (tileY - pov.y - 0.5) * 0.5 * tileStride * height.toDouble()).toInt()
    private fun screenXtoTileX(screenX: Int): Int {
        val ix = (((((screenX.toFloat() - dragPixels.x) / width) * 2.0 - 1.0) * aspectRatio) + tileStride * 0.5) / tileStride + cameraPov.x
        return ix.toInt() + (if (ix < 0.0) -1 else 0)
    }
    private fun screenYtoTileY(screenY: Int): Int {
        val iy = (((screenY.toFloat() - dragPixels.y) / height) * 2.0 - 1.0 + tileStride * 0.5) / tileStride + cameraPov.y
        return iy.toInt() + (if (iy < 0.0) -1 else 0)
    }
    fun pxToTiles(p: Int): Float = ((p.toFloat() / width.toFloat() * 2f) * aspectRatio / tileStride).toFloat()
    fun pyToTiles(p: Int): Float = ((p.toFloat() / height.toFloat() * 2f) / tileStride).toFloat()

    private fun updateSurfaceParams() {
        aspectRatio = width.toDouble() / height.toDouble()
        tileStride = 1.0 / (height.coerceAtLeast(400).toDouble() * 0.01) * zoom
        renderTilesWide = kotlin.math.min(MAX_RENDER_WIDTH, ((width.toDouble() * tileStride / zoom / zoom) / 2 + 1).toInt())
        renderTilesHigh = kotlin.math.min(MAX_RENDER_HEIGHT, ((height.toDouble() * tileStride / zoom / zoom) / 2 + 1).toInt())
        log.debug("SCREEN RESIZE: new surface params aspect $aspectRatio stride $tileStride, $renderTilesWide by $renderTilesHigh tiles")
    }

    fun advanceTime(turns: Float) {
        panels.forEach { it.advanceTime(turns) }
    }

    override fun dispose() {
        allBatches.forEach { it.dispose() }
    }
}

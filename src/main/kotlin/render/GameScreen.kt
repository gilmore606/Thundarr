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
import ui.panels.Panel
import ui.modals.Modal
import util.XY
import util.log
import java.lang.Double.max
import java.lang.Double.min

const val RENDER_WIDTH = 160
const val RENDER_HEIGHT = 100

object GameScreen : KtxScreen {

    var zoom = 0.4
        set(value) {
            field = value
            updateSurfaceParams()
        }
    private val zoomLevels = listOf(0.15, 0.17, 0.2, 0.25, 0.3, 0.4, 0.5, 0.6, 0.7)
    private var zoomIndex = 3.0

    var width = 0
    var height = 0
    private var aspectRatio = 1.0
    private var tileStride: Double = 0.01

    private val dungeonBatch = QuadBatch(tileVertShader(), tileFragShader(), DungeonTileSet())
    private val mobBatch = QuadBatch(tileVertShader(), tileFragShader(), MobTileSet())
    private val uiBatch = QuadBatch(tileVertShader(), tileFragShader(), UITileSet())
    private val thingBatch = QuadBatch(tileVertShader(), tileFragShader(), ThingTileSet())
    private val textBatch = SpriteBatch()
    private var textCamera = OrthographicCamera(100f, 100f)

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
            borderWidth = 2f
            color = Color(1f, 1f, 1f, 1f)
            borderColor = Color(0f, 0f, 0f, 0.5f)
        })

    private val panels: MutableList<Panel> = mutableListOf()
    var topModal: Modal? = null

    private var cursorPosition: XY? = null
    private var cursorLine: MutableList<XY> = mutableListOf()

    private val pov get() = App.level.pov

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
        drawEverything()

        App.level.director.runQueue(App.level)
    }

    fun mouseMovedTo(screenX: Int, screenY: Int) {
        topModal?.also { modal ->
            modal.mouseMovedTo(screenX, screenY)
        } ?: run {
            val col = screenXtoTileX(screenX)
            val row = screenYtoTileY(screenY)
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

    private fun clearCursor() {
        cursorPosition = null
        cursorLine.clear()
    }

    fun mouseScrolled(amount: Float) {
        zoomIndex = max(0.0, min(zoomLevels.lastIndex.toDouble(), zoomIndex - amount.toDouble() * 0.6))
        zoom = zoomLevels[zoomIndex.toInt()]
    }

    fun mouseClicked(screenX: Int, screenY: Int): Boolean {
        topModal?.also { modal ->
            modal.mouseClicked(screenX, screenY)
        } ?: run {
            val x = screenXtoTileX(screenX)
            val y = screenYtoTileY(screenY)
            if (App.level.isWalkableAt(x, y)) {
                App.player.queue(WalkTo(App.level, x, y))
            }
        }
        return true
    }

    fun povMoved() {
        clearCursor()
    }

    fun addPanel(panel: Panel) {
        panels.add(panel)
    }

    fun addModal(modal: Modal) {
        clearCursor()
        modal.onResize(this.width, this.height)
        addPanel(modal)
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
    }

    private fun drawEverything() {

        Gdx.gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        Gdx.gl.glEnable(GL_BLEND)

        dungeonBatch.apply {
            clear()
            App.level.forEachCellToRender { tx, ty, vis, glyph ->
                val textureIndex = getTextureIndex(glyph, App.level, tx, ty)
                addTileQuad(
                    tx - pov.x, ty - pov.y, tileStride,
                    textureIndex, vis, aspectRatio
                )
            }
            draw()
        }

        mobBatch.apply {
            clear()
            App.level.forEachActorToRender { tx, ty, glyph ->
                addTileQuad(
                    tx - pov.x, ty - pov.y, tileStride,
                    getTextureIndex(glyph), 1f, aspectRatio)
            }
            draw()
        }

        thingBatch.apply {
            clear()
            App.level.forEachThingToRender { tx, ty, glyph ->
                addTileQuad(
                    tx - pov.x, ty - pov.y, tileStride,
                    getTextureIndex(glyph), 1f, aspectRatio)
            }
            draw()
        }

        uiBatch.apply {
            clear()
            cursorPosition?.also { cursorPosition ->
                addTileQuad(cursorPosition.x - pov.x, cursorPosition.y - pov.y, tileStride,
                    getTextureIndex(Glyph.CURSOR), 1f, aspectRatio)
                cursorLine.forEach { xy ->
                    addTileQuad(xy.x - pov.x, xy.y - pov.y, tileStride,
                        getTextureIndex(Glyph.CURSOR), 1f, aspectRatio)
                }
            }
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
        log.info("Surface params updated to $width x $height (ratio $aspectRatio tileStride $tileStride)")
    }

    override fun dispose() {
        dungeonBatch.dispose()
        mobBatch.dispose()
        uiBatch.dispose()
    }
}

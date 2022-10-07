package render

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20.*
import ktx.app.KtxScreen
import render.shaders.tileFragShader
import render.shaders.tileVertShader
import render.tilesets.DungeonTileSet
import render.tilesets.MobTileSet
import render.tilesets.UITileSet
import util.Glyph
import util.XY
import util.log
import java.lang.Double.max
import java.lang.Double.min


object GameScreen : KtxScreen {

    var zoom = 0.4
        set(value) {
            field = value
            updateSurfaceParams()
        }

    private var width = 0
    private var height = 0
    private var aspectRatio = 1.0
    private var tileStride: Double = 0.01

    private val dungeonBatch = QuadBatch(tileVertShader(), tileFragShader(), DungeonTileSet())
    private val mobBatch = QuadBatch(tileVertShader(), tileFragShader(), MobTileSet())
    private val uiBatch = QuadBatch(tileVertShader(), tileFragShader(), UITileSet())

    private var cursorPosition: XY = XY(-1,-1)
    private var cursorLine: MutableList<XY> = mutableListOf()

    private val pov get() = App.level.pov

    override fun show() {
        super.show()

        Gdx.gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        Gdx.gl.glEnable(GL_BLEND)

        updateSurfaceParams()
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        if (width == 0 || height == 0) return  // Windows does this on a hide/show, ignore it

        this.width = width
        this.height = height

        updateSurfaceParams()
    }

    override fun render(delta: Float) {
        App.level.updateTransientData()

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

        uiBatch.apply {
            clear()
            if (cursorPosition.x >= 0 && cursorPosition.y >= 0) {
                addTileQuad(cursorPosition.x - pov.x, cursorPosition.y - pov.y, tileStride,
                    getTextureIndex(Glyph.CURSOR), 1f, aspectRatio)
                cursorLine.forEach { xy ->
                    addTileQuad(xy.x - pov.x, xy.y - pov.y, tileStride,
                        getTextureIndex(Glyph.CURSOR), 1f, aspectRatio)
                }
            }
            draw()
        }
    }

    fun mouseMovedTo(screenX: Int, screenY: Int) {
        val glX = (screenX.toFloat() / width) * 2.0 - 1.0
        val glY = (screenY.toFloat() / height) * 2.0 - 1.0
        val col = (((glX * aspectRatio) + tileStride * 0.5) / tileStride + pov.x).toInt()
        val row = ((glY + tileStride * 0.5) / tileStride + pov.y).toInt()
        if (col != cursorPosition.x || row != cursorPosition.y) {
            if (App.level.isWalkableAt(col, row)) {
                cursorPosition.x = col
                cursorPosition.y = row
                cursorLine = App.level.getPathToPOV(cursorPosition).toMutableList()
            } else {
                cursorPosition.x = -1
                cursorPosition.y = -1
                cursorLine.clear()
            }
        }
    }

    fun mouseScrolled(amount: Float) {
        zoom = max(0.2, min(2.0, zoom - amount.toDouble() * 0.15))
    }

    fun povMoved() {
        cursorPosition.x = -1
        cursorPosition.y = -1
        cursorLine.clear()
    }

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

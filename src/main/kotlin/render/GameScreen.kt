package render

import ktx.app.KtxScreen
import mu.KotlinLogging
import render.shaders.tileFragShader
import render.shaders.tileVertShader
import render.tilesets.DungeonTileSet
import render.tilesets.UITileSet
import util.Tile
import util.XY
import world.Level

val log = KotlinLogging.logger {}

object GameScreen : KtxScreen {

    private var level: Level? = null

    var zoom = 0.4
        set(value) {
            field = value
            updateSurfaceParams()
        }

    private var width = 0
    private var height = 0
    private var aspectRatio = 1.0
    private var tileStride: Double = 0.01

    private val dungeonTiles = DungeonTileSet()
    private val dungeonDrawList = DrawList(tileVertShader(), tileFragShader(), dungeonTiles)
    //private val mobTiles
    //private val mobDrawList
    private val uiTiles = UITileSet()
    private val uiDrawList = DrawList(tileVertShader(), tileFragShader(), uiTiles)

    var cursorPosition: XY = XY(-1,-1)

    private val pov: XY
        get() = level?.pov ?: XY(0,0)



    fun observeLevel(level: Level) {
        this.level = level
    }

    fun moveCenter(newX: Int, newY: Int) {
        level?.pov = XY(newX, newY)
    }

    // temporary testing horseshit
    fun movePlayer(dir: XY) {
        level?.also { level -> level.pov += dir }
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
        updateSurfaceParams()
    }

    override fun dispose() {

    }

    override fun render(delta: Float) {
        level?.also { level ->
            level.updateVisibility()

            // TODO: optimize: only add onscreen quads
            dungeonDrawList.clear()
            for (tx in 0 until level.width) {
                for (ty in 0 until level.height) {
                    val vis = level.visibilityAt(tx, ty)
                    if (vis > 0f) {
                        val textureIndex = dungeonTiles.getIndex(
                            level.tiles[tx][ty],
                            level, tx, ty
                        )
                        dungeonDrawList.addTileQuad(
                            tx - pov.x, ty - pov.y, tileStride,
                            textureIndex,
                            vis,
                            aspectRatio
                        )
                    }
                }
            }
            dungeonDrawList.draw()
        }

        uiDrawList.clear()
        if (cursorPosition.x >= 0 && cursorPosition.y >= 0) {
            uiDrawList.addTileQuad(cursorPosition.x - pov.x, cursorPosition.y - pov.y, tileStride,
            uiTiles.getIndex(Tile.CURSOR), 0.5f, aspectRatio)
        }
        uiDrawList.draw()
    }

    fun mouseMovedTo(screenX: Int, screenY: Int) {
        val glX = (screenX.toFloat() / width) * 2.0 - 1.0
        val glY = (screenY.toFloat() / height) * 2.0 - 1.0
        val col = (((glX * aspectRatio) + tileStride * 0.5) / tileStride + pov.x).toInt()
        val row = ((glY + tileStride * 0.5) / tileStride + pov.y).toInt()
        if (col != cursorPosition.x || row != cursorPosition.y) {
            if (level?.isWalkableAt(col, row) == true) {
                cursorPosition.x = col
                cursorPosition.y = row
            } else {
                cursorPosition.x = -1
                cursorPosition.y = -1
            }
            log.info("cursor $col $row")
        }
    }


    private fun updateSurfaceParams() {
        aspectRatio = width.toDouble() / height.toDouble()
        tileStride = 1.0 / (height.coerceAtLeast(400).toDouble() * 0.01f) * zoom
        log.info("Surface params updated to $width x $height (ratio $aspectRatio tileStride $tileStride)")
    }

}

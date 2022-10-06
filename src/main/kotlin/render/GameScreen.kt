package render

import ktx.app.KtxScreen
import render.shaders.tileFragShader
import render.shaders.tileVertShader
import render.tilesets.DungeonTileSet
import util.XY
import world.Level

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
    private var stride: Double = 0.01
    private var pixelStride: Double = 0.01

    private val dungeonTiles = DungeonTileSet()
    private val dungeonDrawList = DrawList(tileVertShader(), tileFragShader(), dungeonTiles)
    //private val mobTiles
    //private val mobDrawList
    //private val uiTiles
    //private val uiDrawList

    private val pov: XY
        get() = level?.pov ?: XY(0,0)


    init {

    }

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
                            tx - pov.x, ty - pov.y, stride,
                            textureIndex,
                            vis,
                            aspectRatio
                        )
                    }
                }
            }

            dungeonDrawList.draw()

        }
    }

    private fun updateSurfaceParams() {
        aspectRatio = width.toDouble() / height.toDouble()
        stride = 1.0 / (height.coerceAtLeast(400).toDouble() * 0.01f) * zoom
        pixelStride = height / (2.0 / stride)
    }
}

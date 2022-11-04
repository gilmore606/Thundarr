package render.batches

import RESOURCE_FILE_DIR
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.GL20.GL_TEXTURE0
import render.Screen
import render.shaders.cloudFragShader
import render.shaders.cloudVertShader
import util.log

class CloudBatch : RenderBatch() {

    override fun vertShader() = cloudVertShader()
    override fun fragShader() = cloudFragShader()
    override fun vertexAttributes() = listOf(
        VertexAttribute(VertexAttributes.Usage.Position, 2, "a_Position"),
        VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_CloudUV"),
        VertexAttribute(VertexAttributes.Usage.Generic, 1, "a_CloudAlpha")
    )

    val TILE_SCALE = 170f

    val startTime = System.currentTimeMillis()

    private val cloudTexture = Texture(FileHandle("${RESOURCE_FILE_DIR}mask_clouds.png"), true).apply {
        setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
        setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)
        log.info("Loaded weather texture mask_clouds.png ($width x $height)")
    }


    inline fun FloatArray.addVertex(x: Float, y: Float, tx: Float, ty: Float, alpha: Float) {
        this[floatCount] = x
        this[floatCount+1] = y
        this[floatCount+2] = tx
        this[floatCount+3] = ty
        this[floatCount+4] = alpha
        floatCount += floatsPerVertex
        vertexCount++
    }

    inline fun addTileQuad(col: Int, row: Int, alpha: Float, fadeTop: Boolean = false) {
        val x0 = Screen.tileXtoGlx(col.toDouble()).toFloat()
        val y0 = 0f - Screen.tileYtoGly(row.toDouble()).toFloat()
        val x1 = Screen.tileXtoGlx(col + 1.0).toFloat()
        val y1 = 0f - Screen.tileYtoGly(row + 1.0).toFloat()

        val windX = App.level.weather.windX
        val windY = App.level.weather.windY
        val timeshift = ((System.currentTimeMillis() - startTime) / 128f) // + App.time
        val tcol = col.toFloat() + windX * timeshift
        val trow = row.toFloat() + windY * timeshift
        val tx0 = ((tcol % TILE_SCALE).toFloat() / TILE_SCALE)
        val ty0 = ((trow % TILE_SCALE).toFloat() / TILE_SCALE)
        val tx1 = tx0 + 1f / TILE_SCALE
        val ty1 = ty0 + 1f / TILE_SCALE
        floats.apply {
            addVertex(x0, y0, tx0, ty0, if (fadeTop) 0f else alpha)
            addVertex(x0, y1, tx0, ty1, alpha)
            addVertex(x1, y0, tx1, ty0, if (fadeTop) 0f else alpha)
            addVertex(x1, y0, tx1, ty0, if (fadeTop) 0f else alpha)
            addVertex(x0, y1, tx0, ty1, alpha)
            addVertex(x1, y1, tx1, ty1, alpha)
        }
    }

    override fun bindTextures() {
        Gdx.gl.glActiveTexture(GL_TEXTURE0)
        cloudTexture.bind()
        shader.setUniformi("u_CloudTexture", 0)
    }

    override fun dispose() {
        super.dispose()
        cloudTexture.dispose()
    }

}

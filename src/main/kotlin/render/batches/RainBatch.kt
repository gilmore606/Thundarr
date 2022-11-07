package render.batches

import RESOURCE_FILE_DIR
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.GL20.GL_TEXTURE0
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.graphics.VertexAttributes
import render.Screen
import render.shaders.rainFragShader
import render.shaders.rainVertShader
import util.log

class RainBatch : RenderBatch() {

    override fun vertShader() = rainVertShader()
    override fun fragShader() = rainFragShader()
    override fun vertexAttributes() = listOf(
        VertexAttribute(VertexAttributes.Usage.Position, 2, "a_Position"),
        VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_RainUV"),
        VertexAttribute(VertexAttributes.Usage.Generic, 2, "a_RainAlpha")
    )

    val startTime = System.currentTimeMillis()

    private val rainMask = Texture(FileHandle("${RESOURCE_FILE_DIR}mask_rainfall.png"), true).apply {
        setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        setWrap(Texture.TextureWrap.MirroredRepeat, Texture.TextureWrap.Repeat)
        log.info("Loaded weather texture mask_rainfall.png ($width x $height)")
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

        val scale = 1.3f / Screen.zoom.toFloat()

        val tx0 = x0 * scale
        val ty0 = y0 * scale
        val tx1 = x1 * scale
        val ty1 = y1 * scale

        val falpha = alpha * Screen.brightness

        floats.apply {
            addVertex(x0, y0, tx0, ty0, if (fadeTop) 0f else falpha)
            addVertex(x0, y1, tx0, ty1, falpha)
            addVertex(x1, y0, tx1, ty0, if (fadeTop) 0f else falpha)
            addVertex(x1, y0, tx1, ty0, if (fadeTop) 0f else falpha)
            addVertex(x0, y1, tx0, ty1, falpha)
            addVertex(x1, y1, tx1, ty1, falpha)
        }
    }

    override fun bindTextures() {
        Gdx.gl.glActiveTexture(GL_TEXTURE0)
        rainMask.bind()
        shader.setUniformi("u_RainMask", 0)
        shader.setUniformf("u_Time", (System.currentTimeMillis() - startTime).toFloat())
        shader.setUniformf("u_Speed", 0.005f)
    }

    override fun dispose() {
        super.dispose()
        rainMask.dispose()
    }

}

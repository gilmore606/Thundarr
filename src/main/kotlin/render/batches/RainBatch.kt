package render.batches

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20.GL_TEXTURE0
import com.badlogic.gdx.graphics.GL20.GL_TEXTURE1
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
        VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_UV"),
        VertexAttribute(VertexAttributes.Usage.Generic, 1, "a_RainAlpha"),
        VertexAttribute(VertexAttributes.Usage.Generic, 1, "a_SnowAlpha")
    )

    val startTime = System.currentTimeMillis()

    private val rainMask = Texture(Gdx.files.internal("res/masks/mask_rainfall.png"), true).apply {
        setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        setWrap(Texture.TextureWrap.MirroredRepeat, Texture.TextureWrap.Repeat)
        log.info("Loaded weather texture mask_rainfall.png ($width x $height)")
    }

    private val snowMask = Texture(Gdx.files.internal("res/masks/mask_snowfall.png"), true).apply {
        setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        setWrap(Texture.TextureWrap.MirroredRepeat, Texture.TextureWrap.Repeat)
        log.info("Loaded weather texture mask_snowfall.png ($width x $height)")
    }


    inline fun FloatArray.addVertex(x: Float, y: Float, tx: Float, ty: Float, ralpha: Float, salpha: Float) {
        this[floatCount] = x
        this[floatCount+1] = y
        this[floatCount+2] = tx
        this[floatCount+3] = ty
        this[floatCount+4] = ralpha
        this[floatCount+5] = salpha
        floatCount += floatsPerVertex
        vertexCount++
    }

    inline fun addTileQuad(col: Int, row: Int, rainAlpha: Float, snowAlpha: Float, fadeTop: Boolean = false) {
        val x0 = Screen.tileXtoGlx(col.toDouble()).toFloat()
        val y0 = 0f - Screen.tileYtoGly(row.toDouble()).toFloat()
        val x1 = Screen.tileXtoGlx(col + 1.0).toFloat()
        val y1 = 0f - Screen.tileYtoGly(row + 1.0).toFloat()

        val scale = 1.3f / Screen.zoom.toFloat()

        val tx0 = x0 * scale
        val ty0 = y0 * scale
        val tx1 = x1 * scale
        val ty1 = y1 * scale

        val ralpha = rainAlpha * Screen.brightness
        val salpha = snowAlpha * Screen.brightness

        floats.apply {
            addVertex(x0, y0, tx0, ty0, if (fadeTop) 0f else ralpha, if (fadeTop) 0f else salpha)
            addVertex(x0, y1, tx0, ty1, ralpha, salpha)
            addVertex(x1, y0, tx1, ty0, if (fadeTop) 0f else ralpha, if (fadeTop) 0f else salpha)
            addVertex(x1, y0, tx1, ty0, if (fadeTop) 0f else ralpha, if (fadeTop) 0f else salpha)
            addVertex(x0, y1, tx0, ty1, ralpha, salpha)
            addVertex(x1, y1, tx1, ty1, ralpha, salpha)
        }
    }

    override fun bindTextures() {
        Gdx.gl.glActiveTexture(GL_TEXTURE0)
        rainMask.bind()
        shader.setUniformi("u_RainMask", 0)
        Gdx.gl.glActiveTexture(GL_TEXTURE1)
        snowMask.bind()
        shader.setUniformi("u_SnowMask", 1)
        shader.setUniformf("u_Time", (Screen.timeMs - startTime).toFloat())
        shader.setUniformf("u_RainSpeed", 0.005f)
        shader.setUniformf("u_SnowSpeed", 0.0013f - Screen.sinBob * 0.00000015f)
        Gdx.gl.glActiveTexture(GL_TEXTURE0)
    }

    override fun dispose() {
        super.dispose()
        rainMask.dispose()
        snowMask.dispose()
    }

}

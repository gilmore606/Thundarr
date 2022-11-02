package render.batches

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20.GL_TEXTURE0
import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.graphics.VertexAttributes.Usage
import render.Screen
import render.shaders.tileFragShader
import render.shaders.tileVertShader
import render.tilesets.TileSet
import render.tilesets.Glyph
import util.*
import world.Level
import java.lang.Float.min

class QuadBatch(
    val tileSet: TileSet
) : RenderBatch() {

    override fun vertShader() = tileVertShader()
    override fun fragShader() = tileFragShader()
    override fun vertexAttributes() = listOf(
        VertexAttribute(Usage.Position, 2, "a_Position"),
        VertexAttribute(Usage.TextureCoordinates, 2, "a_TexCoordinate"),
        VertexAttribute(Usage.ColorUnpacked, 4, "a_Light"),
        VertexAttribute(Usage.Generic, 1, "a_Grayout"),
        VertexAttribute(Usage.Generic, 1, "a_Hue")
    )

    val textureIndexCache = tileSet.getCache()
    private val textureEdgePad = 0.0015f
    private val quadEdgePadX = 0.0002f
    private val quadEdgePadY = -0.0024f


    override fun bindTextures() {
        Gdx.gl.glActiveTexture(GL_TEXTURE0)
        tileSet.texture.bind()
        shader.setUniformi("u_Texture", 0)
    }

    override fun dispose() {
        super.dispose()
        tileSet.dispose()
    }

    private inline fun FloatArray.addVertex(x: Float, y: Float, tx: Float, ty: Float,
                                            lightR: Float, lightG: Float, lightB: Float, lightA: Float,
                                            grayOut: Float, hue: Float) {
        this[floatCount] = x
        this[floatCount+1] = y
        this[floatCount+2] = tx
        this[floatCount+3] = ty
        this[floatCount+4] = lightR
        this[floatCount+5] = lightG
        this[floatCount+6] = lightB
        this[floatCount+7] = lightA
        this[floatCount+8] = grayOut
        this[floatCount+9] = hue
        floatCount += floatsPerVertex
        vertexCount++
    }

    fun addTileQuad(col: Int, row: Int, // global tile XY
                    textureIndex: Int, visibility: Float, light: LightColor,
                    offsetX: Float = 0f, offsetY: Float = 0f,
                    scale: Double = 1.0, alpha: Float = 1f, hue: Float = 0f) {
        val scaleOffset = (1.0 - scale) * 0.5
        val x0 = Screen.tileXtoGlx(col + offsetX + scaleOffset)
        val y0 = Screen.tileYtoGly(row + offsetY + scaleOffset)
        val x1 = Screen.tileXtoGlx(col + offsetX + 1.0 - scaleOffset * 2.0)
        val y1 = Screen.tileYtoGly(row + offsetY + 1.0 - scaleOffset * 2.0)
        val lightR = min(visibility, light.r + App.level.weather.lightning.r)
        val lightG = min(visibility, light.g + App.level.weather.lightning.g)
        val lightB = min(visibility, light.b + App.level.weather.lightning.b)
        val grayOut = if (visibility < 1f) 1f else App.level.weather.lightning.r * 0.6f
        addQuad(x0, y0, x1, y1, 0f, 0f, 1f, 1f, textureIndex, lightR, lightG, lightB, alpha, grayOut, hue)
    }

    fun addPartialQuad(x0: Double, y0: Double, x1: Double, y1: Double,
                       textureIndex: Int, visibility: Float, light: LightColor,
                       tx0: Float, ty0: Float, tx1: Float, ty1: Float,
                       alpha: Float = 1f, hue: Float = 0f) {
        val gx0 = Screen.tileXtoGlx(x0)
        val gy0 = Screen.tileYtoGly(y0)
        val gx1 = Screen.tileXtoGlx(x1)
        val gy1 = Screen.tileYtoGly(y1)
        val lightR = min(visibility, light.r + App.level.weather.lightning.r)
        val lightG = min(visibility, light.g + App.level.weather.lightning.g)
        val lightB = min(visibility, light.b + App.level.weather.lightning.b)
        val grayOut = if (visibility < 1f) 1f else App.level.weather.lightning.r * 0.6f
        addQuad(gx0, gy0, gx1, gy1, tx0, ty0, tx1, ty1, textureIndex, lightR, lightG, lightB, alpha, grayOut, hue)
    }

    fun addPixelQuad(x0: Int, y0: Int, x1: Int, y1: Int, // absolute screen pixel XY
                     textureIndex: Int, lightR: Float = 1f, lightG: Float = 1f, lightB: Float = 1f,
                     hue: Float? = null) {
        val glx0 = (x0 / Screen.width.toDouble()) * 2f - 1f
        val gly0 = (y0 / Screen.height.toDouble()) * 2f - 1f
        val glx1 = (x1 / Screen.width.toDouble()) * 2f - 1f
        val gly1 = (y1 / Screen.height.toDouble()) * 2f - 1f
        addQuad(glx0, gly0, glx1, gly1, 0f, 0f, 1f, 1f, textureIndex,
                lightR, lightG, lightB, 1f, 0f, hue ?: Screen.uiHue.toFloat())
    }

    fun addHealthBar(x0: Int, y0: Int, x1: Int, y1: Int, // absolute screen pixel XY
                     hp: Int, hpMax: Int) {
        val amount = (hp.toFloat() / hpMax.toFloat())
        if (amount >= 1f) return
        val xMid = x0 + ((x1 - x0) * amount).toInt()
        val glx0 = (x0 / Screen.width.toDouble()) * 2f - 1f
        val gly0 = (y0 / Screen.height.toDouble()) * 2f - 1f
        val glx1 = (x1 / Screen.width.toDouble()) * 2f - 1f
        val gly1 = (y1 / Screen.height.toDouble()) * 2f - 1f
        val glxMid = (xMid / Screen.width.toDouble()) * 2f - 1f
        val textureIndex = getTextureIndex(Glyph.COLOR_BARS)

        val texOffset = when {
            amount < 0.35 -> 0f
            amount < 0.65 -> 0.125f
            else -> 0.25f
        }
        addQuad(glx0, gly0, glxMid, gly1, textureIndex = textureIndex, ity0 = texOffset + 0.001f, ity1 = texOffset + 0.124f)
        addQuad(glxMid, gly0, glx1, gly1, textureIndex = textureIndex, ity0 = 0.376f, ity1 = 0.499f)
    }

    private inline fun addQuad(ix0: Double, iy0: Double, ix1: Double, iy1: Double, // GL screen float XY
                        itx0: Float = 0f, ity0: Float = 0f, itx1: Float = 0f, ity1: Float = 0f,
                        textureIndex: Int, lightR: Float = 1f, lightG: Float = 1f, lightB: Float = 1f, lightA: Float = 1f,
                               grayOut: Float = 0f, hue: Float = 0f
    ) {
        val x0 = ix0.toFloat() - quadEdgePadX
        val y0 = -iy0.toFloat() - quadEdgePadY
        val x1 = ix1.toFloat() + quadEdgePadX
        val y1 = -iy1.toFloat() + quadEdgePadY
        val tx0 = (((textureIndex % tileSet.tilesPerRow) + itx0) * tileSet.tileRowStride).toFloat() + textureEdgePad
        val ty0 = (((textureIndex / tileSet.tilesPerRow) + ity0) * tileSet.tileColumnStride).toFloat() + textureEdgePad
        val tx1 = (((textureIndex % tileSet.tilesPerRow) + itx1) * tileSet.tileRowStride).toFloat() - textureEdgePad
        val ty1 = (((textureIndex / tileSet.tilesPerRow) + ity1) * tileSet.tileColumnStride).toFloat() - textureEdgePad
        floats.apply {
            addVertex(x0, y0, tx0, ty0, lightR, lightG, lightB, lightA, grayOut, hue)
            addVertex(x0, y1, tx0, ty1, lightR, lightG, lightB, lightA, grayOut, hue)
            addVertex(x1, y0, tx1, ty0, lightR, lightG, lightB, lightA, grayOut, hue)
            addVertex(x1, y0, tx1, ty0, lightR, lightG, lightB, lightA, grayOut, hue)
            addVertex(x0, y1, tx0, ty1, lightR, lightG, lightB, lightA, grayOut, hue)
            addVertex(x1, y1, tx1, ty1, lightR, lightG, lightB, lightA, grayOut, hue)
        }
    }

    inline fun getTextureIndex(glyph: Glyph, level: Level? = null, x: Int = 0, y: Int = 0): Int {
        return textureIndexCache[glyph] ?: tileSet.getIndex(glyph, level, x, y)
    }

}

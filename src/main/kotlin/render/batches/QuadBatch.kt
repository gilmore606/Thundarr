package render.batches

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20.GL_TEXTURE0
import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.graphics.VertexAttributes.Usage
import render.Screen
import render.Screen.aspectRatio
import render.shaders.tileFragShader
import render.shaders.tileVertShader
import render.tilesets.TileSet
import render.tilesets.Glyph
import util.*
import world.level.Level
import java.lang.Float.min
import kotlin.math.abs
import kotlin.math.sign

class QuadBatch(
    val tileSet: TileSet,
    maxQuads: Int = 80000,
) : RenderBatch(maxQuads) {

    val startTime = System.currentTimeMillis()

    override fun vertShader() = tileVertShader()
    override fun fragShader() = tileFragShader()
    override fun vertexAttributes() = listOf(
        VertexAttribute(Usage.Position, 2, "a_Position"),
        VertexAttribute(Usage.TextureCoordinates, 2, "a_TexCoordinate"),
        VertexAttribute(Usage.ColorUnpacked, 4, "a_Light"),
        VertexAttribute(Usage.Generic, 1, "a_Grayout"),
        VertexAttribute(Usage.Generic, 1, "a_Hue"),
        VertexAttribute(Usage.Generic, 1, "a_Waves")
    )

    val textureIndexCache = tileSet.getCache()
    private val textureEdgePad = 0.0012f
    private val quadEdgePadX = 0.0002f
    private val quadEdgePadY = 0f


    override fun bindTextures() {
        Gdx.gl.glActiveTexture(GL_TEXTURE0)
        tileSet.texture.bind()
        shader.setUniformi("u_Texture", 0)
        shader.setUniformf("u_Time", (Screen.timeMs - startTime).toFloat() / 1000f)
        shader.setUniformf("u_Zoom", Screen.zoom.toFloat())
        shader.setUniformf("u_CameraX", Screen.cameraPovX.toFloat() / 8f)
        shader.setUniformf("u_CameraY", Screen.cameraPovY.toFloat() / 6f)
    }

    override fun dispose() {
        super.dispose()
        tileSet.dispose()
    }

    private inline fun FloatArray.addVertex(x: Float, y: Float, tx: Float, ty: Float,
                                            lightR: Float, lightG: Float, lightB: Float, lightA: Float,
                                            grayOut: Float, hue: Float, waves: Float) {
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
        this[floatCount+10] = waves
        floatCount += floatsPerVertex
        vertexCount++
    }

    fun addTileQuad(col: Int, row: Int, // global tile XY
                    textureIndex: Int, visibility: Float, light: LightColor,
                    offsetX: Float = 0f, offsetY: Float = 0f,
                    scale: Double = 1.0, alpha: Float = 1f, hue: Float = 0f,
                    grayBlend: Float? = null, mirror: Boolean = false, rotate: Boolean = false,
                    waves: Float = 0f, isTall: Boolean = false) {
        val scaleOffset = (1.0 - scale) * 0.5
        val x0 = Screen.tileXtoGlx(col + offsetX + scaleOffset)
        val y0 = Screen.tileYtoGly((row - (if (isTall) 1 else 0)) + offsetY + scaleOffset)
        val x1 = Screen.tileXtoGlx(col + offsetX + 1.0 - scaleOffset * 2.0)
        val y1 = Screen.tileYtoGly(row + offsetY + 1.0 - scaleOffset * 2.0)
        val lightR = min(visibility, light.r + App.level.weather.lightning.r) * Screen.brightness
        val lightG = min(visibility, light.g + App.level.weather.lightning.g) * Screen.brightness
        val lightB = min(visibility, light.b + App.level.weather.lightning.b) * Screen.brightness
        val grayOut = grayBlend ?: if (visibility < 1f) 1f else App.level.weather.lightning.r * 0.6f
        val itx0 = if (mirror) 1f else 0f
        val itx1 = if (mirror) 0f else 1f
        addQuad(x0, y0, x1, y1, itx0, 0f, itx1, if (isTall) 2f else 1f, textureIndex, lightR, lightG, lightB, alpha, grayOut, hue, rotate, waves)
    }

    fun addPartialQuad(x0: Double, y0: Double, x1: Double, y1: Double,
                       textureIndex: Int, visibility: Float, light: LightColor,
                       tx0: Float, ty0: Float, tx1: Float, ty1: Float,
                       alpha: Float = 1f, hue: Float = 0f, rotate: Boolean = false) {
        val gx0 = Screen.tileXtoGlx(x0)
        val gy0 = Screen.tileYtoGly(y0)
        val gx1 = Screen.tileXtoGlx(x1)
        val gy1 = Screen.tileYtoGly(y1)
        val lightR = min(visibility, light.r + App.level.weather.lightning.r) * Screen.brightness
        val lightG = min(visibility, light.g + App.level.weather.lightning.g) * Screen.brightness
        val lightB = min(visibility, light.b + App.level.weather.lightning.b) * Screen.brightness
        val grayOut = if (visibility < 1f) 1f else App.level.weather.lightning.r * 0.6f
        addQuad(gx0, gy0, gx1, gy1, tx0, ty0, tx1, ty1, textureIndex, lightR, lightG, lightB, alpha, grayOut, hue, rotate)
    }

    fun addPixelQuad(x0: Int, y0: Int, x1: Int, y1: Int, // absolute screen pixel XY
                     textureIndex: Int, lightR: Float = 1f, lightG: Float = 1f, lightB: Float = 1f,
                     hue: Float? = null, alpha: Float = 1f, mirror: Boolean = false, isTall: Boolean = false) {
        val glx0 = (x0 / Screen.width.toDouble()) * 2f - 1f
        val gly0 = (y0 / Screen.height.toDouble()) * 2f - 1f
        val glx1 = (x1 / Screen.width.toDouble()) * 2f - 1f
        val gly1 = (y1 / Screen.height.toDouble()) * 2f - 1f
        val itx0 = if (mirror) 1f else 0f
        val itx1 = if (mirror) 0f else 1f
        addQuad(glx0, gly0, glx1, gly1, itx0, 0f, itx1, if (isTall) 2f else 1f, textureIndex,
                lightR, lightG, lightB, alpha, 0f, hue ?: Screen.uiHue.toFloat())
    }

    fun addHealthBar(x0: Int, y0: Int, x1: Int, y1: Int, // absolute screen pixel XY
                     hp: Int, hpMax: Int, allGreen: Boolean = false) {
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
            allGreen -> 0.25f
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
                               grayOut: Float = 0f, hue: Float = 0f, rotate: Boolean = false, waves: Float = 0f
    ) {
        val z = Screen.zoom.toFloat()
        val x0 = ix0.toFloat() - quadEdgePadX * z
        val y0 = -iy0.toFloat() - quadEdgePadY * z
        val x1 = ix1.toFloat() + quadEdgePadX * z
        val y1 = -iy1.toFloat() + quadEdgePadY * z

        val tx0 = (((textureIndex % tileSet.tilesPerRow) + itx0) * tileSet.tileRowStride).toFloat() + textureEdgePad
        val ty0 = (((textureIndex / tileSet.tilesPerRow) + ity0) * tileSet.tileColumnStride).toFloat() + textureEdgePad
        val tx1 = (((textureIndex % tileSet.tilesPerRow) + itx1) * tileSet.tileRowStride).toFloat() - textureEdgePad
        val ty1 = (((textureIndex / tileSet.tilesPerRow) + ity1) * tileSet.tileColumnStride).toFloat() - textureEdgePad

        if (rotate) {
            floats.apply {
                addVertex(x1, y0, tx0, ty0, lightR, lightG, lightB, lightA, grayOut, hue, waves)
                addVertex(x1, y1, tx1, ty0, lightR, lightG, lightB, lightA, grayOut, hue, waves)
                addVertex(x0, y1, tx1, ty1, lightR, lightG, lightB, lightA, grayOut, hue, waves)
                addVertex(x0, y1, tx1, ty1, lightR, lightG, lightB, lightA, grayOut, hue, waves)
                addVertex(x0, y0, tx0, ty1, lightR, lightG, lightB, lightA, grayOut, hue, waves)
                addVertex(x1, y0, tx0, ty0, lightR, lightG, lightB, lightA, grayOut, hue, waves)
            }
        } else {
            floats.apply {
                addVertex(x0, y0, tx0, ty0, lightR, lightG, lightB, lightA, grayOut, hue, waves)
                addVertex(x0, y1, tx0, ty1, lightR, lightG, lightB, lightA, grayOut, hue, waves)
                addVertex(x1, y0, tx1, ty0, lightR, lightG, lightB, lightA, grayOut, hue, waves)
                addVertex(x1, y0, tx1, ty0, lightR, lightG, lightB, lightA, grayOut, hue, waves)
                addVertex(x0, y1, tx0, ty1, lightR, lightG, lightB, lightA, grayOut, hue, waves)
                addVertex(x1, y1, tx1, ty1, lightR, lightG, lightB, lightA, grayOut, hue, waves)
            }
        }
    }

    inline fun getTextureIndex(glyph: Glyph, level: Level? = null, x: Int = 0, y: Int = 0): Int {
        return textureIndexCache[glyph] ?: tileSet.getIndex(glyph, level, x, y)
    }

    inline fun getTerrainTextureIndex(glyph: Glyph, level: Level? = null, x: Int = 0, y: Int = 0): Int {
        if (tileSet.tileHolders[glyph]?.cacheable != true) return getTextureIndex(glyph, level, x, y)
        level?.chunkAt(x,y)?.also { chunk ->
            val cx = x - chunk.x
            val cy = y - chunk.y
            if (cx >= 0 && cy >= 0 && cx < chunk.glyphCache.size && cy < chunk.glyphCache[cx].size) {
                chunk.glyphCache[cx][cy]?.also { return it } ?: run {
                    val index = tileSet.getIndex(glyph, level, x, y)
                    chunk.glyphCache[cx][cy] = index
                    return index
                }
            }
        }
        return tileSet.getIndex(glyph)
    }

}

package render.batches

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.graphics.VertexAttributes
import render.Screen
import render.shaders.actorFragShader
import render.shaders.actorVertShader
import render.tilesets.Glyph
import render.tilesets.TileSet
import util.LightColor
import world.level.Level

class ActorBatch(
    val tileSet: TileSet,
    maxQuads: Int = 5000,
) : RenderBatch(maxQuads) {

    companion object {
        val noAura = LightColor(0f, 0f, 0f, 0f)
    }

    private val textureEdgePad = 0.0012f
    private val quadEdgePadX = 0.0002f
    private val quadEdgePadY = 0f

    override fun vertShader() = actorVertShader()
    override fun fragShader() = actorFragShader()
    override fun vertexAttributes() = listOf(
        VertexAttribute(VertexAttributes.Usage.Position, 2, "a_Position"),
        VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_TexCoordinate"),
        VertexAttribute(VertexAttributes.Usage.ColorUnpacked, 4, "a_Light"),
        VertexAttribute(VertexAttributes.Usage.ColorUnpacked, 4, "a_Aura"),
        VertexAttribute(VertexAttributes.Usage.Generic, 1, "a_Grayout"),
        VertexAttribute(VertexAttributes.Usage.Generic, 1, "a_Hue"),
    )

    override fun bindTextures() {
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0)
        tileSet.texture.bind()
        shader.setUniformi("u_Texture", 0)
    }

    override fun dispose() {
        super.dispose()
        tileSet.dispose()
    }

    private inline fun FloatArray.addVertex(x: Float, y: Float, tx: Float, ty: Float,
                                            lightR: Float, lightG: Float, lightB: Float, lightA: Float,
                                            auraR: Float, auraG: Float, auraB: Float, auraA: Float,
                                            grayOut: Float, hue: Float) {
        this[floatCount] = x
        this[floatCount+1] = y
        this[floatCount+2] = tx
        this[floatCount+3] = ty
        this[floatCount+4] = lightR
        this[floatCount+5] = lightG
        this[floatCount+6] = lightB
        this[floatCount+7] = lightA
        this[floatCount+8] = auraR
        this[floatCount+9] = auraG
        this[floatCount+10] = auraB
        this[floatCount+11] = auraA
        this[floatCount+12] = grayOut
        this[floatCount+13] = hue
        floatCount += floatsPerVertex
        vertexCount++
    }

    fun addActorQuad(col: Int, row: Int, // global tile XY
                    textureIndex: Int, visibility: Float, light: LightColor,
                    offsetX: Float = 0f, offsetY: Float = 0f,
                    scale: Double = 1.0, alpha: Float = 1f, aura: LightColor = noAura, hue: Float = 0f,
                    grayBlend: Float? = null, mirror: Boolean = false, rotate: Boolean = false,
                    isTall: Boolean = false) {
        val scaleOffset = (1.0 - scale) * 0.5
        val x0 = Screen.tileXtoGlx(col + offsetX + scaleOffset)
        val y0 = Screen.tileYtoGly((row - (if (isTall) 1 else 0)) + offsetY + scaleOffset)
        val x1 = Screen.tileXtoGlx(col + offsetX + 1.0 - scaleOffset * 2.0)
        val y1 = Screen.tileYtoGly(row + offsetY + 1.0 - scaleOffset * 2.0)
        val lightR = java.lang.Float.min(visibility, light.r + App.level.weather.lightning.r) * Screen.brightness
        val lightG = java.lang.Float.min(visibility, light.g + App.level.weather.lightning.g) * Screen.brightness
        val lightB = java.lang.Float.min(visibility, light.b + App.level.weather.lightning.b) * Screen.brightness
        val grayOut = grayBlend ?: if (visibility < 1f) Screen.grayOutLevel() else App.level.weather.lightning.r * 0.6f
        val itx0 = if (mirror) 0.98f else 0f
        val itx1 = if (mirror) 0.02f else 1f
        addQuad(x0, y0, x1, y1, itx0, 0f, itx1, if (isTall) 2f else 1f, textureIndex, lightR, lightG, lightB, alpha,
            aura.r, aura.g, aura.b, aura.a, grayOut, hue, rotate)
    }

    fun addPartialActorQuad(x0: Double, y0: Double, x1: Double, y1: Double,
                       textureIndex: Int, visibility: Float, light: LightColor,
                       tx0: Float, ty0: Float, tx1: Float, ty1: Float,
                       alpha: Float = 1f, aura: LightColor = noAura, hue: Float = 0f, rotate: Boolean = false) {
        val gx0 = Screen.tileXtoGlx(x0)
        val gy0 = Screen.tileYtoGly(y0)
        val gx1 = Screen.tileXtoGlx(x1)
        val gy1 = Screen.tileYtoGly(y1)
        val lightR = java.lang.Float.min(visibility, light.r + App.level.weather.lightning.r) * Screen.brightness
        val lightG = java.lang.Float.min(visibility, light.g + App.level.weather.lightning.g) * Screen.brightness
        val lightB = java.lang.Float.min(visibility, light.b + App.level.weather.lightning.b) * Screen.brightness
        val grayOut = if (visibility < 1f) 1f else App.level.weather.lightning.r * 0.6f
        addQuad(gx0, gy0, gx1, gy1, tx0, ty0, tx1, ty1, textureIndex, lightR, lightG, lightB, alpha,
            aura.r, aura.g, aura.b, aura.a, grayOut, hue, rotate)
    }

    private inline fun addQuad(ix0: Double, iy0: Double, ix1: Double, iy1: Double, // GL screen float XY
                               itx0: Float = 0f, ity0: Float = 0f, itx1: Float = 0f, ity1: Float = 0f,
                               textureIndex: Int, lightR: Float = 1f, lightG: Float = 1f, lightB: Float = 1f, lightA: Float = 1f,
                               auraR: Float = 0f, auraG: Float = 0f, auraB: Float = 0f, auraA: Float = 0f,
                               grayOut: Float = 0f, hue: Float = 0f, rotate: Boolean = false
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
                addVertex(x1, y0, tx0, ty0, lightR, lightG, lightB, lightA, auraR, auraG, auraB, auraA, grayOut, hue)
                addVertex(x1, y1, tx1, ty0, lightR, lightG, lightB, lightA, auraR, auraG, auraB, auraA, grayOut, hue)
                addVertex(x0, y1, tx1, ty1, lightR, lightG, lightB, lightA, auraR, auraG, auraB, auraA, grayOut, hue)
                addVertex(x0, y1, tx1, ty1, lightR, lightG, lightB, lightA, auraR, auraG, auraB, auraA, grayOut, hue)
                addVertex(x0, y0, tx0, ty1, lightR, lightG, lightB, lightA, auraR, auraG, auraB, auraA, grayOut, hue)
                addVertex(x1, y0, tx0, ty0, lightR, lightG, lightB, lightA, auraR, auraG, auraB, auraA, grayOut, hue)
            }
        } else {
            floats.apply {
                addVertex(x0, y0, tx0, ty0, lightR, lightG, lightB, lightA, auraR, auraG, auraB, auraA, grayOut, hue)
                addVertex(x0, y1, tx0, ty1, lightR, lightG, lightB, lightA, auraR, auraG, auraB, auraA, grayOut, hue)
                addVertex(x1, y0, tx1, ty0, lightR, lightG, lightB, lightA, auraR, auraG, auraB, auraA, grayOut, hue)
                addVertex(x1, y0, tx1, ty0, lightR, lightG, lightB, lightA, auraR, auraG, auraB, auraA, grayOut, hue)
                addVertex(x0, y1, tx0, ty1, lightR, lightG, lightB, lightA, auraR, auraG, auraB, auraA, grayOut, hue)
                addVertex(x1, y1, tx1, ty1, lightR, lightG, lightB, lightA, auraR, auraG, auraB, auraA, grayOut, hue)
            }
        }
    }

    inline fun getTextureIndex(glyph: Glyph, level: Level? = null, x: Int = 0, y: Int = 0): Int {
        return tileSet.getIndex(glyph, level, x, y)
    }
}

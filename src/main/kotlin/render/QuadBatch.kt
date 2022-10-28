package render

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.GL20.GL_TEXTURE0
import com.badlogic.gdx.graphics.Mesh
import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.graphics.VertexAttributes.Usage
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import ktx.graphics.use
import render.tilesets.TileSet
import render.tilesets.Glyph
import util.*
import world.Level
import java.lang.Float.min

class QuadBatch(
    vertexShaderSource: String,
    fragmentShaderSource: String,
    val tileSet: TileSet
) {

    private val FLOATS_PER_VERTEX = 9
    private val MAX_QUADS = 40000

    private val floats: FloatArray = FloatArray(MAX_QUADS * FLOATS_PER_VERTEX * 4 * 4)
    private var floatCount = 0
    var vertexCount = 0

    private val textureIndexCache = tileSet.getCache()

    private val textureEdgePad = 0.0015f
    var shadowTexturePadX = -0.0105f
    var shadowTexturePadY = -0.0062f
    private val quadEdgePadX = 0.0002f
    private val quadEdgePadY = -0.0024f
    private val shadowPad = -0.0001f

    private inline fun FloatArray.addVertex(x: Float, y: Float, tx: Float, ty: Float,
                                     lightR: Float, lightG: Float, lightB: Float, lightA: Float, grayOut: Float) {
        this[floatCount] = x
        this[floatCount+1] = y
        this[floatCount+2] = tx
        this[floatCount+3] = ty
        this[floatCount+4] = lightR
        this[floatCount+5] = lightG
        this[floatCount+6] = lightB
        this[floatCount+7] = lightA
        this[floatCount+8] = grayOut
        floatCount += FLOATS_PER_VERTEX
        vertexCount++
    }

    private val mesh = Mesh(
        true, MAX_QUADS * 6, 0,
        VertexAttribute(Usage.Position, 2, "a_Position"),
        VertexAttribute(Usage.TextureCoordinates, 2, "a_TexCoordinate"),
        VertexAttribute(Usage.ColorUnpacked, 4, "a_Light"),
        VertexAttribute(Usage.Generic, 1, "a_Grayout")
    )

    private val tileShader = ShaderProgram(vertexShaderSource, fragmentShaderSource).apply {
        if (!isCompiled) throw RuntimeException("Can't compile shader: $log")
    }


    fun clear() {
        floatCount = 0
        vertexCount = 0
    }

    fun addTileQuad(col: Int, row: Int, // global tile XY
                    textureIndex: Int, visibility: Float, light: LightColor,
                    offsetX: Float = 0f, offsetY: Float = 0f, scale: Double = 1.0, alpha: Float = 1f) {
        val scaleOffset = (1.0 - scale) * 0.5
        val x0 = Screen.tileXtoGlx(col + offsetX + scaleOffset)
        val y0 = Screen.tileYtoGly(row + offsetY + scaleOffset)
        val x1 = Screen.tileXtoGlx(col + offsetX + 1.0 - scaleOffset * 2.0)
        val y1 = Screen.tileYtoGly(row + offsetY + 1.0 - scaleOffset * 2.0)
        val lightR = min(visibility, light.r)
        val lightG = min(visibility, light.g)
        val lightB = min(visibility, light.b)
        val grayOut = if (visibility < 1f) 1f else 0f
        addQuad(x0, y0, x1, y1, 0f, 0f, 1f, 1f, textureIndex, lightR, lightG, lightB, alpha, grayOut)
    }

    fun addOverlapQuad(col: Int, row: Int, // global tile XY
                       edge: XY, textureIndex: Int, visibility: Float, light: LightColor) {
        var ox0 = col.toDouble()
        var oy0 = row.toDouble()
        var ox1 = col.toDouble() + 1.0
        var oy1 = row.toDouble() + 0.25
        var tx0 = 0f
        var ty0 = 0f
        var tx1 = 1f
        var ty1 = 0.25f
        when (edge) {
            SOUTH -> {
                oy0 = row.toDouble() + 0.75
                oy1 = row.toDouble() + 1.0
                ty0 = 0.75f
                ty1 = 1f
            }
            EAST -> {
                ox0 = col.toDouble() + 0.75
                oy1 = row.toDouble() + 1.0
                tx0 = 0.75f
                ty1 = 1f
            }
            WEST -> {
                ox1 = col.toDouble() + 0.25
                oy1 = row.toDouble() + 1.0
                tx1 = 0.25f
                ty1 = 1f
            }
        }
        val ix0 = Screen.tileXtoGlx(ox0)
        val iy0 = Screen.tileYtoGly(oy0)
        val ix1 = Screen.tileXtoGlx(ox1)
        val iy1 = Screen.tileYtoGly(oy1)

        val lightR = min(visibility, light.r)
        val lightG = min(visibility, light.g)
        val lightB = min(visibility, light.b)
        val grayOut = if (visibility < 1f) 1f else 0f
        addQuad(ix0, iy0, ix1, iy1, tx0, ty0, tx1, ty1, textureIndex, lightR, lightG, lightB, 1f, grayOut)
    }

    fun addOccludeQuad(col: Int, row: Int, // global tile XY
                       edge: XY, textureIndex: Int, visibility: Float, light: LightColor) {
        var ox0 = col.toDouble()
        var oy0 = row.toDouble()
        var ox1 = col.toDouble() + 1.0
        var oy1 = row.toDouble() + 0.25
        var tx0 = 0f
        var ty0 = 0.5f
        var tx1 = 1f
        var ty1 = 0.75f
        when (edge) {
            SOUTH -> {
                oy0 = row.toDouble() + 0.75
                oy1 = row.toDouble() + 1.0
                ty0 = 0.75f
                ty1 = 0.5f
            }
            EAST -> {
                ox0 = col.toDouble() + 0.75
                oy1 = row.toDouble() + 1.0
                tx0 = 0.75f
                tx1 = 0.5f
                ty0 = 0f
                ty1 = 1f
            }
            WEST -> {
                ox1 = col.toDouble() + 0.25
                oy1 = row.toDouble() + 1.0
                tx0 = 0.5f
                tx1 = 0.75f
                ty0 = 0f
                ty1 = 1f
            }
            NORTHEAST -> {
                ox0 = col.toDouble() + 0.75
                tx0 = 1f
                tx1 = 0.75f
                ty0 = 0.25f
                ty1 = 0f
            }
            NORTHWEST -> {
                ox1 = col.toDouble() + 0.25
                tx0 = 0.75f
                tx1 = 1f
                ty0 = 0.25f
                ty1 = 0f
            }
            SOUTHEAST -> {
                ox0 = col.toDouble() + 0.75
                oy0 = row.toDouble() + 0.75
                oy1 = row.toDouble() + 1.0
                tx0 = 1f
                tx1 = 0.75f
                ty0 = 0f
                ty1 = 0.25f
            }
            SOUTHWEST -> {
                oy0 = row.toDouble() + 0.75
                ox1 = col.toDouble() + 0.25
                oy1 = row.toDouble() + 1.0
                tx0 = 0.75f
                tx1 = 1f
                ty0 = 0f
                ty1 = 0.25f
            }
        }
        val ix0 = Screen.tileXtoGlx(ox0)
        val iy0 = Screen.tileYtoGly(oy0)
        val ix1 = Screen.tileXtoGlx(ox1)
        val iy1 = Screen.tileYtoGly(oy1)

        val lightR = min(visibility, light.r)
        val lightG = min(visibility, light.g)
        val lightB = min(visibility, light.b)
        val grayOut = if (visibility < 1f) 1f else 0f
        addQuad(ix0 - shadowPad, iy0 - shadowPad, ix1 + shadowPad, iy1 + shadowPad,
            tx0 + shadowTexturePadX, ty0 + shadowTexturePadY, tx1 - shadowTexturePadX, ty1 - shadowTexturePadY,
            textureIndex, lightR, lightG, lightB, 1f, grayOut)
    }

    fun addPixelQuad(x0: Int, y0: Int, x1: Int, y1: Int, // absolute screen pixel XY
                     textureIndex: Int, lightR: Float = 1f, lightG: Float = 1f, lightB: Float = 1f) {
        val glx0 = (x0 / Screen.width.toDouble()) * 2f - 1f
        val gly0 = (y0 / Screen.height.toDouble()) * 2f - 1f
        val glx1 = (x1 / Screen.width.toDouble()) * 2f - 1f
        val gly1 = (y1 / Screen.height.toDouble()) * 2f - 1f
        addQuad(glx0, gly0, glx1, gly1, 0f, 0f, 1f, 1f, textureIndex, lightR, lightG, lightB, 1f, 0f)
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

    private fun addQuad(ix0: Double, iy0: Double, ix1: Double, iy1: Double, // GL screen float XY
                        itx0: Float = 0f, ity0: Float = 0f, itx1: Float = 0f, ity1: Float = 0f,
                        textureIndex: Int, lightR: Float = 1f, lightG: Float = 1f, lightB: Float = 1f, lightA: Float = 1f, grayOut: Float = 0f
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
            addVertex(x0, y0, tx0, ty0, lightR, lightG, lightB, lightA, grayOut)
            addVertex(x0, y1, tx0, ty1, lightR, lightG, lightB, lightA, grayOut)
            addVertex(x1, y0, tx1, ty0, lightR, lightG, lightB, lightA, grayOut)
            addVertex(x1, y0, tx1, ty0, lightR, lightG, lightB, lightA, grayOut)
            addVertex(x0, y1, tx0, ty1, lightR, lightG, lightB, lightA, grayOut)
            addVertex(x1, y1, tx1, ty1, lightR, lightG, lightB, lightA, grayOut)
        }
    }

    fun getTextureIndex(glyph: Glyph, level: Level? = null, x: Int = 0, y: Int = 0): Int {
        return textureIndexCache[glyph] ?: tileSet.getIndex(glyph, level, x, y)
    }

    fun draw() {
        mesh.setVertices(floats, 0, floatCount)
        tileShader.use { shader ->
            Gdx.gl.glActiveTexture(GL_TEXTURE0)
            tileSet.texture.bind()
            shader.setUniformi("u_Texture", 0)
            mesh.render(shader, GL20.GL_TRIANGLES, 0, vertexCount)
        }
    }

    fun dispose() {
        mesh.dispose()
        tileSet.dispose()
        tileShader.dispose()
    }
}

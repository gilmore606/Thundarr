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
    val tileSet: TileSet,
    val isScrolling: Boolean = true
) {

    private val FLOATS_PER_VERTEX = 8
    private val MAX_QUADS = 40000

    private val floats: FloatArray = FloatArray(MAX_QUADS * FLOATS_PER_VERTEX * 4 * 4)
    private var floatCount = 0
    private var vertexCount = 0

    private val tilePad = 0.00004f
    private val shadowPad = 0.001f

    private inline fun FloatArray.addVertex(x: Float, y: Float, tx: Float, ty: Float,
                                     lightR: Float, lightG: Float, lightB: Float, grayOut: Float) {
        this[floatCount] = x
        this[floatCount+1] = y
        this[floatCount+2] = tx
        this[floatCount+3] = ty
        this[floatCount+4] = lightR
        this[floatCount+5] = lightG
        this[floatCount+6] = lightB
        this[floatCount+7] = grayOut
        floatCount += 8
        vertexCount++
    }

    private val mesh = Mesh(
        true, MAX_QUADS * 6, 0,
        VertexAttribute(Usage.Position, 2, "a_Position"),
        VertexAttribute(Usage.TextureCoordinates, 2, "a_TexCoordinate"),
        VertexAttribute(Usage.ColorUnpacked, 3, "a_Light"),
        VertexAttribute(Usage.Generic, 1, "a_Grayout")
    )

    private val tileShader = ShaderProgram(vertexShaderSource, fragmentShaderSource).apply {
        if (!isCompiled) throw RuntimeException("Can't compile shader: $log")
    }


    fun clear() {
        floatCount = 0
        vertexCount = 0
    }

    fun addTileQuad(col: Int, row: Int, stride: Double,
                    textureIndex: Int, visibility: Float, light: LightColor, aspectRatio: Double) {
        val x0 = (col.toDouble() * stride - (stride * 0.5)) / aspectRatio
        val y0 = row.toDouble() * stride - (stride * 0.5)
        val lightR = min(visibility, light.r)
        val lightG = min(visibility, light.g)
        val lightB = min(visibility, light.b)
        val grayOut = if (visibility < 1f) 1f else 0f
        addQuad(x0, y0, x0 + stride / aspectRatio, y0 + stride, 0f, 0f, 1f, 1f, textureIndex, lightR, lightG, lightB, grayOut)
    }

    fun addOverlapQuad(col: Int, row: Int, stride: Double, edge: XY,
                        textureIndex: Int, visibility: Float, light: LightColor, aspectRatio: Double) {
        var ix0 = (col.toDouble() * stride - (stride * 0.5)) / aspectRatio
        var iy0 = row.toDouble() * stride - (stride * 0.5)
        var ix1 = ix0 + stride / aspectRatio
        var iy1 = iy0 + stride * 0.25
        var tx0 = 0f
        var ty0 = 0f
        var tx1 = 1f
        var ty1 = 0.25f
        when (edge) {
            SOUTH -> {
                iy0 += stride * 0.75
                iy1 = iy0 + stride * 0.25
                ty0 = 0.75f
                ty1 = 1f
            }
            EAST -> {
                ix0 += stride * 0.75 / aspectRatio
                iy1 = iy0 + stride
                tx0 = 0.75f
                ty1 = 1f
            }
            WEST -> {
                ix1 = ix0 + stride * 0.25 / aspectRatio
                iy1 = iy0 + stride
                tx1 = 0.25f
                ty1 = 1f
            }
        }

        val lightR = min(visibility, light.r)
        val lightG = min(visibility, light.g)
        val lightB = min(visibility, light.b)
        val grayOut = if (visibility < 1f) 1f else 0f
        addQuad(ix0, iy0, ix1, iy1, tx0, ty0, tx1, ty1, textureIndex, lightR, lightG, lightB, grayOut)
    }

    fun addOccludeQuad(col: Int, row: Int, stride: Double, edge: XY,
                        textureIndex: Int, visibility: Float, light: LightColor, aspectRatio: Double) {
        var ix0 = (col.toDouble() * stride - (stride * 0.5)) / aspectRatio
        var iy0 = row.toDouble() * stride - (stride * 0.5)
        var ix1 = ix0 + stride / aspectRatio
        var iy1 = iy0 + stride * 0.25
        var tx0 = 0f
        var ty0 = 0.5f
        var tx1 = 1f
        var ty1 = 0.75f
        when (edge) {
            SOUTH -> {
                iy0 += stride * 0.75
                iy1 = iy0 + stride * 0.25
                ty0 = 0.75f
                ty1 = 0.5f
            }
            EAST -> {
                ix0 += stride * 0.75 / aspectRatio
                iy1 = iy0 + stride
                tx0 = 0.75f
                tx1 = 0.5f
                ty0 = 0f
                ty1 = 1f
            }
            WEST -> {
                ix1 = ix0 + stride * 0.25 / aspectRatio
                iy1 = iy0 + stride
                tx0 = 0.5f
                tx1 = 0.75f
                ty0 = 0f
                ty1 = 1f
            }
            NORTHEAST -> {
                ix0 += stride * 0.75 / aspectRatio
                tx0 = 1f
                tx1 = 0.75f
                ty0 = 0.25f
                ty1 = 0f
            }
            NORTHWEST -> {
                ix1 -= stride * 0.75 / aspectRatio
                tx0 = 0.75f
                tx1 = 1f
                ty0 = 0.25f
                ty1 = 0f
            }
            SOUTHEAST -> {
                ix0 += stride * 0.75 / aspectRatio
                iy0 += stride * 0.75
                iy1 += stride * 0.75
                tx0 = 1f
                tx1 = 0.75f
                ty0 = 0f
                ty1 = 0.25f
            }
            SOUTHWEST -> {
                iy0 += stride * 0.75
                iy1 += stride * 0.75
                ix1 -= stride * 0.75 / aspectRatio
                tx0 = 0.75f
                tx1 = 1f
                ty0 = 0f
                ty1 = 0.25f
            }
        }

        val lightR = min(visibility, light.r)
        val lightG = min(visibility, light.g)
        val lightB = min(visibility, light.b)
        val grayOut = if (visibility < 1f) 1f else 0f
        addQuad(ix0 - shadowPad, iy0 - shadowPad, ix1 + shadowPad, iy1 + shadowPad,
            tx0, ty0, tx1, ty1, textureIndex, lightR, lightG, lightB, grayOut)
    }

    fun addPixelQuad(x0: Int, y0: Int, x1: Int, y1: Int,
                     textureIndex: Int, lightR: Float = 1f, lightG: Float = 1f, lightB: Float = 1f) {
        val glx0 = (x0 / GameScreen.width.toDouble()) * 2f - 1f
        val gly0 = (y0 / GameScreen.height.toDouble()) * 2f - 1f
        val glx1 = (x1 / GameScreen.width.toDouble()) * 2f - 1f
        val gly1 = (y1 / GameScreen.height.toDouble()) * 2f - 1f
        addQuad(glx0, gly0, glx1, gly1, 0f, 0f, 1f, 1f, textureIndex, lightR, lightG, lightB)
    }

    private fun addQuad(ix0: Double, iy0: Double, ix1: Double, iy1: Double,
                        itx0: Float = 0f, ity0: Float = 0f, itx1: Float = 0f, ity1: Float = 0f,
                        textureIndex: Int, lightR: Float = 1f, lightG: Float = 1f, lightB: Float = 1f, grayOut: Float = 0f
    ) {
        val x0 = ix0.toFloat() - if (isScrolling) GameScreen.scrollX * GameScreen.zoom.toFloat() else 0f
        val y0 = -iy0.toFloat() + if (isScrolling) GameScreen.scrollY * GameScreen.zoom.toFloat() else 0f
        val x1 = ix1.toFloat() - if (isScrolling) GameScreen.scrollX * GameScreen.zoom.toFloat() else 0f
        val y1 = -iy1.toFloat() + if (isScrolling) GameScreen.scrollY * GameScreen.zoom.toFloat() else 0f
        val tx0 = (((textureIndex % tileSet.tilesPerRow) + itx0) * tileSet.tileRowStride).toFloat() + (tileSet.tilesPerRow * tilePad)
        val ty0 = (((textureIndex / tileSet.tilesPerRow) + ity0) * tileSet.tileColumnStride).toFloat() + (tileSet.tilesPerColumn * tilePad)
        val tx1 = (((textureIndex % tileSet.tilesPerRow) + itx1) * tileSet.tileRowStride).toFloat() - (tileSet.tilesPerRow * tilePad)
        val ty1 = (((textureIndex / tileSet.tilesPerRow) + ity1) * tileSet.tileColumnStride).toFloat() - (tileSet.tilesPerColumn * tilePad)
        floats.apply {
            addVertex(x0, y0, tx0, ty0, lightR, lightG, lightB, grayOut)
            addVertex(x0, y1, tx0, ty1, lightR, lightG, lightB, grayOut)
            addVertex(x1, y0, tx1, ty0, lightR, lightG, lightB, grayOut)
            addVertex(x1, y0, tx1, ty0, lightR, lightG, lightB, grayOut)
            addVertex(x0, y1, tx0, ty1, lightR, lightG, lightB, grayOut)
            addVertex(x1, y1, tx1, ty1, lightR, lightG, lightB, grayOut)
        }
    }

    fun getTextureIndex(glyph: Glyph, level: Level? = null, x: Int = 0, y: Int = 0) =
        tileSet.getIndex(glyph, level, x, y)

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

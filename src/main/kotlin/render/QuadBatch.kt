package render

import com.badlogic.gdx.Game
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
import util.LightColor
import world.Level
import java.lang.Float.min

class QuadBatch(
    vertexShaderSource: String,
    fragmentShaderSource: String,
    private val tileSet: TileSet,
    val isScrolling: Boolean = true
) {

    private val FLOATS_PER_VERTEX = 8
    private val MAX_QUADS = 40000

    private val floats: FloatArray = FloatArray(MAX_QUADS * FLOATS_PER_VERTEX * 4 * 4)
    private var floatCount = 0
    private var vertexCount = 0

    private val tilePad = 0.00001f

    private fun FloatArray.addVertex(x: Float, y: Float, tx: Float, ty: Float,
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
        addQuad(x0, y0, x0 + stride / aspectRatio, y0 + stride, textureIndex, lightR, lightG, lightB, grayOut)
    }

    fun addPixelQuad(x0: Int, y0: Int, x1: Int, y1: Int,
                     textureIndex: Int, lightR: Float = 1f, lightG: Float = 1f, lightB: Float = 1f) {
        val glx0 = (x0 / GameScreen.width.toDouble()) * 2f - 1f
        val gly0 = (y0 / GameScreen.height.toDouble()) * 2f - 1f
        val glx1 = (x1 / GameScreen.width.toDouble()) * 2f - 1f
        val gly1 = (y1 / GameScreen.height.toDouble()) * 2f - 1f
        addQuad(glx0, gly0, glx1, gly1, textureIndex, lightR, lightG, lightB)
    }

    private fun addQuad(ix0: Double, iy0: Double, ix1: Double, iy1: Double,
                        textureIndex: Int, lightR: Float = 1f, lightG: Float = 1f, lightB: Float = 1f,
                        grayOut: Float = 0f) {
        val x0 = ix0.toFloat() - if (isScrolling) GameScreen.scrollX * GameScreen.zoom.toFloat() else 0f
        val y0 = -iy0.toFloat() + if (isScrolling) GameScreen.scrollY * GameScreen.zoom.toFloat() else 0f
        val x1 = ix1.toFloat() - if (isScrolling) GameScreen.scrollX * GameScreen.zoom.toFloat() else 0f
        val y1 = -iy1.toFloat() + if (isScrolling) GameScreen.scrollY * GameScreen.zoom.toFloat() else 0f
        val tx0 = ((textureIndex % tileSet.tilesPerRow) * tileSet.tileRowStride).toFloat() + (tileSet.tilesPerRow * tilePad)
        val ty0 = ((textureIndex / tileSet.tilesPerRow) * tileSet.tileColumnStride).toFloat() + (tileSet.tilesPerColumn * tilePad)
        val tx1 = (((textureIndex % tileSet.tilesPerRow) + 1) * tileSet.tileRowStride).toFloat() - (tileSet.tilesPerRow * tilePad)
        val ty1 = (((textureIndex / tileSet.tilesPerRow) + 1) * tileSet.tileColumnStride).toFloat() - (tileSet.tilesPerColumn * tilePad)

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

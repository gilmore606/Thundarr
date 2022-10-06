package render

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.GL20.GL_TEXTURE0
import com.badlogic.gdx.graphics.Mesh
import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.graphics.VertexAttributes.*
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import ktx.graphics.use
import render.tilesets.TileSet
import java.lang.RuntimeException

class DrawList(
    vertexShaderSource: String,
    fragmentShaderSource: String,
    private val tileSet: TileSet
) {

    private val FLOATS_PER_VERTEX = 5
    private val MAX_QUADS = 20000

    private val floats: FloatArray = FloatArray(MAX_QUADS * FLOATS_PER_VERTEX * 4 * 4)
    private var floatCount = 0
    private var vertexCount = 0

    private fun FloatArray.addVertex(x: Float, y: Float, tx: Float, ty: Float, vis: Float) {
        this[floatCount] = x
        this[floatCount+1] = y
        this[floatCount+2] = tx
        this[floatCount+3] = ty
        this[floatCount+4] = vis
        floatCount += 5
        vertexCount++
    }

    private val mesh = Mesh(
        true, MAX_QUADS * 6, 0,
        VertexAttribute(Usage.Position, 2, "a_Position"),
        VertexAttribute(Usage.TextureCoordinates, 2, "a_TexCoordinate"),
        VertexAttribute(Usage.Generic, 1, "a_Visibility")
    )

    private val tileShader = ShaderProgram(vertexShaderSource, fragmentShaderSource).apply {
        if (!isCompiled) throw RuntimeException("Can't compile shader: $log")
    }


    fun clear() {
        floatCount = 0
        vertexCount = 0
    }

    fun addTileQuad(col: Int, row: Int, stride: Double,
                    textureIndex: Int, visibility: Float, aspectRatio: Double) {
        val x0 = (col.toDouble() * stride - (stride * 0.5)) / aspectRatio
        val y0 = row.toDouble() * stride - (stride * 0.5)
        addQuad(x0, y0, x0 + stride / aspectRatio, y0 + stride, textureIndex, visibility)
    }

    fun addQuad(ix0: Double, iy0: Double, ix1: Double, iy1: Double,
                textureIndex: Int, vis: Float) {
        val x0 = (ix0).toFloat()
        val y0 = (-iy0).toFloat()
        val x1 = (ix1).toFloat()
        val y1 = (-iy1).toFloat()
        val tx0 = (textureIndex % tileSet.tilesPerRow) * tileSet.tileRowStride
        val ty0 = (textureIndex / tileSet.tilesPerColumn) * tileSet.tileColumnStride
        val tx1 = tx0 + tileSet.tileRowStride
        val ty1 = ty0 + tileSet.tileColumnStride

        floats.apply {
            addVertex(x0, y0, tx0, ty0, vis)
            addVertex(x0, y1, tx0, ty1, vis)
            addVertex(x1, y0, tx1, ty0, vis)
            addVertex(x1, y0, tx1, ty0, vis)
            addVertex(x0, y1, tx0, ty1, vis)
            addVertex(x1, y1, tx1, ty1, vis)
        }
    }

    fun draw() {
        mesh.setVertices(floats, 0, floatCount)
        tileShader.use { shader ->
            Gdx.gl.glActiveTexture(GL_TEXTURE0)
            tileSet.texture.bind()
            shader.bind()
            shader.setUniformi("u_Texture", 0)
            mesh.render(shader, GL20.GL_TRIANGLES, 0, vertexCount)
        }
    }

}

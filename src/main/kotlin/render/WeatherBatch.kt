package render

import RESOURCE_FILE_DIR
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.GL20.GL_TEXTURE0
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import ktx.graphics.use
import render.shaders.weatherFragShader
import render.shaders.weatherVertShader
import util.log
import java.lang.RuntimeException

class WeatherBatch : RenderBatch {

    val TILE_SCALE = 120f

    private val FLOATS_PER_VERTEX = 5
    private val MAX_QUADS = 40000

    private val floats: FloatArray = FloatArray(MAX_QUADS * FLOATS_PER_VERTEX * 4 * 4)
    private var floatCount = 0
    var vertexCount = 0

    private val startTime = System.currentTimeMillis()

    private val texture = Texture(FileHandle("${RESOURCE_FILE_DIR}mask_clouds.png"), true).apply {
        setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
        log.info("Loaded weather texture mask_clouds.png ($width x $height)")
    }

    private val tileShader = ShaderProgram(weatherVertShader(), weatherFragShader()).apply {
        if (!isCompiled) throw RuntimeException("Can't compile weather shader: $log")
    }
    private val mesh = Mesh(
        true, MAX_QUADS * 6, 0,
        VertexAttribute(VertexAttributes.Usage.Position, 2, "a_Position"),
        VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_TexCoordinate"),
        VertexAttribute(VertexAttributes.Usage.Generic, 1, "a_Alpha")
    )

    private inline fun FloatArray.addVertex(x: Float, y: Float, tx: Float, ty: Float, alpha: Float) {
        this[floatCount] = x
        this[floatCount+1] = y
        this[floatCount+2] = tx
        this[floatCount+3] = ty
        this[floatCount+4] = alpha
        floatCount += FLOATS_PER_VERTEX
        vertexCount++
    }

    override fun clear() {
        floatCount = 0
        vertexCount = 0
    }

    fun addTileQuad(col: Int, row: Int, alpha: Float) {
        val x0 = Screen.tileXtoGlx(col.toDouble()).toFloat()
        val y0 = 0f - Screen.tileYtoGly(row.toDouble()).toFloat()
        val x1 = Screen.tileXtoGlx(col + 1.0).toFloat()
        val y1 = 0f - Screen.tileYtoGly(row + 1.0).toFloat()

        val windX = 0.6f
        val windY = 0.2f
        val timeshift = App.time + ((System.currentTimeMillis() - startTime) / 256f)
        val tcol = col.toFloat() + windX * timeshift
        val trow = row.toFloat() + windY * timeshift
        val tx0 = ((tcol % TILE_SCALE).toFloat() / TILE_SCALE)
        val ty0 = ((trow % TILE_SCALE).toFloat() / TILE_SCALE)
        val tx1 = tx0 + 1f / TILE_SCALE
        val ty1 = ty0 + 1f / TILE_SCALE
        floats.apply {
            addVertex(x0, y0, tx0, ty0, alpha)
            addVertex(x0, y1, tx0, ty1, alpha)
            addVertex(x1, y0, tx1, ty0, alpha)
            addVertex(x1, y0, tx1, ty0, alpha)
            addVertex(x0, y1, tx0, ty1, alpha)
            addVertex(x1, y1, tx1, ty1, alpha)
        }
    }

    override fun draw() {
        mesh.setVertices(floats, 0, floatCount)
        tileShader.use { shader ->
            Gdx.gl.glActiveTexture(GL_TEXTURE0)
            texture.bind()
            shader.setUniformi("u_Texture", 0)
            mesh.render(shader, GL20.GL_TRIANGLES, 0, vertexCount)
        }
    }

    override fun dispose() {
        mesh.dispose()
        tileShader.dispose()
        texture.dispose()
    }

}

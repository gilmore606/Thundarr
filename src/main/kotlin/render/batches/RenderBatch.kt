package render.batches

import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Mesh
import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import ktx.graphics.use

abstract class RenderBatch(
    maxQuads: Int = 150000
) {

    abstract fun vertexAttributes(): List<VertexAttribute>
    abstract fun vertShader(): String
    abstract fun fragShader(): String

    val floatsPerVertex = vertexAttributes().let {
        var fpv = 0
        it.forEach { fpv += it.numComponents }
        fpv
    }
    private val mesh = Mesh(true, maxQuads * 6, 0, *vertexAttributes().toTypedArray())
    val floats: FloatArray = FloatArray(maxQuads * floatsPerVertex * 4 * 4)
    protected val shader = ShaderProgram(vertShader(), fragShader()).apply {
        if (!isCompiled) throw RuntimeException("Can't compile shader: $log")
    }

    var floatCount = 0
    var vertexCount = 0

    fun clear() {
        floatCount = 0
        vertexCount = 0
    }

    fun draw() {
        mesh.setVertices(floats, 0, floatCount)
        shader.use { shader ->
            bindTextures()
            mesh.render(shader, GL20.GL_TRIANGLES, 0, vertexCount)
        }
    }

    open fun bindTextures() { }

    open fun dispose() {
        mesh.dispose()
        shader.dispose()
    }
}

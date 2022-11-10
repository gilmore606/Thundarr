package render.batches

import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.graphics.VertexAttributes
import render.Screen
import render.shaders.fireFragShader
import render.shaders.fireVertShader

class FireBatch : RenderBatch() {

    override fun vertShader() = fireVertShader()
    override fun fragShader() = fireFragShader()
    override fun vertexAttributes() = listOf(
        VertexAttribute(VertexAttributes.Usage.Position, 2, "a_Position"),
        VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_FireUV"),
        VertexAttribute(VertexAttributes.Usage.Generic, 1, "a_Offset")
    )

    val startTime = System.currentTimeMillis()

    inline fun FloatArray.addVertex(x: Float, y: Float, tx: Float, ty: Float, offset: Float) {
        this[floatCount] = x
        this[floatCount+1] = y
        this[floatCount+2] = tx
        this[floatCount+3] = ty
        this[floatCount+4] = offset
        floatCount += floatsPerVertex
        vertexCount++
    }

    inline fun addTileQuad(col: Int, row: Int, timeOffset: Float, offX: Float, offY: Float, size: Float) {

        val x0 = Screen.tileXtoGlx(col.toDouble() - (size - 1f) * 0.5f - 0.3f + offX).toFloat()
        val y0 = 0f - Screen.tileYtoGly(row.toDouble() - (size - 1f) * 1.4f - 0.7 + offY).toFloat()
        val x1 = Screen.tileXtoGlx(col.toDouble() + (size - 1f) * 0.5f + 1.3 + offX).toFloat()
        val y1 = 0f - Screen.tileYtoGly(row.toDouble() + 1.0 + offY).toFloat()

        floats.apply {
            addVertex(x0, y0, 0f, 0f, timeOffset)
            addVertex(x0, y1, 0f, 1f, timeOffset)
            addVertex(x1, y0, 1f, 0f, timeOffset)
            addVertex(x1, y0, 1f, 0f, timeOffset)
            addVertex(x0, y1, 0f, 1f, timeOffset)
            addVertex(x1, y1, 1f, 1f, timeOffset)
        }
    }

    override fun bindTextures() {
        shader.setUniformf("u_Time", ((Screen.timeMs - startTime) % 10000L).toFloat())
    }
}

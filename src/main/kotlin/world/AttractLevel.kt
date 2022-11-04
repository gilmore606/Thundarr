package world

import java.lang.Integer.max
import java.lang.Integer.min

class AttractLevel() : EnclosedLevel("attract") {

    companion object {
        val dimension = 120
    }
    val povBufferX = 60
    val povBufferY = 40

    override fun timeScale() = 5f

    override fun setPov(x: Int, y: Int) {
        val cx = max(povBufferX, min(x, dimension - povBufferX))
        val cy = max(povBufferY, min(y, dimension - povBufferY))
        super.setPov(cx, cy)
    }

    override fun updateVisibility() {
        for (cx in 0 until width) {
            for (cy in 0 until height) {
                chunk?.setTileVisibility(cx, cy, true)
            }
        }
    }

}

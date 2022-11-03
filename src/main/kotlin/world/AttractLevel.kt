package world

import java.lang.Integer.max
import java.lang.Integer.min

class AttractLevel() : EnclosedLevel("attract") {

    companion object {
        val dimension = 120
    }
    val povBuffer = 40

    override fun timeScale() = 5f

    override fun setPov(x: Int, y: Int) {
        val cx = max(povBuffer, min(x, dimension - povBuffer))
        val cy = max(povBuffer, min(y, dimension - povBuffer))
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

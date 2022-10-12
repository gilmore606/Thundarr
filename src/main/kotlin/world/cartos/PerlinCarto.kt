package world.cartos

import util.Perlin
import util.log

object PerlinCarto : Carto() {

    val scale = 0.06
    val fullness = 0.12

    override fun doCarveLevel() {
        forEachCell { x, y ->
            val n = Perlin.noise(x.toDouble() * scale, y.toDouble() * scale, 56.0)
            log.info("$x $y = $n")
            if (n > fullness * scale) {
                carve(x, y, 0)
            }
        }
    }
}

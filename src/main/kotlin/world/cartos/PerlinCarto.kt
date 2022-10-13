package world.cartos

import util.Perlin

object PerlinCarto : Carto() {

    val scale = 0.05
    val fullness = 0.02

    override fun doCarveLevel() {
        forEachCell { x, y ->
            val n = Perlin.noise(x.toDouble() * scale, y.toDouble() * scale, 59.0)
            if (n > fullness * scale) {
                carve(x, y, 0)
            }
        }
    }
}

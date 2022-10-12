package world.cartos

import util.Perlin

object PerlinCarto : Carto() {

    val scale = 0.06
    val fullness = 0.04

    override fun doCarveLevel() {
        forEachCell { x, y ->
            val n = Perlin.noise(x.toDouble() * scale, y.toDouble() * scale, 54.0)
            if (n > fullness * scale) {
                carve(x, y, 0)
            }
        }
    }
}

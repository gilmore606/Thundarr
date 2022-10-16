package world.cartos

import util.Perlin

class WorldCarto : Carto() {

    val scale = 0.02
    val fullness = 0.002

    override fun doCarveLevel() {
        forEachCell { x, y ->
            val n = Perlin.noise(x.toDouble() * scale, y.toDouble() * scale, 59.0)
            if (n > fullness * scale) {
                carve(x, y, 0)
            }
        }
    }
}

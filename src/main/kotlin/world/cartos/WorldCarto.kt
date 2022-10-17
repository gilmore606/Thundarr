package world.cartos

import render.tilesets.Glyph
import things.Thing
import util.Dice
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

        for (x in 0 until width) {
            for (y in 0 until height) {
                if (isWalkableAt(x + this.x0, y + this.y0)) {
                    val n = Perlin.noise(x * 0.04, y * 0.04, 0.01)
                    if (Dice.chance(n.toFloat() * 2.5f)) {
                        addThingAt(x + this.x0, y + this.y0, Thing(
                            Glyph.TREE,
                            true, false
                        )
                        )
                    }
                }
            }
        }
    }
}

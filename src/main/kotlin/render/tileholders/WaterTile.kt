package render.tileholders

import render.tilesets.TileSet
import world.level.Level
import kotlin.random.Random

class WaterTile(
    set: TileSet
): TileHolder(set)  {

    private val variants = ArrayList<Triple<Float, Int, Int>>()

    fun add(frequency: Float, tx: Int, ty: Int) {
        variants.add(Triple(frequency, tx, ty))
    }

    override fun getTextureIndex(level: Level?, x: Int, y: Int): Int {
        val seed = x * 10 + y + (System.currentTimeMillis() / (400 + Random(x*10+y).nextInt(200))).toInt()
        val dice = Random(seed).nextFloat()
        var chance = 0f
        variants.forEach { v ->
            chance += v.first
            if (dice <= chance) return v.third * set.tilesPerRow + v.second
        }
        return 0
    }
}

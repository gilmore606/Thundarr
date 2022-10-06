package render.tileholders

import kotlin.random.Random
import world.Level
import render.tilesets.TileSet

abstract class TileHolder(
    val set: TileSet
) {

    abstract fun getTextureIndex(
        level: Level? = null,
        x: Int = 0,
        y: Int = 0
    ): Int

    protected fun pickIndexFromVariants(bucket: ArrayList<Triple<Float, Int, Int>>, seed: Int): Int {
        val dice = Random(seed).nextFloat()
        var chance = 0f
        bucket.forEach { v ->
            chance += v.first
            if (dice <= chance) return v.third * set.tilesPerRow + v.second
        }
        return 0
    }

}

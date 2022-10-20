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

    protected fun pickIndexFromVariants(random: Int, bucket: ArrayList<Triple<Float, Int, Int>>): Int {
        val dice = (random % 1000).toFloat() / 1000f
        var chance = 0f
        bucket.forEach { v ->
            chance += v.first
            if (dice <= chance) return v.third * set.tilesPerRow + v.second
        }
        return 0
    }

}

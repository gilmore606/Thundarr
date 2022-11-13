package render.tileholders

import world.level.Level
import render.tilesets.TileSet

abstract class TileHolder(
    val set: TileSet
) {

    open val cacheable: Boolean = true

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
            if (dice <= chance) return indexFromCoords(v.second, v.third)
        }
        return indexFromCoords(bucket.first().second, bucket.first().third)
    }

    protected fun indexFromCoords(tx: Int, ty: Int) = ty * set.tilesPerRow + tx

}

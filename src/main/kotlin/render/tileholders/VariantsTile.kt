package render.tileholders

import world.Level
import render.tilesets.TileSet
import kotlin.random.Random

open class VariantsTile(
    set: TileSet
): TileHolder(set) {

    private val variants = ArrayList<Triple<Float, Int, Int>>()

    fun add(frequency: Float, tx: Int, ty: Int) {
        variants.add(Triple(frequency, tx, ty))
    }

    override fun getTextureIndex(level: Level?, x: Int, y: Int): Int {
        level?.also { level ->
            return pickIndexFromVariants(level.getRandom(x,y), variants)
        }
        return 0
    }

}

package render.tileholders

import render.tilesets.Glyph
import render.tilesets.TileSet
import world.level.Level

class HighwayTile(
    set: TileSet
): TileHolder(set) {

    var isHorizontal = false

    enum class Slot { STRIPED, PLAIN }

    private val variants = HashMap<Slot, ArrayList<Triple<Float, Int, Int>>>()

    fun add(slot: Slot, frequency: Float, tx: Int, ty: Int) {
        variants[slot]?.also {
            it.add(Triple(frequency, tx, ty))
        } ?: run {
            variants[slot] = ArrayList<Triple<Float, Int, Int>>().apply { add(Triple(frequency, tx, ty)) }
        }
    }

    override fun getTextureIndex(level: Level?, x: Int, y: Int): Int {
        val bucket = when {
            isHorizontal && (y % 2 == 0) -> Slot.STRIPED
            !isHorizontal && (x % 2 == 0) -> Slot.STRIPED
            else -> Slot.PLAIN
        }
        return pickIndexFromVariants(level?.getRandom(x,y) ?: 0, variants[bucket]!!)
    }
}

package render.tileholders

import world.Level
import render.tilesets.TileSet
import render.tilesets.Glyph

class WallTile(
    set: TileSet,
    val neighborType: Glyph,
): TileHolder(set) {

    enum class Slot { TOP, LEFT, LEFTBOTTOM, BOTTOM, RIGHTBOTTOM, RIGHT }

    private val variants = HashMap<Slot, ArrayList<Triple<Float, Int, Int>>>()

    fun add(slot: Slot, frequency: Float, tx: Int, ty: Int) {
        variants[slot]?.also {
            it.add(Triple(frequency, tx, ty))
        } ?: run {
            variants[slot] = ArrayList<Triple<Float, Int, Int>>().apply { add(Triple(frequency, tx, ty)) }
        }
    }
    override fun getTextureIndex(level: Level?, x: Int, y: Int): Int {
        level?.also { level ->
            val bucket = if (!neighborAt(level, x, y + 1)) {
                variants[Slot.TOP]
            } else if (!neighborAt(level, x, y - 1)) {
                variants[Slot.BOTTOM]
            } else if (!neighborAt(level, x - 1, y)) {
                variants[Slot.RIGHT]
            } else if (!neighborAt(level, x + 1, y)) {
                variants[Slot.LEFT]
            } else if (visibleOtherAt(level, x + 1, y + 1)) {
                variants[Slot.LEFT]
            } else if (visibleOtherAt(level, x - 1, y + 1)) {
                variants[Slot.RIGHT]
            } else if (visibleOtherAt(level, x + 1, y -1)) {
                variants[Slot.LEFTBOTTOM]
            } else if (visibleOtherAt(level, x - 1, y -1)) {
                variants[Slot.RIGHTBOTTOM]
            } else {
                variants[Slot.TOP]
            }
            return pickIndexFromVariants(bucket!!, x * 10 + y)
        }
        return 0
    }

    private fun neighborAt(level: Level, x: Int, y: Int) = level.getGlyph(x, y) == neighborType
    private fun visibleAt(level: Level, x: Int, y: Int) = level.visibilityAt(x, y) == 1f
    private fun visibleOtherAt(level: Level, x: Int, y: Int) = !neighborAt(level, x, y) && visibleAt(level, x, y)
}

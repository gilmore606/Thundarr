package render.tileholders

import world.Level
import render.tilesets.TileSet
import render.tilesets.Glyph

class WallTile(
    set: TileSet,
    val neighborType: Glyph,
): TileHolder(set) {

    enum class Slot { TOP, LEFT, LEFTBOTTOM, BOTTOM, RIGHTBOTTOM, RIGHT, FULL }

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
            var bucket: ArrayList<Triple<Float,Int,Int>>? = null
            if (!visibleAt(level, x, y)) {
                bucket = variants[Slot.FULL]
            } else {
                if (!visibleAt(level, x, y - 1)) {
                    bucket = variants[Slot.TOP]
                } else if (!visibleAt(level, x, y + 1)) {
                    bucket = variants[Slot.BOTTOM]
                } else if (!visibleAt(level, x - 1, y)) {
                    bucket = if (visibleNeighborAt(level, x, y - 1) && visibleNeighborAt(level, x, y + 1)) {
                        variants[Slot.LEFT]
                    } else variants[Slot.FULL]
                } else if (!visibleAt(level, x + 1, y)) {
                    bucket = if (visibleNeighborAt(level, x, y - 1) && visibleNeighborAt(level, x, y + 1)) {
                        variants[Slot.RIGHT]
                    } else variants[Slot.FULL]
                } else if (visibleNeighborAt(level, x, y - 1)) {
                    bucket = variants[Slot.FULL]
                } else {
                    bucket = variants[Slot.TOP]
                }
            }
            return pickIndexFromVariants(bucket!!, x * 10 + y)
        }
        return 0
    }

    private fun visibleNeighborAt(level: Level, x: Int, y: Int) = level.getGlyph(x, y) == neighborType && visibleAt(level,x,y)
    private fun visibleAt(level: Level, x: Int, y: Int) = level.visibilityAt(x, y) == 1f
    private fun visibleOtherAt(level: Level, x: Int, y: Int) = !visibleNeighborAt(level, x, y) && visibleAt(level, x, y)
}

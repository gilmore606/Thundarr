package render.tileholders

import world.Level
import render.tilesets.TileSet
import render.tilesets.Glyph
import util.*

class WallTile(
    set: TileSet,
    val neighborType: Glyph,
): TileHolder(set) {

    enum class Slot { TOP, LEFT, LEFTBOTTOM, BOTTOM, RIGHTBOTTOM, RIGHT, OUTSIDE_LEFTBOTTOM, OUTSIDE_RIGHTBOTTOM, FULL }

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
            var bucket = variants[Slot.FULL]
            if (visibleAt(level, x, y)) {
                if (!visibleTo(level, x, y, EAST)) {
                    if (visibleTo(level, x, y, SOUTH)) {
                        if (openTo(level, x, y, NORTH)) {
                            if (neighborTo(level,x,y,SOUTH)) {
                                bucket = variants[Slot.OUTSIDE_RIGHTBOTTOM]
                            } else {
                                bucket = variants[Slot.TOP]
                            }
                        } else if (!neighborTo(level,x,y,SOUTH)) {
                            bucket = variants[Slot.TOP]
                        } else {
                            bucket = variants[Slot.RIGHT]
                        }
                    } else {
                        bucket = variants[Slot.RIGHTBOTTOM]
                    }
                } else if (!visibleTo(level, x, y, WEST)) {
                    if (visibleTo(level, x, y, SOUTH)) {
                        if (openTo(level, x, y, NORTH)) {
                            if (neighborTo(level,x,y,SOUTH)) {
                                bucket = variants[Slot.OUTSIDE_LEFTBOTTOM]
                            } else {
                                bucket = variants[Slot.TOP]
                            }
                        } else if (!neighborTo(level,x,y,SOUTH)) {
                            bucket = variants[Slot.TOP]
                        } else {
                            bucket = variants[Slot.LEFT]
                        }
                    } else {
                        bucket = variants[Slot.LEFTBOTTOM]
                    }
                } else if (!visibleTo(level, x, y, NORTH)) {
                    bucket = variants[Slot.TOP]
                } else if (!visibleTo(level, x, y, SOUTH)) {
                    bucket = variants[Slot.BOTTOM]
                } else if (!visibleTo(level, x, y, SOUTHEAST) && neighborTo(level, x, y, EAST) && neighborTo(level, x, y, SOUTH)) {
                    bucket = variants[Slot.OUTSIDE_RIGHTBOTTOM]
                } else if (!visibleTo(level, x, y, SOUTHWEST) && neighborTo(level, x, y, WEST) && neighborTo(level, x, y, SOUTH)) {
                    bucket = variants[Slot.OUTSIDE_LEFTBOTTOM]
                } else if (neighborTo(level,x,y,NORTH) && !neighborTo(level,x,y,SOUTH) && (!visibleTo(level,x,y,NORTHEAST) || !visibleTo(level,x,y,NORTHWEST))) {
                    bucket = variants[Slot.TOP]
                }
            }
            return pickIndexFromVariants(bucket!!, x * 10 + y)
        }
        return 0
    }

    private fun neighborAt(level: Level, x: Int, y: Int) = level.getGlyph(x, y) == neighborType
    private fun visibleAt(level: Level, x: Int, y: Int) = level.visibilityAt(x, y) > 0f
    private fun visibleTo(level: Level, x: Int, y: Int, dir: XY) = level.visibilityAt(x + dir.x, y + dir.y) > 0f
    private fun neighborTo(level: Level, x: Int, y: Int, dir: XY) = neighborAt(level, x + dir.x, y + dir.y)
    private fun openTo(level: Level, x: Int, y: Int, dir: XY) = visibleAt(level,x+dir.x,y+dir.y) && level.getGlyph(x+dir.x,y+dir.y) != neighborType
}

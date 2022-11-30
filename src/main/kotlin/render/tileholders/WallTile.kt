package render.tileholders

import world.level.Level
import render.tilesets.TileSet
import render.tilesets.Glyph
import util.*

class WallTile(
    set: TileSet,
    val neighborType: Glyph,
): TileHolder(set) {

    enum class Slot { TOP, LEFT, LEFTBOTTOM, BOTTOM, RIGHTBOTTOM, RIGHT,
        OUTSIDE_LEFTBOTTOM, OUTSIDE_RIGHTBOTTOM, LEFTTOP, RIGHTTOP, OUTSIDE_LEFTTOP, OUTSIDE_RIGHTTOP,
        CAP_TOP, CAP_BOTTOM, CAP_LEFT, CAP_RIGHT, FULL }

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
                val nWest = neighborTo(level,x,y,WEST)
                val nEast = neighborTo(level,x,y,EAST)
                val nNorth = neighborTo(level,x,y,NORTH)
                val nSouth = neighborTo(level,x,y,SOUTH)
                val vWest = visibleTo(level,x,y,WEST)
                val vEast = visibleTo(level,x,y,EAST)
                val vNorth = visibleTo(level,x,y,NORTH)
                val vSouth = visibleTo(level,x,y,SOUTH)
                if (!nWest && !nEast && !nSouth && nNorth) {
                    bucket = variants[Slot.CAP_BOTTOM]
                } else if (!nWest && !nEast && !nNorth && nSouth) {
                    bucket = variants[Slot.CAP_TOP]
                } else if (!nWest && !nSouth && !nNorth && nEast) {
                    bucket = variants[Slot.CAP_LEFT]
                } else if (!nEast && !nSouth && !nNorth && nWest) {
                    bucket = variants[Slot.CAP_RIGHT]
                } else if (nNorth && !nSouth && nEast && !nWest && vSouth) {
                    bucket = variants[Slot.LEFTTOP]
                } else if (nNorth && !nSouth && !nEast && nWest && vSouth) {
                    bucket = variants[Slot.RIGHTTOP]
                } else if (!vEast) {
                    if (vSouth) {
                        if (openTo(level, x, y, NORTH)) {
                            if (nNorth) {
                                bucket = variants[Slot.OUTSIDE_RIGHTBOTTOM]
                            } else {
                                bucket = variants[Slot.TOP]
                            }
                        } else if (!nSouth) {
                            if (nNorth) {
                                bucket = variants[Slot.LEFTTOP]
                            } else {
                                bucket = variants[Slot.TOP]
                            }
                        } else if (nWest) {
                            bucket = variants[Slot.OUTSIDE_RIGHTTOP]
                        } else {
                            bucket = variants[Slot.RIGHT]
                        }
                    } else {
                        bucket = variants[Slot.RIGHTBOTTOM]
                    }
                } else if (!vWest) {
                    if (vSouth) {
                        if (openTo(level, x, y, NORTH)) {
                            if (nSouth) {
                                bucket = variants[Slot.OUTSIDE_LEFTBOTTOM]
                            } else {
                                bucket = variants[Slot.TOP]
                            }
                        } else if (!nSouth) {
                            if (nNorth) {
                                bucket = variants[Slot.RIGHTTOP]
                            } else {
                                bucket = variants[Slot.TOP]
                            }
                        } else if (nEast) {
                            bucket = variants[Slot.OUTSIDE_LEFTTOP]
                        } else {
                            bucket = variants[Slot.LEFT]
                        }
                    } else {
                        bucket = variants[Slot.LEFTBOTTOM]
                    }
                } else if (!vNorth) {
                    bucket = variants[Slot.TOP]
                } else if (!vSouth) {
                    bucket = variants[Slot.BOTTOM]
                } else if (!visibleTo(level, x, y, SOUTHEAST) && nEast && nSouth) {
                    bucket = variants[Slot.OUTSIDE_RIGHTBOTTOM]
                } else if (!visibleTo(level, x, y, SOUTHWEST) && nWest && nSouth) {
                    bucket = variants[Slot.OUTSIDE_LEFTBOTTOM]
                } else if (!visibleTo(level, x, y, NORTHEAST) && nNorth && nEast) {
                    bucket = variants[Slot.LEFTTOP]
                } else if (!visibleTo(level, x, y, NORTHWEST) && nNorth && nWest) {
                    bucket = variants[Slot.RIGHTTOP]
                } else if (nNorth && !nSouth && (!visibleTo(level,x,y,NORTHEAST) || !visibleTo(level,x,y,NORTHWEST))) {
                    bucket = variants[Slot.TOP]
                }
            }
            return pickIndexFromVariants(level.getRandom(x,y), bucket!!)
        }
        return 0
    }

    private fun neighborAt(level: Level, x: Int, y: Int) = level.getGlyph(x, y) == neighborType
    private fun visibleAt(level: Level, x: Int, y: Int) = level.visibilityAt(x, y) == 1f
    private fun visibleTo(level: Level, x: Int, y: Int, dir: XY) = level.visibilityAt(x + dir.x, y + dir.y) == 1f
    private fun neighborTo(level: Level, x: Int, y: Int, dir: XY) = neighborAt(level, x + dir.x, y + dir.y)
    private fun openTo(level: Level, x: Int, y: Int, dir: XY) = visibleAt(level,x+dir.x,y+dir.y) && level.getGlyph(x+dir.x,y+dir.y) != neighborType
}

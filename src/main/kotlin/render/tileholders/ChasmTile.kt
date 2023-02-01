package render.tileholders

import render.tilesets.Glyph
import render.tilesets.TileSet
import util.*
import world.level.Level

class ChasmTile(
    set: TileSet,
    val neighborType: Glyph,
): TileHolder(set) {

    enum class Slot { SOLO, LEFTCAP, RIGHTCAP, TOPLEFT, TOP, TOPRIGHT, LEFT, MIDDLE, RIGHT, BOTTOMLEFT, BOTTOM, BOTTOMRIGHT, TOPCAP, BOTTOMCAP, VERTICAL, HORIZONTAL }

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
            var bucket = variants[Slot.SOLO]
            val nWest = neighborTo(level,x,y, WEST)
            val nEast = neighborTo(level,x,y, EAST)
            val nNorth = neighborTo(level,x,y, NORTH)
            val nSouth = neighborTo(level,x,y, SOUTH)
            if (nWest && nEast && nNorth && nSouth) {
                bucket = variants[Slot.MIDDLE]
            } else if (nEast && !nWest && !nNorth && !nSouth) {
                bucket = variants[Slot.LEFTCAP]
            } else if (nWest && !nEast && !nNorth && !nSouth) {
                bucket = variants[Slot.RIGHTCAP]
            } else if (nNorth && !nSouth && !nWest && !nEast) {
                bucket = variants[Slot.BOTTOMCAP]
            } else if (nSouth && !nNorth && !nWest && !nEast) {
                bucket = variants[Slot.TOPCAP]
            } else if (nNorth && nEast && !nWest && !nSouth) {
                bucket = variants[Slot.BOTTOMLEFT]
            } else if (nNorth && nEast && nWest && !nSouth) {
                bucket = variants[Slot.BOTTOM]
            } else if (nNorth && nWest && !nEast && !nSouth) {
                bucket = variants[Slot.BOTTOMRIGHT]
            } else if (nNorth && nSouth && nEast && !nWest) {
                bucket = variants[Slot.LEFT]
            } else if (!nNorth && nSouth && nEast && !nWest) {
                bucket = variants[Slot.TOPLEFT]
            } else if (!nNorth && nSouth && !nEast && nWest) {
                bucket = variants[Slot.TOPRIGHT]
            } else if (!nNorth && nSouth && nEast && nWest) {
                bucket = variants[Slot.TOP]
            } else if (nNorth && nSouth && !nEast && nWest) {
                bucket = variants[Slot.RIGHT]
            } else if (nNorth && nSouth && !nEast && !nWest) {
                bucket = variants[Slot.VERTICAL]
            } else if (!nNorth && !nSouth && nEast && nWest) {
                bucket = variants[Slot.HORIZONTAL]
            }
            return pickIndexFromVariants(level.getRandom(x,y), bucket!!)
        }
        return 0
    }

    protected fun neighborAt(level: Level, x: Int, y: Int) = level.getGlyph(x, y) == neighborType
    protected fun visibleAt(level: Level, x: Int, y: Int) = level.visibilityAt(x, y) == 1f
    protected fun visibleTo(level: Level, x: Int, y: Int, dir: XY) = level.visibilityAt(x + dir.x, y + dir.y) == 1f
    protected fun neighborTo(level: Level, x: Int, y: Int, dir: XY) = neighborAt(level, x + dir.x, y + dir.y)
    protected fun openTo(level: Level, x: Int, y: Int, dir: XY) = visibleAt(level,x+dir.x,y+dir.y) && level.getGlyph(x+dir.x,y+dir.y) != neighborType

}

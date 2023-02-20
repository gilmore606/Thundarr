package render.tileholders

import render.tilesets.TileSet
import things.Thing
import util.*
import world.level.Level

class TableTile(
    set: TileSet,
    private val neighborType: Thing.Tag
): TileHolder(set) {

    enum class Slot { SINGLE, LEFT, RIGHT, MIDDLE, UPPER }

    private val variants = HashMap<Slot, XY>()

    fun add(slot: Slot, tx: Int, ty: Int) {
        variants[slot] = XY(tx,ty)
    }

    override fun getTextureIndex(level: Level?, x: Int, y: Int): Int {
        var v = variants[Slot.SINGLE]
        level?.also { level ->
            v = if (neighborTo(level, x, y, SOUTH)) {
                variants[Slot.UPPER]
            } else if (neighborTo(level, x, y, WEST)) {
                if (neighborTo(level, x, y, EAST)) {
                    variants[Slot.MIDDLE]
                } else {
                    variants[Slot.RIGHT]
                }
            } else if (neighborTo(level, x, y, EAST)) {
                variants[Slot.LEFT]
            } else {
                variants[Slot.SINGLE]
            }
        }
        return indexFromCoords(v!!.x, v!!.y)
    }

    private fun neighborAt(level: Level, x: Int, y: Int) = level.thingsAt(x,y).hasOneWhere { it.tag == neighborType }
    private fun neighborTo(level: Level, x: Int, y: Int, dir: XY) = neighborAt(level, x + dir.x, y + dir.y)
}

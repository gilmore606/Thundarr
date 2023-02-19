package world.gen.decors

import things.Table
import things.Thing
import util.Dice
import util.Rect
import util.XY
import world.gen.cartos.Carto

open class Decor {

    data class Room(
        val rect: Rect,
        val clearCells: List<XY>,
    ) {
        val width = rect.x1 - rect.x0
        val height = rect.y1 - rect.y0
        val clearAt = Array(width) { Array(height) { true } }
        init {
            clearCells.forEach {
                val lx = it.x - rect.x0
                val ly = it.y - rect.y0
                if (lx >= 0 && ly >= 0 && lx < width && ly < height) clearAt[lx][ly] = false
            }
        }

        fun unclear(loc: XY) {
            clearAt[loc.x-rect.x0][loc.y-rect.y0] = false
        }

        fun clearCount(): Int {
            var c = 0
            clearAt.forEach { it.forEach { if (it) c++ }}
            return c
        }

        fun randomClear(): XY? {
            val clearCount = clearCount()
            if (clearCount < 1) return null
            val i = Dice.zeroTil(clearCount)
            var j = 0
            for (ix in 0 until width) {
                for (iy in 0 until height) {
                    if (clearAt[ix][iy]) {
                        if (j >= i) return XY(rect.x0+ix, rect.y0+iy)
                        j++
                    }
                }
            }
            return null
        }
    }

    open fun furniture(): Thing = Table()

    open fun furnish(room: Room, carto: Carto) {
        repeat ((room.clearCount() * 0.12).toInt()) {
            room.randomClear()?.also {
                carto.spawnThing(it.x, it.y, furniture())
                room.unclear(it)
            }
        }

    }
}

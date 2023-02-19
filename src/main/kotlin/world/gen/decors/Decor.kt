package world.gen.decors

import things.Thing
import util.Dice
import util.Rect
import util.XY
import world.gen.cartos.Carto

abstract class Decor {

    data class Room(
        val rect: Rect,
        val forbiddenCells: List<XY>,
    ) {
        val width = rect.x1 - rect.x0 + 1
        val height = rect.y1 - rect.y0 + 1
        val clearAt = Array(width) { Array(height) { true } }
        val clears = mutableListOf<XY>()
        init {
            for (ix in 0 until width) {
                for (iy in 0 until height) {
                    val cell = XY(rect.x0 + ix,rect.y0 + iy)
                    if (!forbiddenCells.contains(cell)) clears.add(cell)
                }
            }
            forbiddenCells.forEach {
                val lx = it.x - rect.x0
                val ly = it.y - rect.y0
                if (lx >= 0 && ly >= 0 && lx < width && ly < height) clearAt[lx][ly] = false
            }
        }

        fun unclear(loc: XY) {
            clearAt[loc.x-rect.x0][loc.y-rect.y0] = false
            clears.remove(loc)
        }
        fun unclearAround(loc: XY) {
            for (ix in -1..1) {
                for (iy in -1..1) {
                    val lx = loc.x + ix - rect.x0
                    val ly = loc.y + iy - rect.y0
                    if (lx >= 0 && ly >= 0 && lx < width && ly < height) {
                        clearAt[lx][ly] = false
                        clears.remove(XY(lx + rect.x0, ly + rect.y0))
                    }
                }
            }
        }
    }

    private lateinit var carto: Carto
    private lateinit var room: Room
    private var cell = XY(0,0)

    fun furnish(room: Room, carto: Carto) {
        this.carto = carto
        this.room = room
        doFurnish()
    }

    abstract fun doFurnish()

    protected fun chance(chance: Float, doThis: ()->Unit) {
        if (Dice.chance(chance)) doThis()
    }

    protected fun spawn(thing: Thing) {
        carto.spawnThing(cell.x, cell.y, thing)
        room.unclear(cell)
    }

    protected fun clearAround() {
        room.unclearAround(cell)
    }

    protected fun againstWall(doThis: ()->Unit) {
        val walls = room.clears.filter {
            it.x == room.rect.x0 || it.y == room.rect.y0 || it.x == room.rect.x1 || it.y == room.rect.y1
        }
        if (walls.isEmpty()) return
        cell = walls.random()
        doThis()
    }

    protected fun awayFromWall(doThis: ()->Unit) {
        val nonwalls = room.clears.filter {
            it.x != room.rect.x0 && it.y != room.rect.y0 && it.x != room.rect.x1 && it.y != room.rect.y1
        }
        if (nonwalls.isEmpty()) return
        cell = nonwalls.random()
        doThis()
    }

    protected fun anyEmpty(doThis: ()->Unit) {
        if (room.clears.isEmpty()) return
        cell = room.clears.random()
        doThis()
    }

    protected fun forEachClear(doThis: (x: Int, y: Int)->Unit) {
        for (ix in room.rect.x0..room.rect.x1) {
            for (iy in room.rect.y0..room.rect.y1) {
                if (room.clearAt[ix-room.rect.x0][iy-room.rect.y0]) {
                    cell = XY(ix, iy)
                    doThis(ix, iy)
                }
            }
        }
    }

    protected val clearCount: Int
        get() = room.clears.size

}

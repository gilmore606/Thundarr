package world.gen.decors

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import things.Thing
import ui.panels.Console
import util.Dice
import util.Rect
import util.XY
import world.ChunkMeta
import world.gen.cartos.Carto
import world.gen.cartos.WorldCarto
import world.terrains.Terrain

@Serializable
sealed class Decor {

    @Serializable
    data class Room(
        val rect: Rect,
        val forbiddenCells: List<XY> = listOf(),
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
        fun isClearAt(x: Int, y: Int): Boolean {
            if (x < rect.x0 || y < rect.y0|| x > rect.x1 || y > rect.y1) return false
            return clearAt[x-rect.x0][y-rect.y0]
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

    @Transient
    lateinit var carto: Carto
    lateinit var room: Room
    var isAbandoned: Boolean = false
    protected val x0: Int get() = room.rect.x0
    protected val y0: Int get() = room.rect.y0
    protected val x1: Int get() = room.rect.x1
    protected val y1: Int get() = room.rect.y1
    protected val width: Int get() = room.width
    protected val height: Int get() = room.height

    private var cell = XY(0,0)

    open fun fitsInRoom(room: Room): Boolean = true

    fun furnish(room: Room, carto: Carto, isAbandoned: Boolean = false) {
        if (!fitsInRoom(room)) return
        this.isAbandoned = isAbandoned
        this.carto = carto
        this.room = room
        doFurnish()
        this.carto.chunk.rooms.add(this)
    }

    open fun getDescription(): String = if (isAbandoned) abandonedDescription() else description()
    open fun description() = ""
    open fun abandonedDescription() = "The room is covered in dust and cobwebs, clearly abandoned long ago."
    abstract fun doFurnish()

    open fun onPlayerEnter() {
        Console.say(getDescription())
    }

    open fun onPlayerExit() { }

    protected fun chance(chance: Float, doThis: ()->Unit) {
        if (Dice.chance(chance)) doThis()
    }

    protected fun spawn(thing: Thing) {
        if (isAbandoned && Dice.chance(0.3f)) return
        carto.spawnThing(cell.x, cell.y, thing)
        room.unclear(cell)
    }

    protected fun spawnAt(x: Int, y: Int, thing: Thing) {
        carto.spawnThing(x, y, thing)
        room.unclear(XY(x, y))
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

    protected fun atCenter(doThis: ()->Unit) {
        val cx = room.rect.x0 + (room.width / 2)
        val cy = room.rect.y0 + (room.height / 2)
        if (room.isClearAt(cx, cy)) {
            cell = XY(cx, cy)
            doThis()
        } else {
            for (ix in -1..1) {
                for (iy in -1..1) {
                    if (room.isClearAt(cx+ix, cy+iy)) {
                        cell = XY(cx+ix, cy+iy)
                        doThis()
                        return
                    }
                }
            }
        }
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

    protected fun forArea(x0: Int, y0: Int, x1: Int, y1: Int, doThis: (x: Int, y: Int)->Unit) {
        for (ix in x0..x1) {
            for (iy in y0..y1) {
                doThis(ix, iy)
            }
        }
    }

    protected fun setTerrain(x: Int, y: Int, type: Terrain.Type, roofed: Boolean? = null) {
        carto.setTerrain(x, y, type, roofed)
    }

    protected val clearCount: Int
        get() = room.clears.size

    protected fun boundsCheck(x: Int, y: Int) = (x in x0..x1 && y in y0..y1)

    protected fun flagsAt(x: Int, y: Int) = if (carto is WorldCarto) (carto as WorldCarto).flagsMap[x - x0][y - y0] else null
}

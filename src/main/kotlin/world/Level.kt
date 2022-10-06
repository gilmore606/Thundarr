package world

import util.*

class Level(val width: Int, val height: Int) {

    val tiles = Array(width) { Array(height) { Tile.WALL } }
    val visible = Array(width) { Array(height) { false } }
    val seen = Array(width) { Array(height) { false } }

    var pov = XY(0, 0)
        set(value) {
            field = value
            isTransientDirty = true
        }

    private var isTransientDirty = false
    private val stepMap = DijkstraMap(this)
    private val shadowCaster = ShadowCaster(
        { x, y -> isOpaqueAt(x, y) },
        { x, y, vis -> setTileVisibility(x, y, vis) }
    )

    fun forEachCell(doThis: (x: Int, y: Int) -> Unit) {
        for (x in 0 until width) {
            for (y in 0 until height) {
                doThis(x, y)
            }
        }
    }

    fun setTile(x: Int, y: Int, tile: Tile) {
        tiles[x][y] = tile
    }

    fun getTile(x: Int, y: Int) = try {
        tiles[x][y]
    } catch (e: ArrayIndexOutOfBoundsException) {
        Tile.FLOOR
    }
    fun getTile(xy: XY) = getTile(xy.x, xy.y)

    // Update anything that changes when things move, like visibility, shadows, and pathing.
    fun updateTransientData() {
        if (!isTransientDirty) return
        isTransientDirty = false

        updateVisibility()
        updateStepMaps()
    }

    fun getPathToPOV(from: XY) = stepMap.pathFrom(from)

    fun isWalkableAt(x: Int, y: Int): Boolean = try {
        tiles[x][y] == Tile.FLOOR
    } catch (e: ArrayIndexOutOfBoundsException) { false }

    fun isReachableAt(x: Int, y: Int): Boolean = try {
        stepMap.map[x][y] >= 0
    } catch (e: ArrayIndexOutOfBoundsException) { false }

    fun visibilityAt(x: Int, y: Int): Float = try {
        (if (seen[x][y]) 0.6f else 0f) + (if (visible[x][y]) 0.4f else 0f)
    } catch (e: ArrayIndexOutOfBoundsException) { 0f }

    private fun isOpaqueAt(x: Int, y: Int): Boolean = try {
        tiles[x][y] !== Tile.FLOOR
    } catch (e: ArrayIndexOutOfBoundsException) { true }

    private fun updateVisibility() {
        val distance = 12f
        for (y in 0 until height) {
            for (x in 0 until width) {
                visible[x][y] = false
            }
        }

        shadowCaster.cast(pov, distance)
    }

    private fun updateStepMaps() {
        stepMap.update(this.pov)
    }

    private fun setTileVisibility(x: Int, y: Int, vis: Boolean) {
        try {
            visible[x][y] = vis
            if (vis) seen[x][y] = true
        } catch (_: ArrayIndexOutOfBoundsException) { }
    }
}

package world

import util.*

class Level(val width: Int, val height: Int) {

    private val allVisible = false

    val tiles = Array(width) { Array(height) { Tile.WALL } }
    val visible = Array(width) { Array(height) { false } }
    val seen = Array(width) { Array(height) { false } }

    var pov = XY(0, 0)
        set(value) {
            field = value
            isVisibilityDirty = true
            updateStepMap()
        }

    private var isVisibilityDirty = false
    private val stepMap = DijkstraMap(this)
    private val shadowCaster = ShadowCaster(
        { x, y -> isOpaqueAt(x, y) },
        { x, y, vis -> setTileVisibility(x, y, vis) }
    )


    fun setTile(x: Int, y: Int, tile: Tile) {
        tiles[x][y] = tile
    }

    fun getTile(x: Int, y: Int) = try {
        tiles[x][y]
    } catch (e: ArrayIndexOutOfBoundsException) {
        Tile.FLOOR
    }

    fun getTile(xy: XY) = getTile(xy.x, xy.y)

    fun updateVisibility() {
        if (!isVisibilityDirty) return
        isVisibilityDirty = false

        if (allVisible) {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    visible[x][y] = true
                    seen[x][y] = true
                }
            }
            return
        }
        val distance = 12f
        for (y in 0 until height) {
            for (x in 0 until width) {
                visible[x][y] = false
            }
        }

        shadowCaster.cast(pov, distance)
    }

    fun updateStepMap() {
        stepMap.update(this.pov)
    }

    fun getPathToPOV(from: XY): List<XY> = stepMap.pathFrom(from)

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

    private fun setTileVisibility(x: Int, y: Int, vis: Boolean) {
        try {
            visible[x][y] = vis
            if (vis) seen[x][y] = true
        } catch (_: ArrayIndexOutOfBoundsException) { }
    }
}

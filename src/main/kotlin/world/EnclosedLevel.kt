package world

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.tilesets.Glyph
import util.Dice
import util.DijkstraMap
import util.ShadowCaster
import util.XY
import world.terrains.Terrain

@Serializable
class EnclosedLevel(
    val width: Int,
    val height: Int
    ) : Level() {

    private val seen = Array(width) { Array(height) { false } }
    private val visible = Array(width) { Array(height) { false } }
    private val terrains: Array<Array<Terrain.Type>> = Array(width) { Array(height) { Terrain.Type.TERRAIN_BRICKWALL } }

    @Transient
    override val stepMap = DijkstraMap(width, height) { x, y ->
        isWalkableAt(x, y)
    }

    @Transient
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

    override fun forEachCellToRender(
        doThis: (x: Int, y: Int, vis: Float, glyph: Glyph) -> Unit
    ) {
        for (x in pov.x - 50 until pov.x + 50) {
            for (y in pov.y - 50 until pov.y + 50) {
                if (x in 0 until width && y in 0 until height) {
                    val vis = visibilityAt(x, y)
                    if (vis > 0f) {
                        doThis(
                            x, y, vis,
                            Terrain.get(terrains[x][y]).glyph()
                        )
                    }
                }
            }
        }
    }

    override fun getTerrain(x: Int, y:Int) = try {
        terrains[x][y]
    } catch (e: ArrayIndexOutOfBoundsException) {
        Terrain.Type.TERRAIN_BRICKWALL
    }

    override fun setTerrain(x: Int, y: Int, type: Terrain.Type) {
        terrains[x][y] = type
    }

    override fun getGlyph(x: Int, y: Int) = try {
        Terrain.get(terrains[x][y]).glyph()
    } catch (e: ArrayIndexOutOfBoundsException) {
        Glyph.FLOOR
    }

    override fun isSeenAt(x: Int, y: Int): Boolean = try {
        seen[x][y]
    } catch (e: ArrayIndexOutOfBoundsException) { false }

    override fun isWalkableAt(x: Int, y: Int): Boolean = try {
        Terrain.get(terrains[x][y]).isWalkable()
    } catch (e: ArrayIndexOutOfBoundsException) { false }

    override fun visibilityAt(x: Int, y: Int): Float = if (App.DEBUG_VISIBLE) 1f else try {
        (if (seen[x][y]) 0.6f else 0f) + (if (visible[x][y]) 0.4f else 0f)
    } catch (e: ArrayIndexOutOfBoundsException) { 0f }

    override fun isOpaqueAt(x: Int, y: Int): Boolean = try {
        Terrain.get(terrains[x][y]).isOpaque()
    } catch (e: ArrayIndexOutOfBoundsException) { true }

    override fun setTileVisibility(x: Int, y: Int, vis: Boolean) {
        try {
            visible[x][y] = vis
            if (vis) seen[x][y] = true
        } catch (_: ArrayIndexOutOfBoundsException) { }
    }

    override fun updateVisibility() {
        for (y in 0 until height) {
            for (x in 0 until width) {
                visible[x][y] = false
            }
        }

        shadowCaster.cast(pov, 12f)
    }

    override fun getPathToPOV(from: XY) = stepMap.pathFrom(from)

    override fun tempPlayerStart(): XY {
        var tries = 5000
        while (tries > 0) {
            val x = Dice.zeroTil(width)
            val y = Dice.zeroTil(height)
            if (isWalkableAt(x, y)) return XY(x,y)
            tries--
        }
        throw RuntimeException("No space to put player in level!")
    }

}

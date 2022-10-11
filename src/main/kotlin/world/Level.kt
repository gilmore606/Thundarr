package world

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.GameScreen
import util.*
import world.terrains.BrickWall
import world.terrains.Terrain

@Serializable
class Level(val width: Int, val height: Int) {

    val seen = Array(width) { Array(height) { false } }
    val visible = Array(width) { Array(height) { false } }
    val terrains: Array<Array<Terrain>> = Array(width) { Array(height) { BrickWall() } }

    val pov = XY(0, 0)

    val director = Director()

    @Transient
    private val stepMap = DijkstraMap(this)
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

    fun forEachCellToRender(doThis: (x: Int, y: Int, vis: Float, glyph: Glyph) -> Unit) = forEachCell { x, y ->
            val vis = visibilityAt(x, y)
            if (vis > 0f) {
                doThis(
                    x, y,
                    visibilityAt(x, y),
                    terrains[x][y].glyph()
                )
            }
        }

    fun forEachActorToRender(doThis: (x: Int, y: Int, glyph: Glyph) -> Unit) = director.actors.forEach { actor ->
        val x = actor.xy.x
        val y = actor.xy.y
        val vis = visibilityAt(x, y)
        if (vis == 1f) {
            doThis(
                x, y,
                actor.glyph
            )
        }
    }

    fun setPov(x: Int, y: Int) {
        pov.x = x
        pov.y = y
        updateVisibility()
        updateStepMaps()
        if (this == App.level) GameScreen.povMoved()
    }

    fun setTerrain(x: Int, y: Int, type: Terrain.Type) {
        terrains[x][y] = Terrain.create(type)
    }

    fun getGlyph(x: Int, y: Int) = try {
        terrains[x][y].glyph()
    } catch (e: ArrayIndexOutOfBoundsException) {
        Glyph.FLOOR
    }

    fun getPathToPOV(from: XY) = stepMap.pathFrom(from)

    fun isSeenAt(x: Int, y: Int): Boolean = try {
        seen[x][y]
    } catch (e: ArrayIndexOutOfBoundsException) { false }

    fun isWalkableAt(x: Int, y: Int): Boolean = try {
        terrains[x][y].isWalkable()
    } catch (e: ArrayIndexOutOfBoundsException) { false }

    fun isWalkableAt(xy: XY, toDir: XY) = isWalkableAt(xy.x + toDir.x, xy.y + toDir.y)

    fun visibilityAt(x: Int, y: Int): Float = try {
        (if (seen[x][y]) 0.6f else 0f) + (if (visible[x][y]) 0.4f else 0f)
    } catch (e: ArrayIndexOutOfBoundsException) { 0f }

    fun tempPlayerStart(): XY {
        var xy: XY? = null
        forEachCell { x, y ->
            if (isWalkableAt(x, y)) xy = XY(x, y)
        }
        xy?.also { return it }
        throw RuntimeException("No space to put player in level!")
    }

    private fun isOpaqueAt(x: Int, y: Int): Boolean = try {
        terrains[x][y].isOpaque()
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

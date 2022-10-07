package world

import render.GameScreen
import util.*

class Level(val width: Int, val height: Int) {

    val glyphs = Array(width) { Array(height) { Glyph.WALL } }
    val visible = Array(width) { Array(height) { false } }
    val seen = Array(width) { Array(height) { false } }

    val pov = XY(0, 0)

    private val stepMap = DijkstraMap(this)
    private val shadowCaster = ShadowCaster(
        { x, y -> isOpaqueAt(x, y) },
        { x, y, vis -> setTileVisibility(x, y, vis) }
    )

    val director = Director(this)

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
                    glyphs[x][y]
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

    fun setTile(x: Int, y: Int, glyph: Glyph) {
        glyphs[x][y] = glyph
    }

    fun getTile(x: Int, y: Int) = try {
        glyphs[x][y]
    } catch (e: ArrayIndexOutOfBoundsException) {
        Glyph.FLOOR
    }

    fun getPathToPOV(from: XY) = stepMap.pathFrom(from)

    fun isWalkableAt(x: Int, y: Int): Boolean = try {
        glyphs[x][y] == Glyph.FLOOR
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
        glyphs[x][y] !== Glyph.FLOOR
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

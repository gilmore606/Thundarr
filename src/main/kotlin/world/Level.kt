package world

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.GameScreen
import render.tilesets.Glyph
import util.*
import world.terrains.Terrain

@Serializable
sealed class Level {

    var DEBUG_VISIBLE = false

    val pov = XY(0, 0)

    val director = Director()


    abstract fun forEachCellToRender(
        doThis: (x: Int, y: Int, vis: Float, glyph: Glyph) -> Unit
    )

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

    abstract fun getTerrain(x: Int, y: Int): Terrain.Type

    abstract fun setTerrain(x: Int, y: Int, type: Terrain.Type)

    abstract fun getGlyph(x: Int, y: Int): Glyph

    abstract fun getPathToPOV(from: XY): List<XY>

    abstract fun isSeenAt(x: Int, y: Int): Boolean

    abstract fun isWalkableAt(x: Int, y: Int): Boolean

    fun isWalkableAt(xy: XY, toDir: XY) = isWalkableAt(xy.x + toDir.x, xy.y + toDir.y)

    abstract fun visibilityAt(x: Int, y: Int): Float

    abstract fun isOpaqueAt(x: Int, y: Int): Boolean

    abstract fun updateVisibility()

    abstract fun updateStepMaps()

    abstract fun setTileVisibility(x: Int, y: Int, vis: Boolean)
}

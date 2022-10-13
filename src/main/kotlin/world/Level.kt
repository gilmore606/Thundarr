package world

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.GameScreen
import render.tilesets.Glyph
import util.*
import world.terrains.Terrain

@Serializable
sealed class Level {

    val pov = XY(0, 0)

    val director = Director()

    @Transient
    abstract val stepMap: StepMap

    // Temporary
    abstract fun tempPlayerStart(): XY

    // DoThis for all cells relevant to rendering the frame around the POV.
    abstract fun forEachCellToRender(
        doThis: (x: Int, y: Int, vis: Float, glyph: Glyph) -> Unit
    )

    // DoThis for all actor glyphs relevant to rendering the frame around the POV.
    fun forEachActorToRender(doThis: (x: Int, y: Int, glyph: Glyph) -> Unit) = director.actors.forEach { actor ->
        if (actor.renderable) {
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
    }

    // Move the POV.
    fun setPov(x: Int, y: Int) {
        pov.x = x
        pov.y = y
        onSetPov()
        updateVisibility()
        updateStepMap()
        if (this == App.level) GameScreen.povMoved()
    }

    abstract fun makeStepMap(): StepMap

    open fun updateStepMap() { stepMap.update(this.pov.x, this.pov.y) }

    protected open fun onSetPov() { }

    open fun onRestore() { }

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

    abstract fun setTileVisibility(x: Int, y: Int, vis: Boolean)
}

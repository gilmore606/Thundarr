package world

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.GameScreen
import render.RENDER_HEIGHT
import render.RENDER_WIDTH
import render.tilesets.Glyph
import things.Thing
import util.*
import world.terrains.Terrain

@Serializable
sealed class Level {

    val pov = XY(0, 0)

    val director = Director()

    @Transient protected val shadowCaster = ShadowCaster(
        { x, y -> isOpaqueAt(x, y) },
        { x, y, vis -> setTileVisibility(x, y, vis) }
    )

    @Transient protected lateinit var stepMap: StepMap


    // Temporary
    abstract fun tempPlayerStart(): XY

    open fun debugText(): String = ""

    abstract fun chunkAt(x: Int, y: Int): Chunk?

    // DoThis for all cells relevant to rendering the frame around the POV.
    fun forEachCellToRender(
        doThis: (x: Int, y: Int, vis: Float, glyph: Glyph) -> Unit
    ) {
        for (x in pov.x - RENDER_WIDTH /2 until pov.x + RENDER_WIDTH /2) {
            for (y in pov.y - RENDER_HEIGHT /2 until pov.y + RENDER_HEIGHT /2) {
                val vis = visibilityAt(x, y)
                if (vis > 0f) {
                    doThis(
                        x, y, vis,
                        Terrain.get(getTerrain(x,y)).glyph()
                    )
                }
            }
        }
    }

    fun forEachThingToRender(
        doThis: (x: Int, y: Int, vis: Float, glyph: Glyph) -> Unit
    ) {
        for (x in pov.x - RENDER_WIDTH/2 until pov.x + RENDER_WIDTH/2) {
            for (y in pov.y - RENDER_HEIGHT/2 until pov.y + RENDER_HEIGHT/2) {
                val thingsAt = getThingsAt(x,y)
                val vis = visibilityAt(x, y)
                if (thingsAt.isNotEmpty() && vis > 0f) {
                    doThis(
                        x, y, vis,
                        thingsAt[0].glyph()
                    )
                }
            }
        }
    }

    // DoThis for all actor glyphs relevant to rendering the frame around the POV.
    fun forEachActorToRender(doThis: (x: Int, y: Int, glyph: Glyph) -> Unit) = director.actors.forEach { actor ->
        if (actor.real) {
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

    fun getThingsAt(x: Int, y: Int): List<Thing> = chunkAt(x,y)?.getThingsAt(x,y) ?: ArrayList()

    fun getTerrain(x: Int, y: Int): Terrain.Type = chunkAt(x,y)?.getTerrain(x,y) ?: Terrain.Type.TERRAIN_STONEFLOOR

    fun setTerrain(x: Int, y: Int, type: Terrain.Type) = chunkAt(x,y)?.setTerrain(x,y,type) ?: Unit

    fun getGlyph(x: Int, y: Int): Glyph = chunkAt(x,y)?.getGlyph(x,y) ?: Glyph.FLOOR

    fun getPathToPOV(from: XY) = stepMap.pathFrom(from)

    fun isSeenAt(x: Int, y: Int) = chunkAt(x,y)?.isSeenAt(x,y) ?: false

    fun isWalkableAt(x: Int, y: Int) = chunkAt(x,y)?.isWalkableAt(x,y) ?: false

    fun isWalkableFrom(xy: XY, toDir: XY) = isWalkableAt(xy.x + toDir.x, xy.y + toDir.y)

    fun visibilityAt(x: Int, y: Int) = chunkAt(x,y)?.visibilityAt(x,y) ?: 0f

    fun isOpaqueAt(x: Int, y: Int) = chunkAt(x,y)?.isOpaqueAt(x,y) ?: true

    abstract fun updateVisibility()

    fun setTileVisibility(x: Int, y: Int, vis: Boolean) = chunkAt(x,y)?.setTileVisibility(x,y,vis) ?: Unit
}

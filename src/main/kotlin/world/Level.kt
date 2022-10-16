package world

import actors.Actor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.GameScreen
import render.RENDER_HEIGHT
import render.RENDER_WIDTH
import render.tilesets.Glyph
import things.LightSource
import things.Thing
import util.*
import world.terrains.Terrain

@Serializable
sealed class Level {

    val pov = XY(0, 0)

    val director = Director()

    @Transient protected val shadowCaster = RayCaster()
    @Transient var shadowDirty = true

    @Transient protected lateinit var stepMap: StepMap

    @Transient private val noThing = mutableListOf<Thing>()

    @Transient val dirtyLights = mutableMapOf<LightSource,XY>()


    abstract fun receiveChunk(chunk: Chunk)

    abstract fun tempPlayerStart(): XY?

    open fun debugText(): String = ""

    abstract fun chunkAt(x: Int, y: Int): Chunk?

    private val ambientLight = LightColor(0.3f, 0.3f, 0.8f)

    abstract fun allChunks(): Set<Chunk>


    fun forEachCellToRender(
        doThis: (x: Int, y: Int, vis: Float, glyph: Glyph, light: LightColor) -> Unit
    ) {
        for (x in pov.x - RENDER_WIDTH /2 until pov.x + RENDER_WIDTH /2) {
            for (y in pov.y - RENDER_HEIGHT /2 until pov.y + RENDER_HEIGHT /2) {
                val vis = visibilityAt(x, y)
                if (vis > 0f) {
                    doThis(
                        x, y, vis,
                        Terrain.get(getTerrain(x,y)).glyph(),
                        lightAt(x, y)
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
                val thingsAt = thingsAt(x,y)
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
        shadowDirty = true
        updateStepMap()
        if (this == App.level) GameScreen.povMoved()
    }

    fun onActorMovedTo(actor: Actor, x: Int, y: Int) {
        thingsAt(x, y).forEach { it.onWalkedOnBy(actor) }
    }

    abstract fun makeStepMap(): StepMap

    abstract fun isReady(): Boolean

    open fun updateStepMap() {
        //stepMap.update(this.pov.x, this.pov.y)
    }

    protected open fun onSetPov() { }

    open fun onRestore() { }

    open fun unload() { }

    fun thingsAt(x: Int, y: Int): List<Thing> = chunkAt(x,y)?.thingsAt(x,y) ?: noThing

    fun addThingAt(x: Int, y: Int, thing: Thing) = chunkAt(x,y)?.addThingAt(x, y, thing)

    fun removeThingAt(x: Int, y: Int, thing: Thing) = chunkAt(x,y)?.removeThingAt(x, y, thing)

    fun getTerrain(x: Int, y: Int): Terrain.Type = chunkAt(x,y)?.getTerrain(x,y) ?: Terrain.Type.TERRAIN_STONEFLOOR

    fun setTerrain(x: Int, y: Int, type: Terrain.Type) = chunkAt(x,y)?.setTerrain(x,y,type) ?: Unit

    fun getGlyph(x: Int, y: Int): Glyph = chunkAt(x,y)?.getGlyph(x,y) ?: Glyph.FLOOR

    fun getPathToPOV(from: XY) = stepMap.pathFrom(from)

    fun isSeenAt(x: Int, y: Int) = chunkAt(x,y)?.isSeenAt(x,y) ?: false

    fun isWalkableAt(x: Int, y: Int) = chunkAt(x,y)?.isWalkableAt(x,y) ?: false

    fun isWalkableFrom(xy: XY, toDir: XY) = isWalkableAt(xy.x + toDir.x, xy.y + toDir.y)

    fun visibilityAt(x: Int, y: Int) = chunkAt(x,y)?.visibilityAt(x,y) ?: 0f

    fun isOpaqueAt(x: Int, y: Int) = chunkAt(x,y)?.isOpaqueAt(x,y) ?: true

    fun updateForRender() {
        if (shadowDirty) {
            allChunks().forEach { it.clearVisibility() }
            shadowCaster.castVisibility(pov, 18f, { x, y ->
                isOpaqueAt(x, y)
            }, { x, y, vis ->
                setTileVisibility(x, y, vis)
            })
            shadowDirty = false
        }
        if (dirtyLights.isNotEmpty()) {
            log.debug("Reprojecting ${dirtyLights.size} lights")
            mutableMapOf<LightSource,XY>().apply {
                putAll(dirtyLights)
                forEach { (lightSource, location) ->
                    removeLightSource(lightSource)
                    chunkAt(location.x, location.y)?.projectLightSource(location, lightSource)
                }
            }
            dirtyLights.clear()
        }
    }

    fun setTileVisibility(x: Int, y: Int, vis: Boolean) = chunkAt(x,y)?.setTileVisibility(x,y,vis) ?: Unit

    fun receiveLight(x: Int, y: Int, lightSource: LightSource, r: Float, g: Float, b: Float) =
        chunkAt(x,y)?.receiveLight(x, y, lightSource, r, g, b)

    fun dirtyLightsTouching(x: Int, y: Int) = chunkAt(x,y)?.dirtyLightsTouching(x,y)

    fun ambientLight() = ambientLight

    fun lightAt(x: Int, y: Int) = chunkAt(x,y)?.lightAt(x,y) ?: ambientLight

    fun lightSourceLocation(lightSource: LightSource): XY? = allChunks().firstNotNullOfOrNull {
        it.lightSourceLocation(lightSource)
    }

    fun removeLightSource(lightSource: LightSource) {
        allChunks().forEach { it.removeLightSource(lightSource) }
    }
}

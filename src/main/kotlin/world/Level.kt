package world

import actors.Actor
import actors.actions.processes.WalkTo
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.GameScreen
import render.RENDER_HEIGHT
import render.RENDER_WIDTH
import render.tileholders.OverlapTile
import render.tilesets.Glyph
import render.tilesets.TileSet
import things.LightSource
import things.Thing
import ui.modals.ContextMenu
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
    open fun statusText(): String = ""

    abstract fun chunkAt(x: Int, y: Int): Chunk?

    private val ambientLight = LightColor(0.4f, 0.3f, 0.7f)
    open fun timeScale() = 1.0f
    open val sunLightSteps = mutableMapOf<Int,LightColor>().apply {
        this[0] = LightColor(0.2f, 0.2f, 0.5f)
        this[1] = LightColor(0.15f, 0.15f, 0.5f)
        this[2] = LightColor(0.13f, 0.13f, 0.5f)
        this[3] = LightColor(0.1f, 0.1f, 0.4f)
        this[4] = LightColor(0.2f, 0.25f, 0.4f)
        this[5] = LightColor(0.4f, 0.4f, 0.5f)
        this[6] = LightColor(0.7f, 0.5f, 0.5f)
        this[7] = LightColor(1f, 0.6f, 0.6f)
        this[8] = LightColor(1f, 0.8f, 0.8f)
        this[9] = LightColor(1f, 0.9f, 0.8f)
        this[10] = LightColor(1f, 0.9f, 0.8f)
        this[11] = LightColor(1f, 1f, 0.9f)
        this[12] = LightColor(1f, 1f, 1f)
        this[13] = LightColor(1f, 1f, 1f)
        this[14] = LightColor(1f, 1f, 1f)
        this[15] = LightColor(1f, 1f, 1f)
        this[16] = LightColor(1f, 1f, 1f)
        this[17] = LightColor(1f, 1f, 0.9f)
        this[18] = LightColor(1f, 1f, 0.8f)
        this[19] = LightColor(0.8f, 0.7f, 0.6f)
        this[20] = LightColor(0.8f, 0.6f, 0.5f)
        this[21] = LightColor(0.6f, 0.5f, 0.5f)
        this[22] = LightColor(0.4f, 0.35f, 0.5f)
        this[23] = LightColor(0.2f, 0.2f, 0.5f)
    }

    abstract fun allChunks(): Set<Chunk>


    fun forEachCellToRender(
        tileSet: TileSet,
        doThis: (x: Int, y: Int, vis: Float, glyph: Glyph, light: LightColor) -> Unit,
        doOverlap: (x: Int, y: Int, vis: Float, glyph: Glyph, edge: XY, light: LightColor) -> Unit
    ) {
        for (x in pov.x - RENDER_WIDTH /2 until pov.x + RENDER_WIDTH /2) {
            for (y in pov.y - RENDER_HEIGHT /2 until pov.y + RENDER_HEIGHT /2) {
                val vis = visibilityAt(x, y)
                val glyph = Terrain.get(getTerrain(x,y)).glyph()
                if (vis > 0f) {
                    doThis(
                        x, y, vis, glyph, lightAt(x, y)
                    )
                    if (tileSet.tileHolders[glyph] is OverlapTile) {
                        CARDINALS.forEach { edge ->
                            if ((tileSet.tileHolders[glyph] as OverlapTile).overlapsIn(this, x, y, edge)) {
                                doOverlap(
                                    x, y, vis, glyph, edge, lightAt(x - edge.x, y - edge.y)
                                )
                            }
                        }
                    }
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
                        x, y, vis, thingsAt[0].glyph()
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
                    x, y, actor.glyph
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

    fun getTerrainData(x: Int, y: Int): String = chunkAt(x,y)?.getTerrainData(x,y) ?: ""

    fun setTerrain(x: Int, y: Int, type: Terrain.Type) = chunkAt(x,y)?.setTerrain(x,y,type) ?: Unit

    fun setTerrainData(x: Int, y: Int, data: String) = chunkAt(x,y)?.setTerrainData(x,y,data)

    fun getGlyph(x: Int, y: Int): Glyph = chunkAt(x,y)?.getGlyph(x,y) ?: Glyph.BLANK

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
                    chunkAt(location.x, location.y)?.apply {
                        projectLightSource(location, lightSource)
                        dirtyLights.remove(lightSource)
                    }
                }
            }
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

    fun updateAmbientLight(hour: Int) {
        ambientLight.r = sunLightSteps[hour]?.r ?: 0f
        ambientLight.g = sunLightSteps[hour]?.g ?: 0f
        ambientLight.b = sunLightSteps[hour]?.b ?: 0f
        allChunks().forEach { it.refreshLight() }
    }

    fun makeContextMenu(x: Int, y: Int, menu: ContextMenu) {
        menu.addOption("Examine") {

        }
        if (isWalkableAt(x, y)) {
            menu.addOption("Walk to") {
                App.player.queue(WalkTo(this, x, y))
            }
        }
    }
}

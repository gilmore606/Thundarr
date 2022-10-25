package world

import actors.Actor
import actors.Player
import actors.actions.Get
import actors.actions.UseThing
import actors.actions.processes.WalkTo
import render.GameScreen
import render.sunLights
import render.tileholders.OverlapTile
import render.tileholders.WaterTile
import render.tilesets.Glyph
import render.tilesets.TileSet
import things.LightSource
import things.Portable
import things.Temporal
import things.Thing
import ui.modals.ContextMenu
import ui.panels.Console
import util.*
import world.terrains.Terrain

sealed class Level {

    val pov = XY(0, 0)

    val director = Director(this)

    private val shadowCaster = RayCaster()
    var shadowDirty = true

    protected lateinit var stepMap: StepMap
    private val noThing = mutableListOf<Thing>()

    val dirtyLights = mutableMapOf<LightSource,XY>()

    abstract fun receiveChunk(chunk: Chunk)

    abstract fun getPlayerEntranceFrom(fromLevelId: String): XY?

    open fun debugText(): String = ""
    open fun statusText(): String = ""

    abstract fun chunkAt(x: Int, y: Int): Chunk?

    private val ambientLight = LightColor(0.4f, 0.3f, 0.7f)
    open fun timeScale() = 1.0f
    open val sunLightSteps = sunLights()
    // We write into this value to return per-cell ambient light with player falloff.  This is to avoid allocation.
    // This is safe because one thread asks for these values serially and doesn't store the result directly.
    private val ambientResult = LightColor(0f,0f,0f)

    abstract fun allChunks(): Set<Chunk>
    abstract fun levelId(): String

    companion object {
        fun make(levelId: String) = if (levelId == "world") {
            WorldLevel()
        }  else {
            EnclosedLevel(levelId)
        }
    }

    fun forEachCellToRender(
        tileSet: TileSet,
        doTile: (x: Int, y: Int, vis: Float, glyph: Glyph, light: LightColor) -> Unit,
        doOverlap: (x: Int, y: Int, vis: Float, glyph: Glyph, edge: XY, light: LightColor) -> Unit,
        doOcclude: (x: Int, y: Int, edge: XY) -> Unit,
        doSurf: (x: Int, y: Int, vis: Float, light: LightColor, edge: XY) -> Unit
    ) {
        for (x in pov.x - GameScreen.RENDER_WIDTH /2 until pov.x + GameScreen.RENDER_WIDTH /2) {
            for (y in pov.y - GameScreen.RENDER_HEIGHT /2 until pov.y + GameScreen.RENDER_HEIGHT /2) {
                val vis = if (App.DEBUG_VISIBLE) 1f else visibilityAt(x, y)
                val terrain = Terrain.get(getTerrain(x,y))
                val glyph = terrain.glyph()
                if (vis > 0f) {
                    doTile(
                        x, y, vis, glyph, lightAt(x, y)
                    )
                    // Ground overlaps
                    if (tileSet.tileHolders[glyph] is OverlapTile) {
                        CARDINALS.forEach { edge ->
                            if ((tileSet.tileHolders[glyph] as OverlapTile).overlapsIn(this, x, y, edge)) {
                                doOverlap(
                                    x, y, vis, glyph, edge, lightAt(x - edge.x, y - edge.y)
                                )
                            }
                        }
                    }
                    // Water surf
                    if (tileSet.tileHolders[glyph] is WaterTile) {
                        listOf(NORTH, WEST, EAST).forEach { edge ->
                            if (tileSet.tileHolders[Terrain.get(getTerrain(x + edge.x, y + edge.y)).glyph()] !is WaterTile) {
                                doSurf(
                                    x, y, vis, lightAt(x, y), edge
                                )
                            }
                        }
                    }
                    // Occlusion shadows
                    if (vis == 1f && !terrain.isOpaque()) {
                        var okNE = true
                        var okSE = true
                        var okNW = true
                        var okSW = true
                        CARDINALS.forEach { edge ->
                            if (Terrain.get(getTerrain(x + edge.x, y + edge.y)).isOpaque()) {
                                doOcclude(
                                    x, y, edge
                                )
                                when (edge) {
                                    NORTH -> { okNE = false ; okNW = false }
                                    SOUTH -> { okSE = false ; okSW = false }
                                    EAST -> { okNE = false ; okSE = false}
                                    WEST -> { okNW = false ; okSW = false }
                                }
                            }
                        }
                        CORNERS.forEach { corner ->
                            if (Terrain.get(getTerrain(x + corner.x, y + corner.y)).isOpaque()) {
                                when {
                                    corner == NORTHEAST && okNE -> { doOcclude(x, y, corner) }
                                    corner == NORTHWEST && okNW -> { doOcclude(x, y, corner) }
                                    corner == SOUTHEAST && okSE -> { doOcclude(x, y, corner) }
                                    corner == SOUTHWEST && okSW -> { doOcclude(x, y, corner) }
                                }
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
        for (x in pov.x - GameScreen.RENDER_WIDTH/2 until pov.x + GameScreen.RENDER_WIDTH/2) {
            for (y in pov.y - GameScreen.RENDER_HEIGHT/2 until pov.y + GameScreen.RENDER_HEIGHT/2) {
                val thingsAt = thingsAt(x,y)
                val vis =  if (App.DEBUG_VISIBLE) 1f else visibilityAt(x, y)
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
            val x = actor.xy.x
            val y = actor.xy.y
            val vis =  if (App.DEBUG_VISIBLE) 1f else visibilityAt(x, y)
            if (vis == 1f && chunkAt(x,y) != null) {
                doThis(
                    x, y, actor.glyph()
                )
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
        actor.light()?.also {
            removeLightSource(actor)
            addLightSource(x, y, actor)
        }
        chunkAt(x, y)?.onAddActor(x, y, actor)
        thingsAt(x, y).forEach { it.onWalkedOnBy(actor) }

        if (actor is Player) {
            setPov(actor.xy.x, actor.xy.y)
            director.wakeNPCsNear(actor.xy)
            thingsAt(x, y).apply {
                if (isNotEmpty()) {
                    val thingList = mutableListOf<String>()
                    forEach { if (it is Portable) { thingList.add(it.name().aOrAn()) } }
                    if (thingList.isNotEmpty()) {
                        val things = thingList.joinToString(", ")
                        Console.say("You see $things here.")
                    }
                }
            }
        }
    }

    fun onActorMovedFrom(actor: Actor, x: Int, y: Int) {
        chunkAt(x, y)?.onRemoveActor(x, y, actor)
        actor.light()?.also { removeLightSource(actor) }
    }

    fun advanceTime(delta: Float) = director.advanceTime(delta)
    fun linkTemporal(temporal: Temporal) = director.linkTemporal(temporal)
    fun unlinkTemporal(temporal: Temporal) = director.unlinkTemporal(temporal)

    abstract fun makeStepMap(): StepMap

    abstract fun isReady(): Boolean

    open fun updateStepMap() {
        //stepMap.update(this.pov.x, this.pov.y)
    }

    protected open fun onSetPov() { }

    open fun onRestore() {

    }

    open fun unload() { }

    fun actorAt(x: Int, y: Int) = director.actors.firstOrNull { it.xy.x == x && it.xy.y == y }

    fun thingsAt(x: Int, y: Int): MutableList<Thing> = chunkAt(x,y)?.thingsAt(x,y) ?: noThing

    fun cellContainerAt(x: Int, y: Int) = chunkAt(x,y)?.cellContainerAt(x,y) ?: throw RuntimeException("no cell container for $x $y")

    fun onAddThing(x: Int, y: Int, thing: Thing) = chunkAt(x,y)?.onAddThing(x, y, thing)

    fun onRemoveThing(x: Int, y: Int, thing: Thing) = chunkAt(x,y)?.onRemoveThing(x, y, thing)

    fun getTerrain(x: Int, y: Int): Terrain.Type = chunkAt(x,y)?.getTerrain(x,y) ?: Terrain.Type.TERRAIN_STONEFLOOR

    fun getTerrainData(x: Int, y: Int): String = chunkAt(x,y)?.getTerrainData(x,y) ?: ""

    fun setTerrain(x: Int, y: Int, type: Terrain.Type) = chunkAt(x,y)?.setTerrain(x,y,type) ?: Unit

    fun setTerrainData(x: Int, y: Int, data: String) = chunkAt(x,y)?.setTerrainData(x,y,data)

    fun getRandom(x: Int, y: Int): Int = chunkAt(x,y)?.getRandom(x,y) ?: 4 // chosen by fair dice roll

    fun getGlyph(x: Int, y: Int): Glyph = chunkAt(x,y)?.getGlyph(x,y) ?: Glyph.BLANK

    fun getPathToPOV(from: XY) = stepMap.pathFrom(from)

    fun isSeenAt(x: Int, y: Int) = chunkAt(x,y)?.isSeenAt(x,y) ?: false

    fun isWalkableAt(x: Int, y: Int) = chunkAt(x,y)?.isWalkableAt(x,y) ?: false

    fun isWalkableFrom(xy: XY, toDir: XY) = isWalkableAt(xy.x + toDir.x, xy.y + toDir.y)

    fun visibilityAt(x: Int, y: Int) = chunkAt(x,y)?.visibilityAt(x,y) ?: 0f

    fun isOpaqueAt(x: Int, y: Int) = chunkAt(x,y)?.isOpaqueAt(x,y) ?: true

    fun updateForRender() {
        if (dirtyLights.isNotEmpty()) {
            shadowDirty = true
            mutableMapOf<LightSource,XY>().apply {
                putAll(dirtyLights)
                forEach { (lightSource, location) ->
                    removeLightSource(lightSource)
                    addLightSource(location.x, location.y, lightSource)
                    dirtyLights.remove(lightSource)
                }
            }
        }

        allChunks().forEach { it.dirtyAllLightCacheCells() }

        if (shadowDirty) {
            allChunks().forEach { it.clearVisibility() }
            shadowCaster.castVisibility(pov, App.player.visualRange(), { x, y ->
                isOpaqueAt(x, y)
            }, { x, y ->
                lightAt(x, y)
            }, { x, y, vis ->
                setTileVisibility(x, y, vis)
            })
            shadowDirty = false
        }
    }

    private fun setTileVisibility(x: Int, y: Int, vis: Boolean) = chunkAt(x,y)?.setTileVisibility(x,y,vis) ?: Unit

    fun receiveLight(x: Int, y: Int, lightSource: LightSource, r: Float, g: Float, b: Float) =
        chunkAt(x,y)?.receiveLight(x, y, lightSource, r, g, b)

    fun dirtyLightsTouching(x: Int, y: Int) = chunkAt(x,y)?.dirtyLightsTouching(x,y)

    fun ambientLight(x: Int, y: Int): LightColor {
        val brightness = ambientLight.brightness()
        val distance = java.lang.Float.min(MAX_LIGHT_RANGE, distanceBetween(x, y, App.player.xy.x, App.player.xy.y)).toFloat()
        val nearboost = if (distance < 1f) 1.3f else if (distance < 3f) 0.4f else if (distance < 4f) 0.2f else 0f
        val falloff = 1f + (nearboost - 0.02f * distance) * (1f - brightness)
        return ambientResult.apply {
            r = ambientLight.r * falloff
            g = ambientLight.g * falloff
            b = ambientLight.b * falloff
        }
    }

    fun getSingleLight(x: Int, y: Int, source: LightSource) = chunkAt(x,y)?.getSingleLight(x, y, source)

    fun lightAt(x: Int, y: Int) = chunkAt(x,y)?.lightAt(x,y) ?: ambientLight

    fun lightSourceLocation(lightSource: LightSource): XY? = allChunks().firstNotNullOfOrNull {
        it.lightSourceLocation(lightSource)
    }

    fun addLightSource(x: Int, y: Int, lightSource: LightSource) = chunkAt(x,y)?.projectLightSource(XY(x, y), lightSource)

    fun removeLightSource(lightSource: LightSource) {
        allChunks().forEach { it.removeLightSource(lightSource) }
    }

    fun updateAmbientLight(hour: Int, minute: Int) {
        val stepHours = sunLightSteps.keys
        var hour1 = 0
        var hour2 = 0
        for (stepHour in stepHours) {
            if (stepHour <= hour) {
                hour1 = stepHour
            } else if (hour2 == 0) {
                hour2 = stepHour
            }
        }
        if (hour2 == 0) hour2 = stepHours.first()
        val c1 = sunLightSteps[hour1]
        val c2 = sunLightSteps[hour2]
        val gap = (hour2 * 60) - ((if (hour2 > hour1) hour1 else hour1 - 24) * 60)
        val progress = (hour - hour1) * 60 + minute
        val fraction = (progress.toFloat() / gap.toFloat())

        ambientLight.r = c1!!.r + (c2!!.r - c1.r) * fraction
        ambientLight.g = c1!!.g + (c2!!.g - c1.g) * fraction
        ambientLight.b = c1!!.b + (c2!!.b - c1.b) * fraction
    }

    fun makeContextMenu(x: Int, y: Int, menu: ContextMenu) {
        if (App.player.xy.x == x && App.player.xy.y == y) {
            thingsAt(x,y).forEach {
                if (it.isPortable()) {
                    menu.addOption("take " + it.listName()) {
                        App.player.queue(Get(it, cellContainerAt(x, y)))
                    }
                }
                it.uses().forEach { use ->
                    if (use.canDo(App.player)) {
                        menu.addOption(use.command) {
                            App.player.queue(UseThing(it, use.duration, use.toDo))
                        }
                    }
                }
            }
        } else {
            if (isWalkableAt(x, y)) {
                menu.addOption("walk to") {
                    App.player.queue(WalkTo(this, x, y))
                }
            }
        }
        menu.addOption("examine") { }
    }
}

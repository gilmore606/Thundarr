package world

import actors.Actor
import actors.Player
import actors.actions.*
import actors.actions.processes.WalkTo
import render.Screen
import render.sparks.Raindrop
import render.sparks.Spark
import render.sunLights
import render.tilesets.Glyph
import things.LightSource
import things.Temporal
import things.Thing
import ui.modals.ContextMenu
import ui.modals.ExamineModal
import ui.panels.Console
import util.*
import world.stains.Stain
import world.terrains.Terrain
import world.terrains.TerrainData
import java.lang.Float.max
import java.lang.Float.min

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
    private val indoorLight = LightColor(0.1f, 0.2f, 0.5f)
    var weatherIntensity = 0f
    private var weatherIntensityTarget = 0f
    var cloudIntensity = 0f
    var rainIntensity = 0f
    private var framesBeforeRaindrop = 0
    private var lastWeatherHour = 0
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
        doTile: (x: Int, y: Int, vis: Float, glyph: Glyph, light: LightColor) -> Unit,
        doQuad: (x0: Double, y0: Double, x1: Double, y1: Double, tx0: Float, tx1: Float, ty0: Float, ty1: Float,
                 vis: Float, glyph: Glyph, light: LightColor)->Unit,
        doStain: (x: Int, y: Int, stain: Stain, light: LightColor) -> Unit,
        doWeather: (x: Int, y: Int, cloudAlpha: Float, rainAlpha: Float, fadeUp: Boolean) -> Unit,
        delta: Float
    ) {
        for (x in pov.x - Screen.renderTilesWide / 2 until pov.x + Screen.renderTilesWide / 2) {
            for (y in pov.y - Screen.renderTilesHigh / 2 until pov.y + Screen.renderTilesHigh / 2) {
                chunkAt(x, y)?.also { chunk ->
                    val vis = if (App.DEBUG_VISIBLE) 1f else chunk.visibilityAt(x, y)
                    val terrain = Terrain.get(chunk.getTerrain(x, y))
                    val glyph = terrain.glyph()
                    if (vis > 0f) {
                        val light = if (vis == 1f) chunk.lightAt(x, y) else if (Screen.showSeenAreas) Screen.halfLight else Screen.fullDark
                        doTile(
                            x, y, vis, glyph, light
                        )
                        terrain.renderExtraQuads(this, x, y, vis, glyph, light, doQuad)
                        if (vis == 1f) {
                            if ((!isRoofedAt(x, y) && (!isOpaqueAt(x, y) || isWalkableAt(x, y)))) {
                                doWeather(x, y, this.cloudIntensity, this.rainIntensity, false)
                                framesBeforeRaindrop--
                                if (framesBeforeRaindrop < 0 && rainIntensity > 0.2f) {
                                    val rainInterval = (1800 - rainIntensity * 1600).toInt()
                                    framesBeforeRaindrop = rainInterval + Dice.oneTo(rainInterval)
                                    addSpark(Raindrop().at(x, y))
                                }
                            } else if (!isRoofedAt(x, y+1) && (!isOpaqueAt(x,y+1) || isWalkableAt(x,y+1))) {
                                doWeather(x, y, this.cloudIntensity, this.rainIntensity, true)
                            }
                            chunk.thingsAt(x, y).forEach { it.onRender(delta) }
                            actorAt(x, y)?.onRender(delta)
                            chunk.stainAt(x, y)?.also { doStain(x, y, it, light) }
                        }
                    }
                }
            }
        }
    }

    fun forEachThingToRender(
        doThis: (x: Int, y: Int, thing: Thing, vis: Float) -> Unit
    ) {
        for (x in pov.x - Screen.renderTilesWide/2 until pov.x + Screen.renderTilesWide/2) {
            for (y in pov.y - Screen.renderTilesHigh/2 until pov.y + Screen.renderTilesHigh/2) {
                val thingsAt = thingsAt(x,y)
                val vis =  if (App.DEBUG_VISIBLE) 1f else visibilityAt(x, y)
                if (thingsAt.isNotEmpty() && vis > 0f) {
                    doThis(
                        x, y, thingsAt[0], vis
                    )
                }
            }
        }
    }

    // DoThis for all actor glyphs relevant to rendering the frame around the POV.
    fun forEachActorToRender(doThis: (x: Int, y: Int, actor: Actor) -> Unit) =
        director.actors.forEach { actor ->
            val x = actor.xy.x
            val y = actor.xy.y
            val vis =  if (App.DEBUG_VISIBLE) 1f else visibilityAt(x, y)
            if (vis == 1f && chunkAt(x,y) != null) {
                doThis(
                    x, y, actor
                )
            }
    }

    // DoThis for all spark glyphs.
    fun forEachSparkToRender(doThis: (x: Int, y: Int, glyph: Glyph, light: LightColor,
                                      offsetX: Float, offsetY: Float, scale: Float, alpha: Float) -> Unit) {
        allChunks().forEach { it.sparks().forEach { spark ->
            val vis =  if (App.DEBUG_VISIBLE) 1f else visibilityAt(spark.xy.x, spark.xy.y)
            if (vis == 1f && chunkAt(spark.xy.x, spark.xy.y) != null) {
                val light = if (spark.isLit()) this.lightAt(spark.xy.x, spark.xy.y) else Screen.fullLight
                doThis(
                    spark.xy.x, spark.xy.y, spark.glyph(), light, spark.offsetX(), spark.offsetY(), spark.scale(), spark.alpha()
                )
            }
        }}
    }

    // Move the POV.
    fun setPov(x: Int, y: Int) {
        pov.x = x
        pov.y = y
        onSetPov()
        shadowDirty = true
        updateStepMap()
        if (this == App.level) Screen.povMoved()
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
            Screen.updateZoomTarget()
            director.wakeNPCsNear(actor.xy)
        }
    }

    fun onActorMovedFrom(actor: Actor, x: Int, y: Int) {
        val light = actor.light()
        chunkAt(x, y)?.onRemoveActor(x, y, actor)
        light?.also { removeLightSource(actor) }
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

    open fun onRestore() { }

    open fun unload() { }

    fun actorAt(x: Int, y: Int) = director.actors.firstOrNull { it.xy.x == x && it.xy.y == y }

    fun thingsAt(x: Int, y: Int): MutableList<Thing> = chunkAt(x,y)?.thingsAt(x,y) ?: noThing

    fun stainAt(x: Int, y: Int): Stain? = chunkAt(x,y)?.stainAt(x,y)

    fun cellContainerAt(x: Int, y: Int) = chunkAt(x,y)?.cellContainerAt(x,y) ?: throw RuntimeException("no cell container for $x $y")

    fun onAddThing(x: Int, y: Int, thing: Thing) = chunkAt(x,y)?.onAddThing(x, y, thing)

    fun onRemoveThing(x: Int, y: Int, thing: Thing) = chunkAt(x,y)?.onRemoveThing(x, y, thing)

    fun getTerrain(x: Int, y: Int): Terrain.Type = chunkAt(x,y)?.getTerrain(x,y) ?: Terrain.Type.TERRAIN_STONEFLOOR

    fun getTerrainData(x: Int, y: Int): TerrainData? = chunkAt(x,y)?.getTerrainData(x,y)

    fun setTerrain(x: Int, y: Int, type: Terrain.Type, roofed: Boolean? = null) = chunkAt(x,y)?.setTerrain(x,y,type,roofed) ?: Unit

    fun setTerrainData(x: Int, y: Int, data: TerrainData?) = chunkAt(x,y)?.setTerrainData(x,y,data)

    fun getRandom(x: Int, y: Int): Int = chunkAt(x,y)?.getRandom(x,y) ?: 4 // chosen by fair dice roll

    fun getGlyph(x: Int, y: Int): Glyph = chunkAt(x,y)?.getGlyph(x,y) ?: Glyph.BLANK

    fun getPathToPOV(from: XY) = stepMap.pathFrom(from)

    fun isSeenAt(x: Int, y: Int) = chunkAt(x,y)?.isSeenAt(x,y) ?: false

    fun isRoofedAt(x: Int, y: Int) = chunkAt(x,y)?.isRoofedAt(x,y) ?: false

    fun isWalkableAt(x: Int, y: Int) = chunkAt(x,y)?.isWalkableAt(x,y) ?: false

    fun isWalkableFrom(xy: XY, toDir: XY) = isWalkableAt(xy.x + toDir.x, xy.y + toDir.y)

    fun visibilityAt(x: Int, y: Int) = chunkAt(x,y)?.visibilityAt(x,y) ?: 0f

    fun isOpaqueAt(x: Int, y: Int) = chunkAt(x,y)?.isOpaqueAt(x,y) ?: true

    fun addSpark(spark: Spark) = chunkAt(spark.xy.x, spark.xy.y)?.addSpark(spark)

    fun addStain(stain: Stain, x: Int, y: Int) = chunkAt(x,y)?.addStain(stain, x, y)

    fun onRender(delta: Float) {
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

        weatherIntensity = if (weatherIntensity < weatherIntensityTarget) {
            min(weatherIntensityTarget, weatherIntensity + 0.25f * delta)
        } else {
            max(weatherIntensityTarget, weatherIntensity - 0.25f * delta)
        }

        allChunks().forEach { it.onRender(delta) }
        director.actors.forEach { it.onRender(delta) }

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

    fun ambientLight(x: Int, y: Int, roofed: Boolean): LightColor {
        val light = if (roofed) indoorLight else ambientLight
        val brightness = light.brightness()
        val distance = java.lang.Float.min(MAX_LIGHT_RANGE, distanceBetween(x, y, App.player.xy.x, App.player.xy.y)).toFloat()
        val nearboost = if (distance < 1f) 1.3f else if (distance < 3f) 0.4f else if (distance < 4f) 0.2f else 0f
        val falloff = 1f + (nearboost - 0.02f * distance) * (1f - brightness)
        return ambientResult.apply {
            r = light.r * falloff
            g = light.g * falloff
            b = light.b * falloff
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

    fun updateTime(hour: Int, minute: Int) {
        updateAmbientLight(hour, minute)
        if (hour != lastWeatherHour) {
            lastWeatherHour = hour
            updateWeather(hour, minute)
        }
        val cloudLight = min(1f, (ambientLight.brightness() - 0.5f) * 2f)
        cloudIntensity = max(0f, min(1f, weatherIntensity * 2.0f) * cloudLight)
        rainIntensity = max(0f, (weatherIntensity - 0.5f) * 2f)
    }

    private fun updateWeather(hour: Int, minute: Int) {
        if (Dice.chance(0.4f + weatherIntensity * 0.3f)) {
            weatherIntensityTarget = min(1f, weatherIntensityTarget + 0.3f)
            if (App.player.level == this && !isRoofedAt(App.player.xy.x, App.player.xy.y)) {
                if (weatherIntensity < 0.45f && weatherIntensityTarget > 0.45f) {
                    Console.say("It begins to rain.")
                } else if (weatherIntensity >= 0.5f) {
                    Console.say("The rain falls harder.")
                } else if (hour in 7..18) {
                    Console.say("Clouds gather in the sky.")
                }
            }
        } else {
            weatherIntensityTarget = max(0f, weatherIntensityTarget - 0.3f)
            if (App.player.level == this && !isRoofedAt(App.player.xy.x, App.player.xy.y)) {
                if (weatherIntensity > 0.45f && weatherIntensityTarget < 0.45f) {
                    Console.say("It stops raining.")
                } else if (weatherIntensity > 0.45f) {
                    Console.say("The rain lets up a bit.")
                } else if (hour in 7..18) {
                    if (weatherIntensity > 0.3f) {
                        Console.say("The sun breaks through the clouds.")
                    } else {
                        Console.say("The bright sunlight warms your heart and body.")
                    }
                }
            }
        }
    }

    private fun updateAmbientLight(hour: Int, minute: Int) {
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

        cloudIntensity = max(0f, ambientLight.brightness() - 0.5f)
    }

    fun makeContextMenu(x: Int, y: Int, menu: ContextMenu) {
        if (App.player.xy.x == x && App.player.xy.y == y) {
            thingsAt(x,y).forEach {
                if (it.isPortable()) {
                    menu.addOption("take " + it.listName()) {
                        App.player.queue(Get(it))
                    }
                }
                it.uses().forEach { use ->
                    if (use.canDo(App.player)) {
                        menu.addOption(use.command) {
                            App.player.queue(Use(it, use.duration, use.toDo))
                        }
                    }
                }
            }
        } else {
            if (isWalkableAt(x, y)) {
                menu.addOption("walk here") {
                    App.player.queue(WalkTo(this, x, y))
                }
            }
        }
        actorAt(x, y)?.also { actor ->
            menu.addOption("examine " + actor.name()) {
                Screen.addModal(ExamineModal(actor))
            }
        }
    }

    // What action does the player take when bumping into dir from xy?
    fun bumpActionTo(x: Int, y: Int, dir: XY): Action? {
        actorAt(x + dir.x,y + dir.y)?.also { target ->
            if (App.player.willAggro) {
                return Melee(target, dir)
            }
            return Converse(target)
        } ?: run {
            return Bump(x, y, dir)
        }
        return null
    }

    fun visibleNPCs() = ArrayList<Actor>().apply {
        director.actors.forEach { actor ->
            if (visibilityAt(actor.xy.x, actor.xy.y) == 1f && actor !is Player) add(actor)
        }
    }

    protected open fun unloadChunk(chunk: Chunk, levelId: String = "world") {
        val actorsToSave = director.unloadActorsFromArea(chunk.x, chunk.y, chunk.x + CHUNK_SIZE - 1, chunk.y + CHUNK_SIZE - 1)
        chunk.unload(actorsToSave, levelId)
    }
}

package world

import actors.Actor
import audio.Speaker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import ktx.async.KtxAsync
import render.Screen
import render.sparks.Spark
import render.tilesets.Glyph
import things.LightSource
import things.Thing
import util.*
import world.gen.cartos.AttractCarto
import world.gen.cartos.WorldCarto
import world.gen.decors.Decor
import world.level.AttractLevel
import world.level.EnclosedLevel
import world.level.Level
import world.level.WorldLevel
import world.persist.ChunkLoader
import world.persist.LevelKeeper
import world.stains.Fire
import world.stains.Stain
import world.terrains.Floor
import world.terrains.Terrain
import world.terrains.TerrainData
import java.lang.Float.max
import java.lang.Float.min
import kotlin.random.Random


@Serializable
class Chunk(
    val width: Int, val height: Int
) {
    @Serializable
    enum class ExitType { WORLD, LEVEL }

    @Serializable
    class ExitRecord(
        val type: ExitType,
        val doorLocation: XY,
        val enterMsg: String,
        val buildingId: String = "na",
        val buildingFirstLevelId: String = "na",
        val worldDest: XY = XY(0,0)
    )

    val exits = mutableListOf<ExitRecord>()

    @Transient
    private lateinit var level: Level
    @Transient
    private var unloaded = false

    var x: Int = -999
    var y: Int = -999
    private var x1: Int = -998
    private var y1: Int = -998

    enum class Roofed { OUTDOOR, INDOOR, WINDOW }

    private val seen = Array(width) { Array(height) { false } }
    private val visible = Array(width) { Array(height) { false } }
    private val roofed = Array(width) { Array(height) { Roofed.INDOOR } }
    private val terrains = Array(width) { Array(height) { Terrain.Type.TERRAIN_BRICKWALL } }
    private val terrainData = Array(width) { Array<TerrainData?>(height) { null } }
    private val sounds = Array(width) { Array<Speaker.PointAmbience?>(height) { null } }
    private val things = Array(width) { Array(height) { CellContainer() } }

    val rooms = mutableListOf<Decor>()

    private val savedActors: MutableSet<Actor> = mutableSetOf()

    var generating = true

    @Transient
    private val sparks = ArrayList<Spark>()
    @Transient
    private val lights = Array(width) { Array(height) { mutableMapOf<LightSource, LightColor>() } }
    @Transient
    private val lightCache = Array(width) { Array(height) { LightColor(0f, 0f, 0f) } }
    @Transient
    private val lightCacheDirty = Array(width) { Array(height) { true } }
    @Transient
    private val lightSourceLocations = mutableMapOf<LightSource, XY>()
    @Transient
    private val lightsTouching = ArrayList<LightSource>()
    @Transient
    private val lightCaster = RayCaster()

    @Transient
    val glyphCache = Array(width) { Array<Int?>(height) { null } }
    @Transient
    private val walkableCache = Array(width) { Array<Boolean?>(height) { null } }
    @Transient
    private val opaqueCache = Array(width) { Array<Boolean?>(height) { null } }
    @Transient
    private val randomCache = Array(width) { Array<Int?>(height) { null } }

    @Transient private val noThing = ArrayList<Thing>()


    fun onCreate(x: Int, y: Int) {
        this.x = x
        this.y = y
        this.x1 = x + width - 1
        this.y1 = y + height - 1
    }

    fun connectLevel(level: Level) {
        this.level = level
        forEachCell { x, y ->
            things[x][y].reconnect(level, x + this.x, y + this.y)
        }
    }

    fun randomPlayerStart(): XY {
        var tries = 5000
        while (tries > 0) {
            val x = Dice.range(x, x + width - 1)
            val y = Dice.range(y, y + height - 1)
            return XY(x,y)
            tries--
        }
        throw RuntimeException("No space to put player in level!")
    }

    fun onRestore(level: Level) {
        connectLevel(level)
        KtxAsync.launch {
            while (!level.isReady()) delay(20)
            savedActors.forEach { actor ->
                actor.moveTo(level, actor.xy.x, actor.xy.y)
                actor.onRestore()
            }
        }
    }

    // Carve myself into a chunk for the world.  Maybe with the starter dungeon.
    suspend fun generateWorld(level: Level) {
        generating = true
        connectLevel(level)
        var withStarter = false
        if ((level as WorldLevel).needStarterDungeon) {
            withStarter = true
            log.info("Generating world chunk $x $y for starter dungeon...")
            level.needStarterDungeon = false  // only need to do this once!
        }
        WorldCarto(x, y, x+width-1,y+height-1,this, level, withStarter).carveWorldChunk()
        generating = false
    }

    // Carve myself into a chunk for a building level.
    fun generateLevel(level: Level, building: Building) {
        generating = true
        (level as EnclosedLevel).connectChunk(this)  // TODO: we need to just generically do this for both kinds
        connectLevel(level)
        building.carveLevel(level)
        generating = false
    }

    fun generateAttractLevel(level: Level) {
        generating = true
        connectLevel(level)
        AttractCarto(0, 0, AttractLevel.dimension - 1, AttractLevel.dimension - 1, this, level).apply {
            carveWorldChunk(Random.nextDouble() * 1000.0 + 500.0)
        }
        generating = false
    }

    fun unload(saveActors: Set<Actor>, levelId: String = "world") {
        if (unloaded) return
        unloaded = true
        savedActors.clear()
        savedActors.addAll(saveActors)

        KtxAsync.launch {
            mutableSetOf<LightSource>().apply {
                lightSourceLocations.forEach { (it, _) -> add(it) }
            }.forEach {
                level.removeLightSource(it)
            }
        }

        if (levelId == "world") {
            ChunkLoader.saveWorldChunk(this) { finishUnload() }
        } else {
            ChunkLoader.saveLevelChunk(this, levelId) { finishUnload() }
        }
    }

    // Null out references and so forth.
    private fun finishUnload() {
        generating = true
        savedActors.forEach { it.level = null }
        savedActors.clear()
        KtxAsync.launch {
            delay(500)
            forEachCell { x, y ->
                things[x][y].unload()
                lights[x][y].clear()
            }
            lightSourceLocations.clear()
        }
    }

    fun onPlayerEntered() {
        // Warm up any levels we have exits to.
        exits.forEach { exit ->
            when (exit.type) {
                ExitType.LEVEL -> LevelKeeper.getLevel(exit.buildingFirstLevelId)
                ExitType.WORLD -> LevelKeeper.getLevel("world")
            }
        }
    }

    private inline fun boundsCheck(x: Int, y: Int) = !(x < this.x || y < this.y || x > this.x1 || y > this.y1)
    fun inBounds(x: Int, y: Int) = boundsCheck(x, y)

    fun onAddThing(x: Int, y: Int, thing: Thing) {
        updateOpaque(x - this.x, y - this.y)
        updateWalkable(x - this.x, y - this.y)
        if (thing is LightSource) { projectLightSource(XY(x, y), thing) }
    }

    fun onRemoveThing(x: Int, y: Int, thing: Thing) {
        updateOpaque(x - this.x, y - this.y)
        updateWalkable(x - this.x, y - this.y)
        if (thing is LightSource) { level.removeLightSource(thing) }
    }

    fun onAddActor(x: Int, y: Int, actor: Actor) { }

    fun onRemoveActor(x: Int, y: Int, actor: Actor) { }

    fun thingsAt(x: Int, y: Int) = if (boundsCheck(x, y)) {
        things[x - this.x][y - this.y].contents
    } else noThing

    fun stainsAt(x: Int, y: Int) = if (boundsCheck(x, y)) {
        things[x - this.x][y - this.y].stains
    } else null

    fun exitAt(x: Int, y: Int): ExitRecord? = if (boundsCheck(x, y)) {
        exits.firstOrNull { it.doorLocation.x == x && it.doorLocation.y == y }
    } else null

    fun cellContainerAt(x: Int, y: Int) = if (boundsCheck(x, y)) {
        things[x - this.x][y - this.y]
    } else null

    fun getTerrain(x: Int, y: Int) = if (boundsCheck(x, y)) {
        terrains[x - this.x][y - this.y]
    } else Terrain.Type.BLANK

    fun setTerrain(x: Int, y: Int, type: Terrain.Type, roofed: Boolean? = null) {
        this.terrains[x - this.x][y - this.y] = type
        this.terrainData[x - this.x][y - this.y] = null
        updateOpaque(x - this.x, y - this.y)
        updateWalkable(x - this.x, y - this.y)
        roofed?.also {
            var newRoof = Roofed.OUTDOOR
            if (roofed) {
                if (DIRECTIONS.hasOneWhere { dir ->
                        (level.roofedAt(x + dir.x, y + dir.y) == Roofed.OUTDOOR) && !level.isOpaqueAt(x + dir.x, y + dir.y)
                } && !level.isOpaqueAt(x, y)) {
                    // We're a window
                    this.roofed[x - this.x][y - this.y] = Roofed.WINDOW
                    newRoof = Roofed.WINDOW
                    // We used to be outdoor.  Did we deprive any old window?
                    DIRECTIONS.from(x, y) { dx, dy, _ ->
                        if (level.roofedAt(dx, dy) == Roofed.WINDOW) {
                            if (!DIRECTIONS.hasOneWhere { dir2 ->
                                    level.roofedAt(dx + dir2.x, dy + dir2.y) == Roofed.OUTDOOR && !level.isOpaqueAt(dx + dir2.x, dy + dir2.y)
                                }) {
                                level.setRoofedAt(dx, dy, Roofed.INDOOR)
                            }
                        }
                    }
                } else {
                    newRoof = Roofed.INDOOR
                }
            }
            this.roofed[x - this.x][y - this.y] = newRoof
        }
        for (i in (-1..1)) {
            for (j in (-1..1)) {
                if (boundsCheck(x+i,y+j)) {
                    Terrain.get(getTerrain(x + i, y + j)).also { terrain ->
                        if (terrain is Floor && !generating) {
                            setTerrainData(x + i, y + j, terrain.makeOverlaps(this, x + i, y + j))
                        }
                    }
                }
            }
        }
        level.dirtyLightsTouching(x, y)
    }

    fun getSound(x: Int, y: Int): Speaker.PointAmbience? = if (boundsCheck(x, y)) {
        this.sounds[x-this.x][y-this.y] ?:
        things[x-this.x][y-this.y].contents().firstNotNullOfOrNull { it.ambientSound() }
    } else null

    fun setSound(x: Int, y: Int, newSound: Speaker.PointAmbience) {
        this.sounds[x-this.x][y-this.y] = newSound
    }

    fun setRoofed(x: Int, y: Int, newRoofed: Roofed) {
        this.roofed[x - this.x][y - this.y] = newRoofed
    }

    fun getTerrainData(x: Int, y: Int) = if (boundsCheck(x, y)) {
        terrainData[x - this.x][y - this.y]
    } else null

    fun setTerrainData(x: Int, y: Int, data: TerrainData?) {
        if (data != null && Terrain.get(terrains[x - this.x][y - this.y]).dataType != data.forType) {
            throw RuntimeException("attempt to set terrain data for mismatched type ${data.forType} (terrain was ${terrains[x-this.x][y-this.y]})")
        }
        terrainData[x - this.x][y - this.y] = data
    }

    fun getRandom(x: Int, y: Int): Int {
        return if (boundsCheck(x, y)) {
            randomCache[x - this.x][y - this.y] ?: run {
                val newRandom = Random.nextInt(100000)
                randomCache[x - this.x][y - this.y] = newRandom
                newRandom
            }
        } else 4 // chosen by fair dice roll
    }

    fun getGlyph(x: Int, y: Int) = if (boundsCheck(x, y)) {
        Terrain.get(terrains[x - this.x][y - this.y]).glyph()
    } else { Glyph.BLANK }

    fun isSeenAt(x: Int, y: Int): Boolean = if (boundsCheck(x, y)) {
        seen[x - this.x][y - this.y]
    } else false

    fun roofedAt(x: Int, y: Int): Roofed = if (boundsCheck(x, y)) {
        roofed[x - this.x][y - this.y]
    } else Roofed.OUTDOOR

    fun isRoofedAt(x: Int, y: Int): Boolean = if (boundsCheck(x, y)) {
        roofed[x - this.x][y - this.y] != Roofed.OUTDOOR
    } else false

    fun isWalkableAt(x: Int, y: Int): Boolean = if (boundsCheck(x, y)) {
        walkableCache[x - this.x][y - this.y] ?: updateWalkable(x - this.x, y - this.y)
    } else false

    fun isPathableBy(entity: Entity?, x: Int, y: Int): Boolean {
        if (boundsCheck(x, y)) {
            if (isWalkableAt(x, y)) {
                if (level.stainsAt(x, y)?.hasOneWhere { it is Fire } != true) {
                    return true
                }
            }
        }
        return false
    }

    private fun updateWalkable(x: Int, y: Int): Boolean {
        if (generating) return Terrain.get(terrains[x][y]).isWalkable()
        if (boundsCheck(x + this.x, y + this.y)) {
            var v = Terrain.get(terrains[x][y]).isWalkable()
            if (v) {
                var thingBlocking = false
                things[x][y].contents.forEach { thing ->
                    thingBlocking = thingBlocking || thing.isBlocking()
                }
                v = !thingBlocking
            }
            walkableCache[x][y] = v
            return v
        }
        return false
    }

    fun roomAt(x: Int, y: Int): Decor? = rooms.firstOrNull {
        x >= it.room.rect.x0 && x <= it.room.rect.x1 && y >= it.room.rect.y0 && y <= it.room.rect.y1
    }

    fun visibilityAt(x: Int, y: Int): Float = if (boundsCheck(x, y)) {
        (if (seen[x - this.x][y - this.y]) 0.5f else 0f) +
                (if (visible[x - this.x][y - this.y]) 0.5f else 0f)
    } else 0f


    fun setTileVisibility(x: Int, y: Int, vis: Boolean) {
        if (boundsCheck(x, y)) {
            visible[x - this.x][y - this.y] = vis
            if (vis) seen[x - this.x][y - this.y] = true
        }
    }

    fun clearVisibility() {
        forEachCell { x, y -> visible[x][y] = false }
    }

    fun clearSeen() {
        forEachCell { x, y -> seen[x][y] = false }
    }

    fun isOpaqueAt(x: Int, y: Int): Boolean = if (boundsCheck(x, y)) {
        opaqueCache[x - this.x][y - this.y] ?: updateOpaque(x - this.x, y - this.y)
    } else { false }

    private fun updateOpaque(x: Int, y: Int): Boolean {
        if (boundsCheck(x + this.x, y + this.y)) {
            if (generating) return Terrain.get(terrains[x][y]).isOpaque()
            var v = Terrain.get(terrains[x][y]).isOpaque()
            if (!v) {
                var thingBlocking = false
                things[x][y].contents.forEach { thing ->
                    thingBlocking = thingBlocking || thing.isOpaque()
                }
                v = thingBlocking
            }

            val oldValue = opaqueCache[x][y]
            opaqueCache[x][y] = v
            if (oldValue != null && oldValue != v) {
                level.dirtyLightsTouching(x + this.x, y + this.y)
                level.shadowDirty = true
            }
            return v
        }
        return true
    }

    // Dirty all lights that fall on this cell.
    fun dirtyLightsTouching(x: Int, y: Int) {
        if (boundsCheck(x, y)) {
            lights[x - this.x][y - this.y].forEach { (lightSource, _) ->
                level.lightSourceLocation(lightSource)?.also { location ->
                    log.debug("Dirtying light at $location")
                    level.dirtyLights[lightSource] = location
                }
            }
        }
    }

    fun lightSourceLocation(lightSource: LightSource) = lightSourceLocations[lightSource]

    // Project light from location into all nearby cells.
    fun projectLightSource(xy: XY, lightSource: LightSource) {
        lightSource.light()?.also { lightColor ->
            lightSourceLocations[lightSource] = XY(xy.x,xy.y)
            lightCaster.castLight(xy.x, xy.y, lightColor, { x, y ->
                level.isOpaqueAt(x, y)
            }, { x, y, r, g, b ->
                level.receiveLight(x, y, lightSource, r, g, b)
            }, { x, y ->
                level.getSingleLight(x, y, lightSource)
            })
        }
        level.shadowDirty = true
    }

    // Receive projected light from a source and save it in cache.
    fun receiveLight(x: Int, y: Int, lightSource: LightSource, r: Float, g: Float, b: Float) {
        if (boundsCheck(x, y)) {
            lightsTouching.add(lightSource)
            lightCacheDirty[x - this.x][y - this.y] = true
            if (lights[x - this.x][y - this.y].contains(lightSource)) {
                lights[x - this.x][y - this.y][lightSource]?.r = r
                lights[x - this.x][y - this.y][lightSource]?.g = g
                lights[x - this.x][y - this.y][lightSource]?.b = b
            } else {
                lights[x - this.x][y - this.y][lightSource] = LightColor(r, g, b)
            }
        }
    }

    fun getSingleLight(x: Int, y: Int, source: LightSource) =
        if (boundsCheck(x, y)) lights[x - this.x][y - this.y][source] else null

    fun lightAt(x: Int, y: Int): LightColor {
        if (boundsCheck(x, y)) {
            if (lightCacheDirty[x - this.x][y - this.y]) {
                refreshLightCacheAt(x - this.x, y - this.y)
            }
            return lightCache[x - this.x][y - this.y]
        }
        return level.ambientLight(x, y, level.roofedAt(x, y))
    }

    fun lightsAt(x: Int, y: Int): MutableMap<LightSource, LightColor>? = if (boundsCheck(x, y))
        lights[x - this.x][y - this.y] else null

    // Force all cells to re-sum light on next frame.
    fun dirtyEntireLightAndGlyphCaches() {
        for (x in 0 until width) {
            for (y in 0 until height) {
                lightCacheDirty[x][y] = true
                glyphCache[x][y] = null
            }
        }
    }

    private fun refreshLightCacheAt(x: Int, y: Int) {
        val ambient = level.ambientLight(x + this.x, y + this.y, roofed[x][y])
        lightCache[x][y].r = ambient.r
        lightCache[x][y].g = ambient.g
        lightCache[x][y].b = ambient.b
        var floorLight: LightColor? = null
        if (isOpaqueAt(x + this.x,y + this.y)) {  // wall light!  use the visible neighbor to avoid wall bleed
            DIRECTIONS.from(x + this.x, y + this.y) { lx, ly, _ ->
                if (floorLight == null && !level.isOpaqueAt(lx,ly) && level.visibilityAt(lx,ly) == 1f) {
                    floorLight = level.lightAt(lx, ly)
                }
            }
            if (floorLight == null) floorLight = Screen.fullDark
        }
        floorLight?.also { floorLight ->
            lightCache[x][y].r = floorLight.r
            lightCache[x][y].g = floorLight.g
            lightCache[x][y].b = floorLight.b
        } ?: run {
            lights[x][y].forEach { (light, color) ->
                val flicker = light.flicker()
                lightCache[x][y].r = min(1f, (lightCache[x][y].r + max(0f, (color.r * flicker))))
                lightCache[x][y].g = min(1f, (lightCache[x][y].g + max(0f, (color.g * flicker))))
                lightCache[x][y].b = min(1f, (lightCache[x][y].b + max(0f, (color.b * flicker))))
            }
        }
        Terrain.get(getTerrain(x + this.x, y + this.y)).glowColor()?.also { glowColor ->
            lightCache[x][y].r = min(1f, (lightCache[x][y].r + glowColor.r))
            lightCache[x][y].g = min(1f, (lightCache[x][y].g + glowColor.g))
            lightCache[x][y].b = min(1f, (lightCache[x][y].b + glowColor.b))
        } ?: run {
            var nearGlow: LightColor? = null
            for (ix in -1..1) {
                for (iy in -1 .. 1) {
                    Terrain.get(getTerrain(x + ix + this.x, y + iy + this.y)).glowColor()?.also { glowColor ->
                        nearGlow = glowColor
                    }
                }
            }
            nearGlow?.also {
                lightCache[x][y].r = min(1f, (lightCache[x][y].r + it.r * 0.5f))
                lightCache[x][y].g = min(1f, (lightCache[x][y].g + it.g * 0.5f))
                lightCache[x][y].b = min(1f, (lightCache[x][y].b + it.b * 0.5f))
            }
        }
        lightCacheDirty[x][y] = false
    }

    fun removeLightSource(lightSource: LightSource) {
        if (lightSource in lightsTouching) {
            forEachCell { x, y ->
                lights[x][y].remove(lightSource)
            }
            lightSourceLocations.remove(lightSource)
            lightsTouching.remove(lightSource)
            level.dirtyEntireLightAndGlyphCaches()
        }
    }

    fun onRender(delta: Float) {
        //if (isEveryFrame(4)) dirtyEntireLightAndGlyphCaches()
        sparks.filterOut({ it.done }) { it.onRender(delta) }
    }

    fun addSpark(spark: Spark) { sparks.add(spark) }

    fun sparks() = sparks

    fun addStain(stain: Stain, x: Int, y: Int) {
        if (boundsCheck(x, y)) { things[x-this.x][y-this.y].addStain(stain) }
    }

    private fun forEachCell(doThis: (Int,Int)->Unit) {
        for (x in 0 until width) {
            for (y in 0 until height) {
                doThis(x,y)
            }
        }
    }
}

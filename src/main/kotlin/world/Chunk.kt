package world

import actors.Actor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import ktx.async.KtxAsync
import render.tilesets.Glyph
import things.LightSource
import things.Thing
import util.*
import world.cartos.LevelCarto
import world.cartos.WorldCarto
import world.terrains.Terrain
import java.lang.Float.min
import kotlin.random.Random


@Serializable
class Chunk(
    val width: Int, val height: Int
) {
    @Transient
    private lateinit var level: Level
    @Transient
    private var unloaded = false

    var x: Int = -999
    var y: Int = -999
    private var x1: Int = -998
    private var y1: Int = -998

    private val seen = Array(width) { Array(height) { false } }
    private val visible = Array(width) { Array(height) { false } }
    private val terrains = Array(width) { Array(height) { Terrain.Type.TERRAIN_BRICKWALL } }
    private val terrainData = Array(width) { Array(height) { "" } }
    private val things = Array(width) { Array(height) { CellContainer() } }

    val savedActors: MutableSet<Actor> = mutableSetOf()
    var generating = true

    @Transient
    private val lights = Array(width) { Array(height) { mutableMapOf<LightSource, LightColor>() } }
    @Transient
    private val lightCache = Array(width) { Array(height) { LightColor(0f, 0f, 0f) } }
    @Transient
    private val lightCacheDirty = Array(width) { Array(height) { true } }
    @Transient
    private val lightSourceLocations = mutableMapOf<LightSource, XY>()
    @Transient
    private val lightCaster = RayCaster()
    @Transient
    private val debugLight = LightColor(0.8f, 0.8f, 0.8f)

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
            if (isWalkableAt(x, y)) return XY(x,y)
            tries--
        }
        throw RuntimeException("No space to put player in level!")
    }

    fun onRestore(level: Level) {
        connectLevel(level)
        //log.debug("chunk ${this.x} ${this.y} restoring ${savedActors.size} actors")
        savedActors.forEach { actor ->
            actor.onRestore()
            actor.moveTo(level, actor.xy.x, actor.xy.y)
        }
    }

    // Carve myself into a chunk for the world.
    fun generateWorld(level: Level) {
        generating = true
        WorldCarto(x, y, x+width-1,y+height-1,this, level)
            .carveWorldChunk()
        generating = false
    }

    // Carve myself into a chunk for a building level.
    fun generateLevel(level: Level, building: Building) {
        generating = true
        LevelCarto(0, 0, building.floorWidth - 1, building.floorHeight - 1, this, level)
            .carveLevel(
                worldExit = LevelCarto.WorldExit(NORTH, XY(building.x, building.y - 1))
            )
        generating = false
    }

    fun unload(saveActors: Set<Actor>) {
        if (unloaded) return
        unloaded = true
        savedActors.clear()
        //log.debug("chunk $x $y saving ${saveActors.size} actors")
        savedActors.addAll(saveActors)

        mutableSetOf<LightSource>().apply {
            lightSourceLocations.forEach { (it, _) -> add(it) }
        }.forEach { level.removeLightSource(it) }

        ChunkLoader.saveWorldChunk(this) { finishUnload() }
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

    private inline fun boundsCheck(x: Int, y: Int) = !(x < this.x || y < this.y || x > this.x1 || y > this.y1)

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

    fun onAddActor(x: Int, y: Int, actor: Actor) {
        updateWalkable(x - this.x, y - this.y)
    }

    fun onRemoveActor(x: Int, y: Int, actor: Actor) {
        updateWalkable(x - this.x, y - this.y)
    }

    fun thingsAt(x: Int, y: Int) = if (boundsCheck(x, y)) {
        things[x - this.x][y - this.y].contents
    } else { noThing }

    fun cellContainerAt(x: Int, y: Int) = if (boundsCheck(x, y)) {
        things[x - this.x][y - this.y]
    } else { throw RuntimeException("no cell container for $x $y") }

    fun getTerrain(x: Int, y: Int) = if (boundsCheck(x, y)) {
        terrains[x - this.x][y - this.y]
    } else { Terrain.Type.TERRAIN_BRICKWALL }

    fun setTerrain(x: Int, y: Int, type: Terrain.Type) {
        terrains[x - this.x][y - this.y] = type
        updateOpaque(x - this.x, y - this.y)
        updateWalkable(x - this.x, y - this.y)
    }

    fun getTerrainData(x: Int, y: Int) = if (boundsCheck(x, y)) {
        terrainData[x - this.x][y - this.y]
    } else { "" }

    fun setTerrainData(x: Int, y: Int, data: String) {
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
    } else { false }

    fun isWalkableAt(x: Int, y: Int): Boolean = if (boundsCheck(x, y)) {
        walkableCache[x - this.x][y - this.y] ?: updateWalkable(x - this.x, y - this.y)
    } else { false }

    private fun updateWalkable(x: Int, y: Int): Boolean {
        if (generating) return Terrain.get(terrains[x][y]).isWalkable()
        if (boundsCheck(x + this.x, y + this.y)) {
            var v = (level.actorAt(x + this.x,y + this.y) == null) && Terrain.get(terrains[x][y]).isWalkable()
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

    fun visibilityAt(x: Int, y: Int): Float = if (boundsCheck(x, y)) {
        (if (seen[x - this.x][y - this.y]) 0.5f else 0f) +
                (if (visible[x - this.x][y - this.y]) 0.5f else 0f)
    } else { 0f }


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
    } else { true }

    private fun updateOpaque(x: Int, y: Int): Boolean {
        if (generating) return Terrain.get(terrains[x][y]).isOpaque()
        if (boundsCheck(x + this.x, y + this.y)) {
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
        if (App.DEBUG_VISIBLE) return debugLight
        if (boundsCheck(x, y)) {
            if (lightCacheDirty[x - this.x][y - this.y]) {
                refreshLightCacheAt(x - this.x, y - this.y)
            }
            return lightCache[x - this.x][y - this.y]
        }
        return level.ambientLight(x, y)
    }

    // Force all cells to re-sum light on next frame.
    fun dirtyAllLightCacheCells() {
        forEachCell { x,y -> lightCacheDirty[x][y] = true }
    }

    private fun refreshLightCacheAt(x: Int, y: Int) {
        val ambient = level.ambientLight(x + this.x, y + this.y)
        lightCache[x][y].r = ambient.r
        lightCache[x][y].g = ambient.g
        lightCache[x][y].b = ambient.b
        lights[x][y].forEach { (light, color) ->
            val flicker = light.flicker()
            lightCache[x][y].r = min(1f, (lightCache[x][y].r + color.r) * flicker)
            lightCache[x][y].g = min(1f, (lightCache[x][y].g + color.g) * flicker)
            lightCache[x][y].b = min(1f, (lightCache[x][y].b + color.b) * flicker)
        }
        lightCacheDirty[x][y] = false
    }

    fun removeLightSource(lightSource: LightSource) {
        forEachCell { x, y ->
            if (lights[x][y].remove(lightSource) != null) {
                lightCacheDirty[x][y] = true
            }
        }
        lightSourceLocations.remove(lightSource)
    }

    private fun forEachCell(doThis: (Int,Int)->Unit) {
        for (x in 0 until width) {
            for (y in 0 until height) {
                doThis(x,y)
            }
        }
    }
}

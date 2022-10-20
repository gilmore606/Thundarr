package world

import actors.Actor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.tilesets.Glyph
import things.LightSource
import things.Thing
import util.*
import world.cartos.RoomyMaze
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

    var x: Int = -999
    var y: Int = -999
    private var x1: Int = -998
    private var y1: Int = -998

    private val seen = Array(width) { Array(height) { false } }
    private val visible = Array(width) { Array(height) { false } }
    private val terrains = Array(width) { Array(height) { Terrain.Type.TERRAIN_BRICKWALL } }
    private val terrainData = Array(width) { Array(height) { "" } }
    private val things = Array(width) { Array(height) { mutableListOf<Thing>() } }

    private val savedActors: MutableSet<Actor> = mutableSetOf()
    var generating = false

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
    private val debugLight = LightColor(1f, 1f, 1f)

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
    }

    fun tempPlayerStart(): XY {
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
        this.level = level

        savedActors.forEach { actor -> level.director.attachActor(actor) }

        // Find and reproject all lights.
        for (x in 0 until width) {
            for (y in 0 until height) {
                things[x][y].forEach { thing ->
                    if (thing.light() != null) {
                        level.dirtyLights[thing] = XY(x + this.x, y + this.y)
                    }
                }
            }
        }
    }

    fun generateWorld() {
        generating = true
        WorldCarto().carveLevel(x, y, x + width - 1, y + height - 1, this)
        generating = false
    }

    fun generateLevel(building: Building) {
        generating = true
        RoomyMaze().carveLevel(0, 0, building.floorWidth - 1, building.floorHeight - 1, this)
        generating = false
    }

    fun unload(saveActors: Set<Actor>) {
        log.debug("Chunk ${x}x${y} unloading")
        savedActors.addAll(saveActors)

        val lightsToDispose = mutableSetOf<LightSource>()
        lightSourceLocations.forEach { (it, _) -> lightsToDispose.add(it) }
        lightsToDispose.forEach { level.removeLightSource(it) }

        ChunkLoader.saveWorldChunk(this)
    }

    private inline fun boundsCheck(x: Int, y: Int) = !(x < this.x || y < this.y || x > this.x1 || y > this.y1)

    fun addThingAt(x: Int, y: Int, thing: Thing) {
        things[x - this.x][y - this.y].add(thing)
        if (!generating) {
            updateOpaque(x - this.x, y - this.y)
            updateWalkable(x - this.x, y - this.y)
        }
        thing.light()?.also { projectLightSource(XY(x, y), thing) }
    }

    fun removeThingAt(x: Int, y: Int, thing: Thing) {
        things[x - this.x][y - this.y].remove(thing)
        updateOpaque(x - this.x, y - this.y)
        updateWalkable(x - this.x, y - this.y)
        thing.light()?.also { level.removeLightSource(thing) }
    }

    fun thingsAt(x: Int, y: Int) = if (boundsCheck(x, y)) {
        things[x - this.x][y - this.y]
    } else { noThing }

    fun getTerrain(x: Int, y: Int) = if (boundsCheck(x, y)) {
        terrains[x - this.x][y - this.y]
    } else { Terrain.Type.TERRAIN_BRICKWALL }

    fun setTerrain(x: Int, y: Int, type: Terrain.Type) {
        terrains[x - this.x][y - this.y] = type
        if (!generating) {
            updateOpaque(x - this.x, y - this.y)
            updateWalkable(x - this.x, y - this.y)
        }
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
        var v = Terrain.get(terrains[x][y]).isWalkable()
        if (!v) {
            var thingBlocking = false
            things[x][y].forEach { thing ->
                thingBlocking = thingBlocking || thing.isBlocking
            }
            v = thingBlocking
        }
        walkableCache[x][y] = v
        return v
    }

    fun visibilityAt(x: Int, y: Int): Float = if (App.DEBUG_VISIBLE) 1f else if (boundsCheck(x, y)) {
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
        for (x in 0 until width) {
            for (y in 0 until height) {
                visible[x][y] = false
            }
        }
    }

    fun clearSeen() {
        for (x in 0 until width) {
            for (y in 0 until height) {
                seen[x][y] = false
            }
        }
    }

    fun isOpaqueAt(x: Int, y: Int): Boolean = if (boundsCheck(x, y)) {
        opaqueCache[x - this.x][y - this.y] ?: updateOpaque(x - this.x, y - this.y)
    } else { true }

    private fun updateOpaque(x: Int, y: Int): Boolean {
        var v = Terrain.get(terrains[x][y]).isOpaque()
        if (!v) {
            var thingBlocking = false
            things[x][y].forEach { thing ->
                thingBlocking = thingBlocking || thing.isOpaque
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
            })
        }
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

    fun lightAt(x: Int, y: Int): LightColor {
        if (App.DEBUG_VISIBLE) return debugLight
        if (boundsCheck(x, y)) {
            if (lightCacheDirty[x - this.x][y - this.y]) {
                refreshLightCacheAt(x - this.x, y - this.y)
            }
            return lightCache[x - this.x][y - this.y]
        }
        return level.ambientLight()
    }

    private fun refreshLightCacheAt(x: Int, y: Int) {
        val ambient = level.ambientLight()
        lightCache[x][y].r = ambient.r
        lightCache[x][y].g = ambient.g
        lightCache[x][y].b = ambient.b
        lights[x][y].forEach { (_, color) ->
            lightCache[x][y].r = min(1f, lightCache[x][y].r + color.r)
            lightCache[x][y].g = min(1f, lightCache[x][y].g + color.g)
            lightCache[x][y].b = min(1f, lightCache[x][y].b + color.b)
        }
        lightCacheDirty[x][y] = false
    }

    fun refreshLight() {
        for (x in 0 until width) {
            for (y in 0 until height) {
                lightCacheDirty[x][y] = true
            }
        }
    }

    fun removeLightSource(lightSource: LightSource) {
        for (x in 0 until width) {
            for (y in 0 until height) {
                if (lights[x][y].remove(lightSource) != null) {
                    lightCacheDirty[x][y] = true
                }
            }
        }
        lightSourceLocations.remove(lightSource)
    }

}

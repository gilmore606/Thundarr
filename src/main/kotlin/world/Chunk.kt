package world

import App.saveFileFolder
import actors.Actor
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ktx.async.KtxAsync
import render.tilesets.Glyph
import things.LightSource
import things.Thing
import util.*
import world.cartos.PerlinCarto
import world.terrains.Terrain
import java.io.File
import java.lang.Float.min


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
    private val things = Array(width) { Array(height) { mutableListOf<Thing>() } }

    private val savedActors: MutableSet<Actor> = mutableSetOf()

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
    private val walkableCache = Array(width) { Array<Boolean?>(height) { null } }
    @Transient
    private val opaqueCache = Array(width) { Array<Boolean?>(height) { null } }

    @Transient private val noThing = ArrayList<Thing>()

    companion object {
        fun filepathAt(x: Int, y: Int) = saveFileFolder + "/chunk" + x.toString() + "x" + y.toString() + ".json.gz"
        fun allFiles() = File(saveFileFolder).listFiles()?.filter { it.name.startsWith("chunk") }
    }

    private fun filepath() = filepathAt(x, y)

    fun onCreate(level: Level, x: Int, y: Int, forWorld: Boolean) {
        this.level = level
        this.x = x
        this.y = y
        this.x1 = x + width - 1
        this.y1 = y + height - 1
        if (forWorld) {
            generateWorld()
        }
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

        for (x in 0 until width) {
            for (y in 0 until height) {
                things[x][y].forEach { thing ->
                    if (thing.light() != null) {
                        addLightSource(XY(x + this.x,y + this.y), thing)
                    }
                }
            }
        }
    }

    private fun generateWorld() {
        PerlinCarto.carveLevel(x, y, x + width - 1, y + height - 1, { ix, iy ->
            getTerrain(ix, iy)
        }, { ix, iy, type ->
            setTerrain(ix, iy, type)
        })

        for (x in 0 until width) {
            for (y in 0 until height) {
                if (isWalkableAt(x + this.x, y + this.y)) {
                    val n = Perlin.noise(x * 0.02, y * 0.02, 0.01)
                    if (Dice.chance(n.toFloat() * 2.5f)) {
                        addThingAt(x + this.x, y + this.y, Thing(
                            Glyph.TREE,
                            true, false
                        ))
                    }
                }
            }
        }
    }

    fun generateLevel(doGenerate: (Chunk)->Unit): Chunk {
        this.x = 0
        this.y = 0
        this.x1 = width - 1
        this.y1 = height - 1
        doGenerate(this)
        return this
    }

    fun unload(saveActors: Set<Actor>) {
        savedActors.addAll(saveActors)

        val lightsToDispose = mutableSetOf<LightSource>()
        lightSourceLocations.forEach { (it, _) -> lightsToDispose.add(it) }
        lightsToDispose.forEach { level.removeLightSource(it) }

        KtxAsync.launch {
            File(filepath()).writeBytes(
                Json.encodeToString(this@Chunk).gzipCompress()
            )
        }
    }

    private inline fun boundsCheck(x: Int, y: Int) = !(x < this.x || y < this.y || x > this.x1 || y > this.y1)

    fun addThingAt(x: Int, y: Int, thing: Thing) {
        things[x - this.x][y - this.y].add(thing)
        updateOpaque(x - this.x, y - this.y)
        updateWalkable(x - this.x, y - this.y)
        thing.light()?.also { addLightSource(XY(x, y), thing) }
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
        updateOpaque(x - this.x, y - this.y)
        updateWalkable(x - this.x, y - this.y)
    }

    fun getGlyph(x: Int, y: Int) = if (boundsCheck(x, y)) {
        Terrain.get(terrains[x - this.x][y - this.y]).glyph()
    } else { Glyph.FLOOR }

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
            reprojectLightsTouching(x, y)
            level.shadowDirty = true
        }
        return v
    }

    // TODO: don't remove/add these right now.  Add them to a list to-be-reprojected
    // Process that list before a render/redraw? and then clear it
    private fun reprojectLightsTouching(x: Int, y: Int) {
        val workList = mutableMapOf<LightSource,XY>()
        lights[x][y].forEach { (lightSource, _) ->
            lightSourceLocations[lightSource]?.also { location ->
                workList[lightSource] = location
            }
        }
        workList.forEach { (lightSource, location) ->
            level.removeLightSource(lightSource)
            addLightSource(location, lightSource)
        }
    }

    // Project light from location into all nearby cells.
    private fun addLightSource(xy: XY, lightSource: LightSource) {
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

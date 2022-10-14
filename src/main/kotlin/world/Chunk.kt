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
import things.Thing
import util.*
import world.cartos.PerlinCarto
import world.terrains.Terrain
import java.io.File


@Serializable
class Chunk(
    val width: Int, val height: Int
) {
    var x: Int = -999
    var y: Int = -999
    private var x1: Int = -998
    private var y1: Int = -998

    private val seen = Array(width) { Array(height) { false } }
    private val visible = Array(width) { Array(height) { false } }
    private val terrains = Array(width) { Array(height) { Terrain.Type.TERRAIN_BRICKWALL } }
    private val things = Array(width) { Array<MutableList<Thing>>(height) { mutableListOf() } }

    private val savedActors: MutableSet<Actor> = mutableSetOf()

    @Transient
    lateinit var level: Level

    @Transient
    private val lights = Array(width) { Array(height) { mutableMapOf<Thing, LightColor>() } }
    @Transient
    private val light = Array(width) { Array(height) { LightColor(0f, 0f, 0f) } }
    @Transient
    private val lightDirty = Array(width) { Array(height) { true } }
    @Transient
    private val lightCaster = ShadowCaster()

    @Transient
    private val walkableCache = Array(width) { Array<Boolean?>(height) { null } }
    @Transient
    private val opaqueCache = Array(width) { Array<Boolean?>(height) { null } }


    companion object {
        fun filepathAt(x: Int, y: Int) = saveFileFolder + "/chunk" + x.toString() + "x" + y.toString() + ".json.gz"
        fun allFiles() = File(saveFileFolder).listFiles()?.filter { it.name.startsWith("chunk") }
    }

    private fun filepath() = filepathAt(x, y)

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

    fun generateWorldAt(x: Int, y: Int) {
        this.x = x
        this.y = y
        this.x1 = x + width - 1
        this.y1 = y + height - 1

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
                        addThingAt(x, y, Thing(
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
        KtxAsync.launch {
            File(filepath()).writeBytes(
                Json.encodeToString(this@Chunk).gzipCompress()
            )
        }
    }

    fun getSavedActors() = savedActors

    private inline fun boundsCheck(x: Int, y: Int) = !(x < this.x || y < this.y || x > this.x1 || y > this.y1)

    fun addThingAt(x: Int, y: Int, thing: Thing) {
        things[x][y].add(thing)
        updateOpaque(x, y)
        updateWalkable(x, y)
        thing.light()?.also { addLight(x, y, thing) }
    }

    fun getThingsAt(x: Int, y: Int) = if (boundsCheck(x, y)) {
        things[x - this.x][y - this.y]
    } else { ArrayList() }

    fun getTerrain(x: Int, y: Int) = if (boundsCheck(x, y)) {
        terrains[x - this.x][y - this.y]
    } else { Terrain.Type.TERRAIN_BRICKWALL }

    fun setTerrain(x: Int, y: Int, type: Terrain.Type) {
        terrains[x - this.x][y - this.y] = type
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
        opaqueCache[x][y] = v
        return v
    }

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

    // Project light from location into all nearby cells.
    fun addLight(x: Int, y: Int, thing: Thing) {
        thing.light()?.also { lightColor ->
            lightCaster.castLight(x, y, lightColor, { x, y ->
                level.isOpaqueAt(x, y)
            }, { x, y, r, g, b ->
                level.receiveLight(x, y, thing, r, g, b)
            })
        }
    }

    // Receive projected light from a source and save it in cache.
    fun receiveLight(x: Int, y: Int, thing: Thing, r: Float, g: Float, b: Float) {
        if (boundsCheck(x, y)) {
            lightDirty[x - this.x][y - this.y] = true
            if (lights[x - this.x][y - this.y].contains(thing)) {
                lights[x - this.x][y - this.y][thing]?.r = r
                lights[x - this.x][y - this.y][thing]?.g = g
                lights[x - this.x][y - this.y][thing]?.b = b
            } else {
                lights[x - this.x][y - this.y][thing] = LightColor(r, g, b)
            }
        }
    }

    fun lightAt(x: Int, y: Int): LightColor {
        if (boundsCheck(x, y)) {
            if (lightDirty[x - this.x][y - this.y]) {
                refreshLightAt(x - this.x, y - this.y)
            }
            return light[x - this.x][y - this.y]
        }
        return level.ambientLight()
    }

    private fun refreshLightAt(x: Int, y: Int) {
        val ambient = level.ambientLight()
        light[x][y].r = ambient.r
        light[x][y].g = ambient.g
        light[x][y].b = ambient.b
        lights[x][y].forEach { lightObject, color ->
            light[x][y].r += color.r
            light[x][y].g += color.g
            light[x][y].b += color.b
        }
        lightDirty[x][y] = false
    }
}

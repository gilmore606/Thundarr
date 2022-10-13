package world

import App.saveFileFolder
import actors.Actor
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ktx.async.KtxAsync
import render.tilesets.Glyph
import things.Thing
import util.Dice
import util.XY
import util.gzipCompress
import world.cartos.PerlinCarto
import world.terrains.Terrain
import java.io.File

const val CHUNK_SIZE = 64

@Serializable
class Chunk {
    var x: Int = -999
    var y: Int = -999
    private var x1: Int = -998
    private var y1: Int = -998

    private val seen = Array(CHUNK_SIZE) { Array(CHUNK_SIZE) { false } }
    private val visible = Array(CHUNK_SIZE) { Array(CHUNK_SIZE) { false } }
    private val terrains = Array(CHUNK_SIZE) { Array(CHUNK_SIZE) { Terrain.Type.TERRAIN_BRICKWALL } }
    private val things = Array(CHUNK_SIZE) { Array<MutableList<Thing>>(CHUNK_SIZE) { mutableListOf() } }

    private val noThing = ArrayList<Thing>()

    private val savedActors: MutableSet<Actor> = mutableSetOf()


    fun tempPlayerStart(): XY {
        var tries = 5000
        while (tries > 0) {
            val x = Dice.range(x, x + CHUNK_SIZE - 1)
            val y = Dice.range(y, y + CHUNK_SIZE - 1)
            if (isWalkableAt(x, y)) return XY(x,y)
            tries--
        }
        throw RuntimeException("No space to put player in level!")
    }

    fun generateAtLocation(x: Int, y: Int) {
        this.x = x
        this.y = y
        this.x1 = x + CHUNK_SIZE - 1
        this.y1 = y + CHUNK_SIZE - 1

        PerlinCarto.carveLevel(x, y, x + CHUNK_SIZE - 1, y + CHUNK_SIZE - 1, { ix, iy ->
            getTerrain(ix, iy)
        }, { ix, iy, type ->
            setTerrain(ix, iy, type)
        })

        repeat (20) {
            things[Dice.zeroTil(CHUNK_SIZE)][Dice.zeroTil(CHUNK_SIZE)].add(
                Thing(
                    Glyph.TREE, true, true
                )
            )
        }
    }

    fun unload(saveActors: Set<Actor>) {
        savedActors.addAll(saveActors)
        KtxAsync.launch {
            File("$saveFileFolder/chunk$x=$y.json.gz").writeBytes(
                Json.encodeToString(this@Chunk).gzipCompress()
            )
        }
    }

    fun getSavedActors() = savedActors

    private inline fun boundsCheck(x: Int, y: Int) = !(x < this.x || y < this.y || x > this.x1 || y > this.y1)

    fun getThingsAt(x: Int, y: Int) = if (boundsCheck(x, y)) {
        things[x - this.x][y - this.y]
    } else { noThing }

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
        Terrain.get(terrains[x - this.x][y - this.y]).isWalkable()
    } else { false }

    fun visibilityAt(x: Int, y: Int): Float = if (App.DEBUG_VISIBLE) 1f else if (boundsCheck(x, y)) {
        (if (seen[x - this.x][y - this.y]) 0.6f else 0f) +
                (if (visible[x - this.x][y - this.y]) 0.4f else 0f)
    } else { 0f }

    fun isOpaqueAt(x: Int, y: Int): Boolean = if (boundsCheck(x, y)) {
        Terrain.get(terrains[x - this.x][y - this.y]).isOpaque()
    } else { true }

    fun setTileVisibility(x: Int, y: Int, vis: Boolean) {
        if (boundsCheck(x, y)) {
            visible[x - this.x][y - this.y] = vis
            if (vis) seen[x - this.x][y - this.y] = true
        }
    }

    fun clearVisibility() {
        for (x in 0 until CHUNK_SIZE) {
            for (y in 0 until CHUNK_SIZE) {
                visible[x][y] = false
            }
        }
    }

    fun clearSeen() {
        for (x in 0 until CHUNK_SIZE) {
            for (y in 0 until CHUNK_SIZE) {
                seen[x][y] = false
            }
        }
    }
}

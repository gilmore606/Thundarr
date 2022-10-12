package world

import App.saveFileFolder
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ktx.async.KtxAsync
import render.tilesets.Glyph
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

    private val seen = Array(CHUNK_SIZE) { Array(CHUNK_SIZE) { false } }
    private val visible = Array(CHUNK_SIZE) { Array(CHUNK_SIZE) { false } }
    private val terrains = Array(CHUNK_SIZE) { Array(CHUNK_SIZE) { Terrain.Type.TERRAIN_BRICKWALL } }

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
        PerlinCarto.carveLevel(x, y, x + CHUNK_SIZE - 1, y + CHUNK_SIZE - 1, { ix, iy ->
            getTerrain(ix, iy)
        }, { ix, iy, type ->
            setTerrain(ix, iy, type)
        })
    }

    fun unload() {
        KtxAsync.launch {
            File("$saveFileFolder/chunk$x=$y.json.gz").writeBytes(
                Json.encodeToString(this@Chunk).gzipCompress()
            )
        }
    }

    fun getTerrain(x: Int, y:Int) = try {
        terrains[x - this.x][y - this.y]
    } catch (e: ArrayIndexOutOfBoundsException) {
        Terrain.Type.TERRAIN_BRICKWALL
    }

    fun setTerrain(x: Int, y: Int, type: Terrain.Type) {
        terrains[x - this.x][y - this.y] = type
    }

    fun getGlyph(x: Int, y: Int) = try {
        Terrain.get(terrains[x - this.x][y - this.y]).glyph()
    } catch (e: ArrayIndexOutOfBoundsException) {
        Glyph.FLOOR
    }

    fun isSeenAt(x: Int, y: Int): Boolean = try {
        seen[x - this.x][y - this.y]
    } catch (e: ArrayIndexOutOfBoundsException) { false }

    fun isWalkableAt(x: Int, y: Int): Boolean = try {
        Terrain.get(terrains[x - this.x][y - this.y]).isWalkable()
    } catch (e: ArrayIndexOutOfBoundsException) { false }

    fun visibilityAt(x: Int, y: Int): Float = if (App.DEBUG_VISIBLE) 1f else try {
        (if (seen[x - this.x][y - this.y]) 0.6f else 0f) +
                (if (visible[x - this.x][y - this.y]) 0.4f else 0f)
    } catch (e: ArrayIndexOutOfBoundsException) { 0f }

    fun isOpaqueAt(x: Int, y: Int): Boolean = try {
        Terrain.get(terrains[x - this.x][y - this.y]).isOpaque()
    } catch (e: ArrayIndexOutOfBoundsException) { true }

    fun setTileVisibility(x: Int, y: Int, vis: Boolean) {
        try {
            visible[x - this.x][y - this.y] = vis
            if (vis) seen[x - this.x][y - this.y] = true
        } catch (_: ArrayIndexOutOfBoundsException) { }
    }

    fun clearVisibility() {
        for (x in 0 until CHUNK_SIZE) {
            for (y in 0 until CHUNK_SIZE) {
                visible[x][y] = false
            }
        }
    }
}

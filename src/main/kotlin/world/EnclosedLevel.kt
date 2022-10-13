package world

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.tilesets.Glyph
import things.Thing
import util.Dice
import util.StepMap
import util.ShadowCaster
import util.XY
import world.terrains.Terrain

@Serializable
class EnclosedLevel(
    val width: Int,
    val height: Int
    ) : Level() {

    private val seen = Array(width) { Array(height) { false } }
    private val visible = Array(width) { Array(height) { false } }
    private val terrains: Array<Array<Terrain.Type>> = Array(width) { Array(height) { Terrain.Type.TERRAIN_BRICKWALL } }
    private val things = Array(width) { Array<MutableList<Thing>>(height) { mutableListOf() } }

    private val noThing = ArrayList<Thing>()

    @Transient override val stepMap = makeStepMap()

    @Transient private val shadowCaster = ShadowCaster(
        { x, y -> isOpaqueAt(x, y) },
        { x, y, vis -> setTileVisibility(x, y, vis) }
    )


    private inline fun boundsCheck(x: Int, y: Int) = !(x < 0 || y < 0 || x >= width || y >= height)

    override fun getThingsAt(x: Int, y: Int) = if (boundsCheck(x, y)) {
        things[x][y]
    } else { noThing }

    override fun getTerrain(x: Int, y:Int) = if (boundsCheck(x, y)) {
        terrains[x][y]
    } else { Terrain.Type.TERRAIN_BRICKWALL }

    override fun setTerrain(x: Int, y: Int, type: Terrain.Type) {
        terrains[x][y] = type
    }

    override fun getGlyph(x: Int, y: Int) = if (boundsCheck(x, y)) {
        Terrain.get(terrains[x][y]).glyph()
    } else { Glyph.FLOOR }

    override fun isSeenAt(x: Int, y: Int): Boolean = if (boundsCheck(x, y)) {
        seen[x][y]
    } else { false }

    override fun isWalkableAt(x: Int, y: Int): Boolean = if (boundsCheck(x, y)) {
        Terrain.get(terrains[x][y]).isWalkable()
    } else { false }

    override fun visibilityAt(x: Int, y: Int): Float = if (App.DEBUG_VISIBLE) 1f else if (boundsCheck(x, y)) {
        (if (seen[x][y]) 0.6f else 0f) + (if (visible[x][y]) 0.4f else 0f)
    } else { 0f }

    override fun isOpaqueAt(x: Int, y: Int): Boolean = if (boundsCheck(x, y)) {
        Terrain.get(terrains[x][y]).isOpaque()
    } else { true }

    override fun setTileVisibility(x: Int, y: Int, vis: Boolean) {
        if (boundsCheck(x, y)) {
            visible[x][y] = vis
            if (vis) seen[x][y] = true
        }
    }

    override fun updateVisibility() {
        for (y in 0 until height) {
            for (x in 0 until width) {
                visible[x][y] = false
            }
        }

        shadowCaster.cast(pov, 12f)
    }

    override fun makeStepMap() = StepMap(width, height) { x, y ->
        isWalkableAt(x, y)
    }

    override fun getPathToPOV(from: XY) = stepMap.pathFrom(from)

    override fun tempPlayerStart(): XY {
        var tries = 5000
        while (tries > 0) {
            val x = Dice.zeroTil(width)
            val y = Dice.zeroTil(height)
            if (isWalkableAt(x, y)) return XY(x,y)
            tries--
        }
        throw RuntimeException("No space to put player in level!")
    }

}

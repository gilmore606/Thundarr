package world.terrains

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.LightColor
import util.log
import world.Chunk
import world.Level

sealed class Floor(
    type: Terrain.Type,
    glyph: Glyph
) : Terrain(type, glyph, true, true, false, dataType = Type.GENERIC_FLOOR) {

    @Serializable class Data(
        var extraQuads: Set<Quad>
    ) : TerrainData(Type.GENERIC_FLOOR)

    @Serializable class Quad(
        val x0: Double,
        val y0: Double,
        val x1: Double,
        val y1: Double,
        val glyph: Glyph,
        val tx0: Float,
        val ty0: Float,
        val tx1: Float,
        val ty1: Float
    )

    open fun overlapsOn(): Set<Terrain.Type> = setOf()
    open fun overlapSize() = 0.25f
    open fun overlapInset() = 0.1f

    fun makeOverlaps(chunk: Chunk, x: Int, y: Int): Data {
        val quads = mutableSetOf<Quad>()
        val size = overlapSize()
        val inset = overlapInset()
        val x0 = x.toDouble()
        val y0 = y.toDouble()

        givesOverlapAt(chunk, x, y-1)?.also { glyph ->
            quads.add(Quad(x0 + inset, y0, x0+1.0-inset, y0+size, glyph, 0f, 0f, 1f, size))
        }
        givesOverlapAt(chunk, x, y+1)?.also { glyph ->
            quads.add(Quad(x0+inset, y0+1.0-size, x0+1.0-inset, y0+1.0, glyph, 0f, 0f, 1f, size))
        }
        givesOverlapAt(chunk, x+1, y)?.also { glyph ->
            quads.add(Quad(x0+1.0-size, y0+inset, x0+1.0, y0+1.0-inset, glyph, 0f, 0f, size, 1f))
        }
        givesOverlapAt(chunk, x-1, y)?.also { glyph ->
            quads.add(Quad(x0, y0+inset, x0+size, y0+1.0-inset, glyph, 0f, 0f, size, 1f))
        }

        return Data(quads)
    }

    private fun givesOverlapAt(chunk: Chunk, x: Int, y: Int) =
        Terrain.get(chunk.getTerrain(x, y)).let { if (it is Floor && it.overlapsOn().contains(this.type)) it.glyph() else null }

    override fun renderExtraQuads(level: Level, x: Int, y: Int, vis: Float, glyph: Glyph, light: LightColor,
                                  doQuad: (x0: Double, y0: Double, x1: Double, y1: Double, tx0: Float, tx1: Float, ty0: Float, ty1: Float,
                                           vis: Float, glyph: Glyph, light: LightColor)->Unit
    ) {
        if (overlapsOn().isEmpty()) return
        level.getTerrainData(x, y)?.let { it as Data }?.also {
            it.extraQuads.forEach { quad ->
                doQuad(quad.x0, quad.y0, quad.x1, quad.y1, quad.tx0, quad.ty0, quad.tx1, quad.ty1, vis, quad.glyph, light)
            }
        }
    }

    override fun debugData(data: TerrainData?): String {
        if (data == null) return "none"
        val mine = data as Data
        return mine.extraQuads.size.toString() + " extra quads"
    }
}



object StoneFloor : Floor(Type.TERRAIN_STONEFLOOR, Glyph.STONE_FLOOR)

object Dirt : Floor(Type.TERRAIN_DIRT, Glyph.DIRT) {
    override fun overlapsOn() = setOf(Type.TERRAIN_STONEFLOOR)
}

object Grass : Floor(Type.TERRAIN_GRASS, Glyph.GRASS) {
    override fun overlapsOn() = setOf(Type.TERRAIN_STONEFLOOR, Type.TERRAIN_DIRT)
}

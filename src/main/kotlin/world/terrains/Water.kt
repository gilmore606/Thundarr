package world.terrains

import render.tilesets.Glyph
import util.*
import world.level.Level


sealed class Water(
    type: Type,
    glyph: Glyph
) : Terrain(type, glyph, false, true, false, false, dataType = Type.GENERIC_WATER){

    override fun renderExtraQuads(level: Level, x: Int, y: Int, vis: Float, glyph: Glyph, light: LightColor,
                                  doQuad: (x0: Double, y0: Double, x1: Double, y1: Double, tx0: Float, ty0: Float, tx1: Float, ty1: Float,
                                           vis: Float, glyph: Glyph, light: LightColor, rotate: Boolean
                                  )->Unit
    ) {
        if (vis < 1f) return
        CARDINALS.forEach { dir ->
            if (get(level.getTerrain(x+dir.x,y+dir.y)) !is Water) {
                when (dir) {
                    WEST -> doQuad(x.toDouble(), y.toDouble(), x.toDouble() + 1.0, y.toDouble() + 1.0, 0f, 0f, 1f, 1f, vis, Glyph.SURF, light, false)
                    EAST -> doQuad(x.toDouble(), y.toDouble(), x.toDouble() + 1.0, y.toDouble() + 1.0, 1f, 0f, 0f, 1f, vis, Glyph.SURF, light, false)
                    NORTH -> doQuad(x.toDouble(), y.toDouble(), x.toDouble() + 1.0, y.toDouble() + 1.0, 0f, 0f, 1f, 1f, vis, Glyph.SURF, light, true)
                    SOUTH -> doQuad(x.toDouble(), y.toDouble(), x.toDouble() + 1.0, y.toDouble() + 1.0, 1f, 0f, 0f, 1f, vis, Glyph.SURF, light, true)
                }
            }
        }
    }

}

object ShallowWater : Water(Type.TERRAIN_SHALLOW_WATER, Glyph.SHALLOW_WATER) {
    override fun isWalkable() = true
}

object DeepWater : Water(Type.TERRAIN_DEEP_WATER, Glyph.DEEP_WATER) {
    override fun isWalkable() = false
}

// Only used in generation, should never appear in the world
object ScratchWater : Water(Type.GENERIC_WATER, Glyph.BLANK) { }

object Lava : Terrain(Type.TERRAIN_LAVA, Glyph.LAVA, false, true, false, false) { }

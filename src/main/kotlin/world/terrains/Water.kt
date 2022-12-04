package world.terrains

import render.tilesets.Glyph


sealed class Water(
    type: Type,
    glyph: Glyph
) : Terrain(type, glyph, false, true, false, dataType = Type.GENERIC_WATER){



}

object ShallowWater : Water(Type.TERRAIN_SHALLOW_WATER, Glyph.SHALLOW_WATER) {
    override fun isWalkable() = true
}

object DeepWater : Water(Type.TERRAIN_DEEP_WATER, Glyph.DEEP_WATER) {
    override fun isWalkable() = false
}

// Only used in generation, should never appear in the world
object ScratchWater : Water(Type.GENERIC_WATER, Glyph.BLANK) { }

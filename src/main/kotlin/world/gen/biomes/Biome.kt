package world.gen.biomes

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.Dice
import util.Perlin
import world.terrains.Terrain
import world.terrains.Terrain.Type.*

@Serializable
sealed class Biome(
    val mapGlyph: Glyph,
    val baseTerrain: Terrain.Type
) {
    open fun terrainAt(x: Int, y: Int): Terrain.Type = baseTerrain
}

@Serializable
object Blank : Biome(
    Glyph.BLANK,
    BLANK
) {

}

@Serializable
object Ocean : Biome(
    Glyph.MAP_WATER,
    TERRAIN_DEEP_WATER
) {

}

@Serializable
object Glacier : Biome(
    Glyph.MAP_GLACIER,
    TERRAIN_DIRT
) {

}

@Serializable
object Plain : Biome(
    Glyph.MAP_PLAIN,
    TERRAIN_GRASS
) {
    override fun terrainAt(x: Int, y: Int): Terrain.Type {
        val offset = 0.0
        val scale = 0.02
        val fullness = 0.002
        val n2 = Perlin.noise(x * 0.02, y * 0.03, 8.12) + Perlin.noise(x * 0.041, y * 0.018, 11.17) * 0.8
        if (n2 > 0.02) return TERRAIN_FORESTWALL
        val n = Perlin.noise((x.toDouble() + offset) * scale, y.toDouble() * scale, 59.0) + Perlin.noise((x.toDouble() + offset) * scale * 0.4, y.toDouble() * scale * 0.4, 114.0) * 0.7
        if (n > fullness * scale - Dice.float(0f, 0.18f).toDouble()) {
            return TERRAIN_GRASS
        }
        return TERRAIN_DIRT
    }
}

@Serializable
object Forest : Biome(
    Glyph.MAP_FOREST,
    TERRAIN_GRASS
) {

}

@Serializable
object Mountain : Biome(
    Glyph.MAP_MOUNTAIN,
    TERRAIN_DIRT
) {

}

@Serializable
object Swamp : Biome(
    Glyph.MAP_SWAMP,
    TERRAIN_SWAMP
) {

}

@Serializable
object Desert : Biome(
    Glyph.MAP_DESERT,
    TERRAIN_SAND
) {

}

@Serializable
object Tundra: Biome(
    Glyph.MAP_PLAIN,
    TERRAIN_DIRT
) {

}

@Serializable
object Ruin : Biome(
    Glyph.MAP_PLAIN,
    TERRAIN_DIRT
) {

}

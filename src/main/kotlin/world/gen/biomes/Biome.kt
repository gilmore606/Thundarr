package world.gen.biomes

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import world.terrains.Terrain
import world.terrains.Terrain.Type.*

@Serializable
sealed class Biome(
    val mapGlyph: Glyph,
    val baseTerrain: Terrain.Type
) {

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
    TERRAIN_BEACH
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

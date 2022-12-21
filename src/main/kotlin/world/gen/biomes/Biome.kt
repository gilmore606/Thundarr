package world.gen.biomes

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.*
import util.Dice
import util.Perlin
import util.Rect
import world.gen.cartos.WorldCarto
import world.terrains.Terrain
import world.terrains.Terrain.Type.*
import java.lang.Float.max
import java.lang.Float.min

@Serializable
sealed class Biome(
    val mapGlyph: Glyph,
    val baseTerrain: Terrain.Type
) {
    open fun terrainAt(x: Int, y: Int): Terrain.Type = baseTerrain
    open fun postBlendProcess(carto: WorldCarto, dir: Rect) { }

    open fun addPlant(fertility: Float, addThing: (Thing)->Unit, addTerrain: (Terrain.Type)->Unit) { }
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
    val forestMin = 0.7f

    override fun addPlant(fertility: Float, addThing: (Thing) -> Unit, addTerrain: (Terrain.Type) -> Unit) {
        if (fertility > forestMin) {
            addTerrain(Terrain.Type.TERRAIN_FORESTWALL)
        } else {
            if (Dice.chance(fertility * 0.3f)) {
                val type = fertility + Dice.float(-0.3f, 0.3f)
                addThing(
                    if (type > 0.7f) OakTree()
                    else if (type > 0.5f) Bush()
                    else if (type > 0.2f) Bush2()
                    else if (Dice.flip()) Flowers1()
                    else Flowers2()
                )
            }
        }
    }

}

@Serializable
object Forest : Biome(
    Glyph.MAP_FOREST,
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

    override fun postBlendProcess(carto: WorldCarto, bounds: Rect) {
        for (x in bounds.x0 .. bounds.x1) {
            for (y in bounds.y0 .. bounds.y1) {
                val wx = x + carto.chunk.x
                val wy = y + carto.chunk.y
                if (carto.getTerrain(wx, wy) == Terrain.Type.TERRAIN_FORESTWALL) {
                    if (carto.neighborCount(wx,wy, Terrain.Type.TERRAIN_FORESTWALL) < 1) {
                        carto.setTerrain(wx,wy,TERRAIN_GRASS)
                    }
                }
            }
        }
    }
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
object Beach : Biome(
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

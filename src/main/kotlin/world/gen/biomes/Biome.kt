package world.gen.biomes

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.*
import util.Dice
import util.Perlin
import util.Rect
import world.gen.NoisePatches
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

    open fun addPlant(fertility: Float, variance: Float, addThing: (Thing)->Unit, addTerrain: (Terrain.Type)->Unit) { }
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
    val forestMin = 0.97f
    val grassMin = 0f

    override fun addPlant(fertility: Float, variance: Float,
                          addThing: (Thing) -> Unit, addTerrain: (Terrain.Type) -> Unit) {
        if (fertility > forestMin + (variance * 0.03f)) {
            addTerrain(Terrain.Type.TERRAIN_FORESTWALL)
        } else if (fertility < grassMin + (variance * 0.003f)) {
            if (Dice.chance(1f - fertility * 800f)) addTerrain(Terrain.Type.TERRAIN_DIRT)
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
    val forestMin = 0.7f
    val treeChance = 0.05f

    override fun terrainAt(x: Int, y: Int): Terrain.Type {
        if (NoisePatches.get("extraForest", x, y) > 0.2f) {
            return Terrain.Type.TERRAIN_FORESTWALL
        }
        return super.terrainAt(x, y)
    }

    override fun addPlant(fertility: Float, variance: Float,
                          addThing: (Thing) -> Unit, addTerrain: (Terrain.Type) -> Unit) {
        if (fertility > forestMin) {
            addTerrain(Terrain.Type.TERRAIN_FORESTWALL)
        } else if (Dice.chance(treeChance)) {
            addThing(OakTree())
        } else {
            if (Dice.chance(fertility * 1f)) {
                val type = fertility + Dice.float(-0.3f, 0.3f)
                addThing(
                    if (type > 0.5f) OakTree()
                    else if (type > 0.2f) Bush()
                    else  Bush2()
                )
            }
        }
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
    override fun terrainAt(x: Int, y: Int): Terrain.Type {
        val v = NoisePatches.get("mountainShapes", x, y)
        if (v > 0.65f) return Terrain.Type.TERRAIN_CAVEWALL
        else if (v < 0.01f) return Terrain.Type.TERRAIN_SAND
        else return Terrain.Type.TERRAIN_DIRT
    }

    override fun postBlendProcess(carto: WorldCarto, bounds: Rect) {
        for (x in bounds.x0 .. bounds.x1) {
            for (y in bounds.y0 .. bounds.y1) {
                val wx = x + carto.chunk.x
                val wy = y + carto.chunk.y
                if (carto.getTerrain(wx, wy) == Terrain.Type.TERRAIN_CAVEWALL) {
                    if (carto.neighborCount(wx,wy, Terrain.Type.TERRAIN_CAVEWALL) < 1) {
                        carto.setTerrain(wx,wy,TERRAIN_DIRT)
                    }
                }
            }
        }
    }
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

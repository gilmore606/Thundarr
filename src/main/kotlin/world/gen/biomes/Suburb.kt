package world.gen.biomes

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.Dice
import world.terrains.Terrain

@Serializable
object Suburb: Biome(
    Glyph.MAP_SUBURB,
    Terrain.Type.TERRAIN_DIRT
) {
    override fun defaultTitle() = "suburban ruins"
    override fun riverBankAltTerrain(x: Int, y: Int) = Terrain.Type.TERRAIN_GRASS
    override fun trailChance() = 0f
    override fun plantDensity() = 0.2f

    override fun terrainAt(x: Int, y: Int): Terrain.Type {
        val fert = fertilityAt(x, y)
        if (fert > 0.65f && Dice.chance((fert - 0.65f) * 0.15f)) {
            return Terrain.Type.TERRAIN_RUBBLE
        }
        if (fert > 0.55f) {
            return Terrain.Type.TERRAIN_PAVEMENT
        }
        return super.terrainAt(x, y)
    }
}

package world.gen.biomes

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.Dice
import world.gen.NoisePatches
import world.gen.habitats.Habitat
import world.terrains.Terrain

@Serializable
object Suburb: Biome(
    Glyph.MAP_SUBURB,
    Terrain.Type.TERRAIN_DIRT
) {
    override fun defaultTitle(habitat: Habitat) = "suburban ruins"
    override fun riverBankAltTerrain(x: Int, y: Int) = Terrain.Type.TERRAIN_GRASS
    override fun trailChance() = 0f
    override fun plantDensity() = 0.2f
    override fun metaTravelCost() = 1.5f
    override fun xpValue() = 4

    override fun terrainAt(x: Int, y: Int): Terrain.Type {
        val fertility = fertilityAt(x, y)
        val fert = 1f - fertility
        if (fert > 0.65f && Dice.chance((fert - 0.65f) * 0.15f)) {
            return Terrain.Type.TERRAIN_RUBBLE
        }
        val grassVariance = NoisePatches.get("metaVariance2", x/10, y/10).toFloat() * 0.005f
        if (fert > 0.55f + grassVariance) {
            return Terrain.Type.TERRAIN_PAVEMENT
        } else if (fert < grassVariance) {
            return Terrain.Type.TERRAIN_UNDERGROWTH
        } else if (fert < grassVariance * 18f) {
            return Terrain.Type.TERRAIN_GRASS
        }
        return super.terrainAt(x, y)
    }
}

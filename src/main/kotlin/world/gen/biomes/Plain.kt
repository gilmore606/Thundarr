package world.gen.biomes

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import world.gen.NoisePatches
import world.gen.habitats.Habitat
import world.terrains.Terrain

@Serializable
object Plain : Biome(
    Glyph.MAP_PLAIN,
    Terrain.Type.TERRAIN_GRASS
) {
    override fun defaultTitle(habitat: Habitat) = habitat.grasslandName()
    override fun riverBankTerrain(x: Int, y: Int) = if (NoisePatches.get("ruinMicro",x,y) > 0.9f) Terrain.Type.TERRAIN_SWAMP else super.riverBankTerrain(x, y)
    override fun plantDensity() = 0.5f
    override fun cabinChance() = 0.001f
    override fun outcroppingChance() = 0.05f
    override fun pondChance() = 0.03f
    override fun xpValue() = 0

    override fun terrainAt(x: Int, y: Int): Terrain.Type {
        val fert = fertilityAt(x, y)
        val variance = NoisePatches.get("metaVariance", x, y).toFloat()
        if (fert > 0.96f + (variance * 0.1f)) {
            return Terrain.Type.TERRAIN_TEMPERATE_FORESTWALL
        } else if (fert < (variance * 0.004f)) {
            return Terrain.Type.TERRAIN_DIRT
        }
        return super.terrainAt(x, y)
    }
}

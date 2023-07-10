package world.gen.biomes

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.Dice
import world.gen.NoisePatches
import world.gen.habitats.Habitat
import world.terrains.Terrain

@Serializable
object Scrub : Biome(
    Glyph.MAP_SCRUB,
    Terrain.Type.TERRAIN_GRASS
) {
    override fun defaultTitle(habitat: Habitat) = habitat.scrubName()
    override fun riverBankAltTerrain(x: Int, y: Int) = if (Dice.chance(0.1f)) Terrain.Type.TERRAIN_ROCKS else Terrain.Type.TERRAIN_GRASS
    override fun plantDensity() = 0.25f
    override fun cabinChance() = 0.005f
    override fun outcroppingChance() = 0.15f
    override fun pondChance() = 0.001f
    override fun xpValue() = 1
    override fun villageFloorType() = if (Dice.flip()) Terrain.Type.TERRAIN_DIRT else Terrain.Type.TERRAIN_WOODFLOOR
    override fun temperatureAmplitude() = 1.15f

    override fun terrainAt(x: Int, y: Int): Terrain.Type {
        val fert = fertilityAt(x, y)
        val variance = NoisePatches.get("metaVariance", x, y).toFloat()
        if (fert < 0.1f + (variance * 0.002f)) {
            return Terrain.Type.TERRAIN_HARDPAN
        } else if (fert < 0.4f + (variance * 0.004f)) {
            return Terrain.Type.TERRAIN_DIRT
        }
        return super.terrainAt(x, y)
    }
}

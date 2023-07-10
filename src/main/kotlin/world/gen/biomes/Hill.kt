package world.gen.biomes

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import world.gen.NoisePatches
import world.gen.cartos.WorldCarto
import world.gen.habitats.Habitat
import world.terrains.Terrain

@Serializable
object Hill : Biome(
    Glyph.MAP_HILL,
    Terrain.Type.TERRAIN_GRASS
) {
    override fun defaultTitle(habitat: Habitat) = "hills"
    override fun trailChance() = 0.2f
    override fun cavesChance() = 0.6f
    override fun cabinChance() = 0.05f
    override fun outcroppingChance() = 0.1f
    override fun pondChance() = 0.005f
    override fun plantDensity() = 0.3f
    override fun riverBankAltTerrain(x: Int, y: Int) = Terrain.Type.TERRAIN_ROCKS
    override fun villageWallType() = Terrain.Type.TERRAIN_BRICKWALL
    override fun villageFloorType() = Terrain.Type.TERRAIN_CAVEFLOOR
    override fun metaTravelCost() = 1.5f
    override fun edgeDistanceThreatFactor() = 1f
    override fun xpValue() = 3
    override fun temperatureBase() = -3
    override fun temperatureAmplitude() = 0.9f

    override fun fertilityAt(x: Int, y: Int) = super.fertilityAt(x, y) - NoisePatches.get("mountainShapes", x, y).toFloat() * 0.6f

    override fun terrainAt(x: Int, y: Int): Terrain.Type {
        val v = NoisePatches.get("mountainShapes", x, y)
        if (v > 0.78f) return Terrain.Type.TERRAIN_CAVEWALL
        else if (v < 0.35f) return Terrain.Type.TERRAIN_GRASS
        else return Terrain.Type.TERRAIN_DIRT
    }

    override fun carveExtraTerrain(carto: WorldCarto) {
        super.carveExtraTerrain(carto)
        carto.fringeTerrain(Terrain.Type.TERRAIN_CAVEWALL, Terrain.Type.TERRAIN_ROCKS, 0.7f, Terrain.Type.GENERIC_WATER)
        repeat (2) { carto.varianceFuzzTerrain(Terrain.Type.TERRAIN_ROCKS, Terrain.Type.TERRAIN_CAVEWALL) }
    }
}

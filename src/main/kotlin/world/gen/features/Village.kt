package world.gen.features

import kotlinx.serialization.Serializable
import things.Shrine
import things.Thing
import things.Well
import util.Dice
import world.gen.cartos.WorldCarto
import world.gen.villagePlantSpawns
import world.path.DistanceMap
import world.terrains.Terrain

@Serializable
class Village(
    val name: String,
) : ChunkFeature(
    3, Stage.BUILD
) {

    override fun doDig() {
        printGrid(growBlob(63, 63), x0, y0, Terrain.Type.TEMP1)
        printGrid(growBlob(52, 52), x0+6, y0+6, Terrain.Type.TEMP2)
        forEachTerrain(Terrain.Type.TEMP2) { x, y ->
            flagsAt(x,y).add(WorldCarto.CellFlag.NO_PLANTS)
        }

        val fertility = Dice.float(0.0f, 1.0f)
        val hutCount = Dice.range(6, 12)
        var built = 0
        var featureBuilt = !Dice.chance(0.8f)
        while (built < hutCount) {
            val width = Dice.range(7, 11)
            val height = Dice.range(7, 11)
            var tries = 0
            var placed = false
            while (tries < 200 && !placed) {
                val x = Dice.range(3, 63 - width)
                val y = Dice.range(3, 63 - height)
                var clearHere = true
                for (tx in x until x+width) {
                    for (ty in y until y+height) {
                        if (getTerrain(x0+tx,y0+ty) !in listOf(Terrain.Type.TEMP1, Terrain.Type.TEMP2)) clearHere = false
                    }
                }
                if (clearHere) {
                    if (!featureBuilt) {
                        buildVillageFeature(x, y, width, height)
                        featureBuilt = true
                    } else {
                        buildHut(x, y, width, height, fertility + Dice.float(-0.3f, 0.3f))
                    }
                    placed = true
                }
                tries++
            }
            built++
        }
        if (Dice.chance(0.7f)) {
            placeInMostPublic(
                if (Dice.chance(0.7f)) Well() else Shrine()
            )
        }
        swapTerrain(Terrain.Type.TEMP1, meta.biome.baseTerrain)
        swapTerrain(Terrain.Type.TEMP2, meta.biome.trailTerrain(x0,y0))
    }

    private fun placeInMostPublic(thing: Thing) {
        fun placeWell(x: Int, y: Int) {
            spawnThing(x, y, thing)
        }
        val distanceMap = DistanceMap(chunk) { x,y ->
            getTerrain(x,y) != Terrain.Type.TEMP2
        }
        var placed = false
        forEachCell { x, y ->
            if (!placed && distanceMap.distanceAt(x, y) == distanceMap.maxDistance) {
                placeWell(x, y)
                placed = true
            }
        }
    }

    private fun buildVillageFeature(x: Int, y: Int, width: Int, height: Int) {
        when (Dice.oneTo(3)) {
            1,2 -> buildGarden(x + 1, y + 1, width - 2, height - 2)
            3 -> buildStage(x + 1, y + 1, width - 2, height - 2)
        }

    }

    private fun buildGarden(x: Int, y: Int, width: Int, height: Int) {
        val isWild = Dice.chance(0.4f)
        val inVertRows = Dice.flip()
        val gardenDensity = Dice.float(0.2f, 0.8f) * 3f
        val villagePlantSpawns = villagePlantSpawns()
        for (tx in x0 + x until x0 + x + width) {
            for (ty in y0 + y until y0 + y + height) {
                flagsAt(tx,ty).add(WorldCarto.CellFlag.NO_PLANTS)
                if ((inVertRows && (tx % 2 == 0)) || (!inVertRows && (ty % 2 == 0)) || isWild) {
                    setTerrain(tx, ty, Terrain.Type.TERRAIN_GRASS)
                    carto.getPlant(meta.biome, meta.habitat, 1f,
                        gardenDensity, villagePlantSpawns)?.also { plant ->
                        spawnThing(tx, ty, plant)
                    }
                }
            }
        }
    }

    private fun buildStage(x: Int, y: Int, width: Int, height: Int) {
        val terrain = listOf(Terrain.Type.TERRAIN_WOODFLOOR, Terrain.Type.TERRAIN_STONEFLOOR).random()
        for (tx in x0 + x until x0 + x + width) {
            for (ty in y0 + y until y0 + y + height) {
                setTerrain(tx, ty, terrain)
            }
        }
    }
}

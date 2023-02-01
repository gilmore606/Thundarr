package world.gen.cartos

import things.Bonepile
import things.Glowstone
import things.LitThing
import things.Thing
import util.*
import world.Building
import world.Chunk
import world.gen.NoisePatches
import world.gen.biomes.Cavern
import world.gen.habitats.TemperateA
import world.level.EnclosedLevel
import world.path.DistanceMap
import world.terrains.Terrain.Type.*

class CavernCarto(
    level: EnclosedLevel,
    val building: Building
) : Carto(0, 0, building.floorWidth() - 1, building.floorHeight() -1, level.chunk!!, level) {

    val waterChance = 0.6f
    val pitsChance = 1.0f
    val globalPlantDensity = 1f

    lateinit var distanceMap: DistanceMap

    fun carveLevel(
        worldDest: XY
    ) {
        carveRoom(Rect(x0, y0, x1, y1), 0, TERRAIN_CAVEWALL)

        when (Dice.oneTo(7)) {
            1 -> carveCellular()
            2 -> carveCellularSmoother()
            3 -> carveCracks(true)
            4 -> carveCracks(false)
            5 -> carveWorm(0)
            6 -> carveWorm(1)
            else -> carveWorm(2)
        }

        var plantChance = 0.5f
        if (Dice.chance(waterChance)) {
            digPools(Dice.oneTo(3))
            deepenWater()
            plantChance = 0.9f
        }
        if (Dice.chance(pitsChance)) {
            digPits(Dice.oneTo(3))
        }

        fillEdges()
        varyFloors()
        setOverlaps()

        val doorPos = addWorldPortal(building, worldDest, TERRAIN_PORTAL_CAVE, "You feel a breeze from the tunnel mouth.\nReturn to the surface?")
        distanceMap = DistanceMap(doorPos, chunk)
        placeTreasure()

        addLights()
        if (Dice.chance(plantChance)) growPlants()

        setAllRoofed(Chunk.Roofed.INDOOR)
    }

    private fun placeTreasure() {
        repeat (Dice.oneTo(3)) {
            placeThing(distanceMap, Bonepile(), distanceMap.maxDistance / 2)
        }
    }

    private fun placeThing(map: DistanceMap, thing: Thing, minDistance: Int) {
        while (true) {
            val x = Dice.zeroTil(width)
            val y = Dice.zeroTil(height)
            if (isWalkableAt(chunk.x + x, chunk.y + y) && map.distanceAt(x,y) >= minDistance) {
                addThing(chunk.x + x, chunk.y + y, thing)
                return
            }
        }
    }

    private fun growPlants() {
        forEachTerrain(TERRAIN_CAVE_ROCKS) { x,y ->
            // TODO : pass in and use actual habitat from overworld
            val fert = Cavern.fertilityAt(x, y)
            getPlant(Cavern, TemperateA, fert, globalPlantDensity)?.also {
                addThing(x, y, it)
            }
        }
    }

    private fun varyFloors() {
        forEachTerrain(TERRAIN_CAVEFLOOR) { x,y ->
            val noise = NoisePatches.get("caveRocks", x, y).toFloat()
            if (Dice.chance(noise * noise * noise)) {
                setTerrain(x, y, TERRAIN_CAVE_ROCKS)
            }
        }
        fuzzTerrain(TERRAIN_CAVE_ROCKS, 0.5f, TERRAIN_CAVEWALL)
    }

    private fun digPits(count: Int) {
        repeat (count) {
            val width = Dice.range(3, 8)
            val height = Dice.range(3, 8)
            val x = Dice.range(x0 + 1, x1 - width)
            val y = Dice.range(y0 + 1, y1 - height)
            printGrid(growBlob(width, height), x, y, TERRAIN_CHASM)
        }
    }

    private fun digPools(count: Int) {
        repeat (count) {
            val width = Dice.range(5, 16)
            val height = Dice.range(5, 16)
            val x = Dice.range(x0 + 1, x1 - width)
            val y = Dice.range(y0 + 1, y1 - height)
            printGrid(growBlob(width, height), x, y, GENERIC_WATER)
            if (Dice.chance(0.7f)) {
                fringeTerrain(GENERIC_WATER, TEMP1, if (Dice.flip()) 1f else Dice.float(0.4f, 1f))
                if (Dice.flip()) fuzzTerrain(TEMP1, 0.8f, GENERIC_WATER)
                swapTerrain(TEMP1, TERRAIN_CAVEFLOOR)
            }
        }
    }

    private fun addLights() {
        level.indoorLight.r = 0f
        level.indoorLight.g = 0f
        level.indoorLight.b = 0f
        val color = LightColor(Dice.float(0.0f, 0.1f),  Dice.float(0.1f, 0.3f), Dice.float(0.1f, 0.4f))
        repeat (Dice.range(5, 200)) {
            val x = Dice.zeroTil(width)
            val y = Dice.zeroTil(height)
            if (chunk.isWalkableAt(x,y) && neighborCount(x,y,TERRAIN_CAVEWALL) > 1) {
                if (!chunk.thingsAt(x,y).hasOneWhere { it is LitThing }) {
                    if (chunk.lightAt(x,y).brightness() < 0.3f) {
                        addThing(x, y, Glowstone().withColor(
                            color.r * Dice.float(0.7f, 1.3f),
                            color.g * Dice.float(0.7f, 1.3f),
                            color.b * Dice.float(0.7f, 1.3f)
                        ))
                    }
                }
            }
        }
    }

    private fun carveCellular() {
        carveRoom(Rect(x0 + 2, y0 + 2, x1 - 2, y1 - 2), 0, TERRAIN_CAVEFLOOR)
        randomFill(x0+1,y0+1,x1-1,y1-1, 0.45f, TERRAIN_CAVEWALL)
        evolve(5, TERRAIN_CAVEWALL, TERRAIN_CAVEFLOOR) { x,y ->
            val r1 = neighborCount(x, y, TERRAIN_CAVEWALL)
            var r2 = 0
            for (dx in -2..2) {
                for (dy in -2 .. 2) {
                    if (boundsCheck(x+dx, y+dy) && getTerrain(x+dx,y+dy) == TERRAIN_CAVEWALL) r2++
                }
            }
            (r1 >=5 || r2 <= 1)
        }
    }

    private fun carveCellularSmoother() {
        carveRoom(Rect(x0 + 2, y0 + 2, x1 - 2, y1 - 2), 0, TERRAIN_CAVEFLOOR)
        randomFill(x0+1,y0+1,x1-1,y1-1, 0.41f, TERRAIN_CAVEWALL)
        evolve(3 + Dice.zeroTo(1), TERRAIN_CAVEWALL, TERRAIN_CAVEFLOOR) { x,y ->
            val r1 = neighborCount(x, y, TERRAIN_CAVEWALL)
            var r2 = 0
            for (dx in -2..2) {
                for (dy in -2 .. 2) {
                    if (boundsCheck(x+dx, y+dy) && getTerrain(x+dx,y+dy) == TERRAIN_CAVEWALL) r2++
                }
            }
            (r1 >= 5 || r2 <= 2)
        }
        evolve(2 + Dice.zeroTo(1), TERRAIN_CAVEWALL, TERRAIN_CAVEFLOOR) { x,y ->
            val r1 = neighborCount(x, y, TERRAIN_CAVEWALL)
            (r1 >= 4)
        }
    }

    private fun carveWorm(fuzz: Int) {
        val cursor = XY(x0 + (x1 - x0) / 2, y0 + (y1 - y0) / 2)
        var steps = ((x1 - x0) * (y1 - y0) * 0.6f).toInt()
        while (steps > 0) {
            steps--
            setTerrain(cursor.x, cursor.y, TERRAIN_CAVEFLOOR)
            val dir = CARDINALS.random()
            cursor.x += dir.x
            cursor.y += dir.y
            if (!innerBoundsCheck(cursor.x, cursor.y)) {
                cursor.x = x0 + (x1 - x0) / 2
                cursor.y = y0 + (y1 - y0) / 2
            }
        }
        repeat (fuzz) { fuzzTerrain(TERRAIN_CAVEFLOOR, Dice.float(0.1f,0.5f)) }
    }

    private fun carveCracks(stayCentered: Boolean) {
        val cursor = XY(x0 + (x1 - x0) / 2, y0 + (y1 - y0) / 2)
        var steps = Dice.range(6,14)
        while (steps > 0) {
            steps--
            val next = XY(Dice.range(x0+2,x1-2),Dice.range(y0+2,y1-2))
            drawLine(cursor, next) { x,y -> setTerrain(x,y,TERRAIN_CAVEFLOOR) }
            if (!stayCentered) {
                cursor.x = next.x
                cursor.y = next.y
            }
        }
        repeat (Dice.range(2,5)) { fuzzTerrain(TERRAIN_CAVEFLOOR, Dice.float(0.1f, 0.4f)) }
    }

    private fun fillEdges() {
        for (x in x0..x1) {
            setTerrain(x, y0, TERRAIN_CAVEWALL)
            setTerrain(x, y1, TERRAIN_CAVEWALL)
        }
        for (y in y0 .. y1) {
            setTerrain(x0, y, TERRAIN_CAVEWALL)
            setTerrain(x1, y, TERRAIN_CAVEWALL)
        }
    }
}

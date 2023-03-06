package world.gen.cartos

import audio.Speaker
import things.*
import util.*
import world.Building
import world.Chunk
import world.gen.NoisePatches
import world.gen.biomes.Cavern
import world.gen.features.Rivers
import world.gen.habitats.TemperateA
import world.level.EnclosedLevel
import world.path.DistanceMap
import world.terrains.Terrain
import world.terrains.Terrain.Type.*
import world.terrains.Water

class CavernCarto(
    level: EnclosedLevel,
    val building: Building
) : Carto(0, 0, building.floorWidth() - 1, building.floorHeight() - 1, level.chunk!!, level) {

    val waterChance = 0.5f
    val pitsChance = 0.5f
    val bridgeChance = 0.5f
    val globalPlantDensity = 1f

    lateinit var distanceMap: DistanceMap

    override val wallTerrain = TERRAIN_CAVEWALL
    override val floorTerrain = TERRAIN_CAVEFLOOR

    fun carveLevel(
        worldDest: XY
    ) {
        carveRoom(Rect(x0, y0, x1, y1), 0, TERRAIN_CAVEWALL)

        val size = (x1 - x0) * (y1 - y0)

        when (Dice.oneTo(7)) {
            1 -> carveCellular()
            2 -> carveCellularSmoother()
            3 -> carveCracks(true)
            4 -> carveCracks(false)
            5 -> carveWorm(0)
            6 -> carveWorm(1)
            7 -> carveWorm(2)
        }

        if (size > 2000 && Dice.chance(pitsChance)) {
            when (Dice.oneTo(3)) {
                1 -> digPits(Dice.oneTo(size / 1000))
                2 -> digPits(1)
                3 -> digRiver(TERRAIN_CHASM)
            }

        }

        var plantChance = 0.5f
        if (size > 2000 && Dice.chance(waterChance)) {
            when (Dice.oneTo(5)) {
                1 -> digPools(Dice.range(2, 5))
                2 -> digPools(1)
                3 -> digLake()
                4,5 -> digRiver(GENERIC_WATER)
            }
            deepenWater()
            plantChance = 0.9f
        }

        fillEdges()
        varyFloors()
        setOverlaps()

        val doorPos = addWorldPortal(building, worldDest, TERRAIN_PORTAL_CAVE, "You feel a breeze from the tunnel mouth.\nReturn to the surface?")
        distanceMap = DistanceMap(chunk, { x,y ->
            x == doorPos.x && y == doorPos.y }, { x,y ->
                isWalkableAt(x, y)
            })
        placeTreasure()

        addLights()
        if (Dice.chance(plantChance)) growPlants()

        setAllRoofed(Chunk.Roofed.INDOOR)
    }

    private fun placeTreasure() {
        repeat (Dice.oneTo(3)) {
            placeThing(distanceMap, if (Dice.chance(0.25f)) Trunk() else Bonepile(), distanceMap.maxDistance / 2)
        }
    }

    private fun placeThing(map: DistanceMap, thing: Thing, minDistance: Int) {
        while (true) {
            val x = Dice.zeroTil(width)
            val y = Dice.zeroTil(height)
            if (isWalkableAt(chunk.x + x, chunk.y + y) && Terrain.get(getTerrain(x,y)) !is Water
                && map.distanceAt(chunk.x + x,chunk.y + y) >= minDistance) {
                spawnThing(chunk.x + x, chunk.y + y, thing)
                return
            }
        }
    }

    private fun growPlants() {
        forEachTerrain(TERRAIN_CAVE_ROCKS) { x,y ->
            // TODO : pass in and use actual habitat from overworld
            var fert = Cavern.fertilityAt(x, y)
            if (neighborCount(x, y, TERRAIN_SHALLOW_WATER) > 0) fert *= 2f
            getPlant(Cavern, TemperateA, fert, globalPlantDensity)?.also {
                spawnThing(x, y, it)
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
            digWaterBlob(width, height)
        }
    }

    private fun digLake() {
        val width = Dice.range(16, 24)
        val height = Dice.range(16, 24)
        digWaterBlob(width, height)
    }

    private fun digWaterBlob(width: Int, height: Int) {
        val x = Dice.range(x0 + 1, x1 - width)
        val y = Dice.range(y0 + 1, y1 - height)
        printGrid(growBlob(width, height), x, y, GENERIC_WATER)
        if (Dice.chance(0.7f)) {
            fringeTerrain(GENERIC_WATER, TEMP1, if (Dice.flip()) 1f else Dice.float(0.4f, 1f))
            if (Dice.flip()) fuzzTerrain(TEMP1, 0.8f, GENERIC_WATER)
            swapTerrain(TEMP1, TERRAIN_CAVEFLOOR)
        }
    }

    private fun digRiver(terrain: Terrain.Type) {
        val startPos: XY
        val endPos: XY
        val startControl: XY
        val endControl: XY
        val startEdge: XY
        val endEdge: XY
        val bridgeVertical: Boolean
        var bridgeOffset: Int
        val bridgeTerrain = if (Dice.flip()) TERRAIN_WOODFLOOR else TERRAIN_CAVEFLOOR
        if (Dice.flip()) {
            startPos =  XY(Dice.range(x0 + 3, x1 - 3) - x0, y0)
            startEdge = NORTH
            startControl = XY(startPos.x + Dice.range(-3, 3), startPos.y + Dice.range(4, 10))
            endPos = XY(Dice.range(x0 + 3, x1 - 3) - x0, y1)
            endEdge = SOUTH
            endControl = XY(endPos.x + Dice.range(-3, 3), endPos.y - Dice.range(4, 10))
            bridgeVertical = true
            bridgeOffset = Dice.range(y0 + 6, y1 - 6)
        } else {
            startPos = XY(x0, Dice.range(y0 + 3, y1 - 3) - y0)
            startEdge = WEST
            startControl = XY(startPos.x + Dice.range(4, 10), startPos.y + Dice.range(-3, 3))
            endPos = XY(x1, Dice.range(y0 + 3, y1 - 3) - y0)
            endEdge = EAST
            endControl = XY(endPos.x - Dice.range(4, 10), endPos.y + Dice.range(-3, 3))
            bridgeVertical = false
            bridgeOffset = Dice.range(x0 + 6, x1 - 6)
        }
        if (!Dice.chance(bridgeChance)) bridgeOffset = -1
        val start = Rivers.RiverExit(startPos, startEdge, Dice.range(2, 6), startControl)
        val end = Rivers.RiverExit(endPos, endEdge, Dice.range(2, 6), endControl)
        var t = 0f
        var width = start.width.toFloat()
        val step = 0.02f
        val widthStep = (end.width - start.width).toFloat() * step
        while (t < 1f) {
            val p = getBezier(t, start.pos.toXYf(), start.control.toXYf(), end.control.toXYf(), end.pos.toXYf())
            carveRiverChunk(Rect((x0 + p.x - width/2).toInt(), (y0 + p.y - width/2).toInt(),
                (x0 + p.x + width/2).toInt(), (y0 + p.y + width/2).toInt()), (width >= 3f), terrain,
                bridgeOffset, bridgeVertical, bridgeTerrain)
            chunk.setSound(x0 + p.x.toInt(), y0 + p.y.toInt(), Speaker.PointAmbience(Speaker.Ambience.RIVER1, 30f, 1f))
            t += step
            width += widthStep
        }
    }

    private fun carveRiverChunk(room: Rect, skipCorners: Boolean, terrain: Terrain.Type,
                                bridgeOffset: Int, bridgeVertical: Boolean, bridgeTerrain: Terrain.Type) {
        for (x in room.x0..room.x1) {
            for (y in room.y0..room.y1) {
                if (x >= x0 && y >= y0 && x <= x1 && y <= y1) {
                    if (!skipCorners || !((x == x0 || x == x1) && (y == y0 || y == y1))) {
                        if ((bridgeVertical && y == bridgeOffset) || (!bridgeVertical && x == bridgeOffset)) {
                            setTerrain(x, y, bridgeTerrain)
                        } else {
                            setTerrain(x, y, terrain)
                        }
                    }
                }
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
            if (chunk.isWalkableAt(x,y) && getTerrain(x,y) != TERRAIN_SHALLOW_WATER && neighborCount(x,y,TERRAIN_CAVEWALL) > 1) {
                if (!chunk.thingsAt(x,y).hasOneWhere { it is LitThing }) {
                    if (chunk.lightAt(x,y).brightness() < 0.3f) {
                        spawnThing(x, y, Glowstone().withColor(
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
            overrideEdge(x, y0)
            overrideEdge(x, y1)
        }
        for (y in y0 .. y1) {
            overrideEdge(x0, y)
            overrideEdge(x1, y)
        }
    }

    private fun overrideEdge(x: Int, y: Int) {
        when (getTerrain(x,y)) {
            TERRAIN_CHASM, TERRAIN_DEEP_WATER -> { }
            TERRAIN_SHALLOW_WATER -> { setTerrain(x, y, TERRAIN_DEEP_WATER) }
            else -> { setTerrain(x, y, TERRAIN_CAVEWALL) }
        }
    }
}

package world.cartos

import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ktx.async.KtxAsync
import things.FilingCabinet
import util.*
import world.Chunk
import world.Level
import world.terrains.PortalDoor
import world.terrains.Terrain
import kotlin.random.Random

class LevelCarto(
    x0: Int,
    y0: Int,
    x1: Int,
    y1: Int,
    chunk: Chunk,
    level: Level
) : Carto(x0, y0, x1, y1, chunk, level) {

    class WorldExit(
        val edge: XY,
        val dest: XY
    )

    fun carveLevel(
        worldExit: WorldExit
    ) {
        val rooms = ArrayList<Rect>()

        val roomTries = Random.nextInt(30, 500)
        val roomBigness = Random.nextInt(1, 4)

        var nextRegion = 0
        repeat (roomTries) {
            val room = randomRect(roomBigness)
            if (rooms.hasNoneWhere { it.isTouching(room) }) {
                rooms.add(room)
                carveRoom(room, nextRegion)

                if (Dice.chance(0.3f)) {
                    addThing(Dice.range(room.x0, room.x1), Dice.range(room.y0, room.y1), FilingCabinet())
                }

                nextRegion++
            }
        }
        for (x in x0+1 until x1) {
            for (y in y0+1 until y1) {
                if (isRock(x, y)) {
                    if (neighborCount(x, y, Terrain.Type.TERRAIN_STONEFLOOR) == 0) {
                        growMaze(x, y, Dice.float(0.1f, 0.6f), nextRegion)
                        nextRegion++
                    }
                }
            }
        }

        connectRegions(Dice.float(0.05f, 0.7f))

        var deadEnds = true
        while (deadEnds) {
            deadEnds = removeDeadEnds()
        }

        addDoor(worldExit)

    }

    private fun growMaze(startX: Int, startY: Int, winding: Float, regionId: Int) {
        val cells = ArrayList<XY>()
        var lastDir = NO_DIRECTION

        carve(startX, startY, regionId)
        cells.add(XY(startX, startY))
        while (cells.isNotEmpty()) {
            val cell = cells.last()
            val openDirs = CARDINALS.filter { canCarve(cell, it) }
            if (openDirs.isNotEmpty()) {
                val dir = if (openDirs.contains(lastDir) && !Dice.chance(winding)) {
                    lastDir
                } else {
                    openDirs.random()
                }
                val dest = cell + dir
                val destNext = cell + dir * 2
                carve(dest, regionId)
                carve(destNext, regionId)
                cells.add(destNext)
                lastDir = dir
            } else {
                cells.removeLast()
                lastDir = NO_DIRECTION
            }
        }
    }

    private fun connectRegions(extraConnectionChance: Float) {

        // Find all cells which connect two regions.
        var maxRegion = 0
        forEachCell { x, y ->
            if ((regionAt(x, y) ?: 0) > maxRegion) {
                maxRegion = regionAt(x, y) ?: 0
            }
        }

        val openRegions = mutableSetOf<Int>().apply { repeat(maxRegion+1) {
                n -> add(n)
        } }

        // Cut connections between regions, until all are one.
        while (openRegions.size > 1) {
            val connections = findConnectorWalls()
            if (connections.isNotEmpty()) {
                val connector = connections.random()
                // Merge all regions this connector touches.
                val regions = regionsTouching(connector).toList()
                val mergedRegion = regions[0]
                mergeRegions(mergedRegion, regions)

                // Remove the merged regions from use.
                openRegions.removeAll(regions)
                openRegions.add(mergedRegion)

                // Dig the connection.
                // TODO: doors and shit.
                carve(connector, mergedRegion)

                if (Dice.chance(extraConnectionChance)) {
                    val extraConnector = connections.random()
                    carve(extraConnector, mergedRegion)
                }
            } else {
                return
            }
        }
    }

    private fun addDoor(worldExit: WorldExit) {
        val door = findEdgeForDoor(worldExit.edge)
        carve(door.x, door.y, 0, Terrain.Type.TERRAIN_PORTAL_DOOR)
        setTerrainData(door.x, door.y, Json.encodeToString(
            PortalDoor.Data(
                enterMsg = "The door leads outside to the wilderness.\nExit the building?",
                levelId = "world",
                xy = XY(worldExit.dest.x, worldExit.dest.y)
        )))
    }


}

package world.cartos

import actors.Peeper
import actors.Ratman
import actors.Ratthing
import things.CeilingLight
import things.FilingCabinet
import things.Fridge
import util.*
import world.Building
import world.Chunk
import world.level.EnclosedLevel
import world.level.Level
import world.terrains.PortalDoor
import world.terrains.Terrain
import java.lang.Float.max
import java.lang.Float.min
import kotlin.random.Random

class LevelCarto(
    x0: Int,
    y0: Int,
    x1: Int,
    y1: Int,
    level: EnclosedLevel,
    val building: Building
) : Carto(x0, y0, x1, y1, level.chunk!!, level) {

    fun carveLevel(
        worldDest: XY
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

                if (Dice.chance(0.6f)) {
                    addThing(Dice.range(room.x0, room.x1), Dice.range(room.y0, room.y1), if (Dice.chance(0.7f)) FilingCabinet() else Fridge())
                }
                if (Dice.chance(0.7f)) {
                    Ratthing().spawnAt(level, Dice.range(room.x0, room.x1), Dice.range(room.y0, room.y1))
                }
                if (Dice.chance(0.1f)) {
                    Ratman().spawnAt(level, Dice.range(room.x0, room.x1), Dice.range(room.y0, room.y1))
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

        connectRegions(Dice.float(0.1f, 0.7f))

        var deadEnds = true
        while (deadEnds) {
            deadEnds = removeDeadEnds()
        }

        addDoor(worldDest)

        addLights()

        forEachCell { x, y -> chunk.setRoofed(x - chunk.x, y - chunk.y, Chunk.Roofed.INDOOR) }
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

    private fun addDoor(worldDest: XY) {
        val door = findEdgeForDoor(building.facing)
        carve(door.x, door.y, 0, Terrain.Type.TERRAIN_PORTAL_DOOR)
        chunk.exits.add(Chunk.ExitRecord(
            Chunk.ExitType.WORLD, door,
            "The door leads outside to the wilderness.\nExit the building?",
            worldDest = worldDest
        ))
    }

    private fun addLights() {
        val hallColor = building.lightColorHalls
        val roomColor = building.lightColorRooms
        val specialColor = building.lightColorSpecial
        val colorVariance = building.lightColorVariance
        val variance = building.lightVariance
        val attempts = building.lightAttempts

        var count = 0
        repeat (attempts) {
            val x = Dice.zeroTil(width)
            val y = Dice.zeroTil(height)
            if (chunk.isWalkableAt(x,y)) {
                if (!chunk.thingsAt(x, y).hasOneWhere { it is CeilingLight }) {
                    if (chunk.lightAt(x, y).brightness() < 0.3f) {
                        val color = if ((!chunk.isWalkableAt(x,y-1) && !chunk.isWalkableAt(x, y+1)) ||
                            (!chunk.isWalkableAt(x-1,y) && !chunk.isWalkableAt(x+1, y))) {
                            hallColor
                        } else if (Dice.chance(0.2f)) {
                            specialColor
                        } else roomColor

                        val v = Dice.float(1f - variance, 1f + variance)
                        color.r = min(1f, max(0f, color.r * Dice.float(1f - colorVariance, 1f + colorVariance) * v))
                        color.g = min(1f, max(0f, color.g * Dice.float(1f - colorVariance, 1f + colorVariance) * v))
                        color.b = min(1f, max(0f, color.b * Dice.float(1f - colorVariance, 1f + colorVariance) * v))
                        addThing(x, y, CeilingLight().withColor(color.r, color.g, color.b))
                        count++
                    }
                }
            }
        }
        log.info("placed $count lights on $attempts attempts in dungeon level")
    }

}

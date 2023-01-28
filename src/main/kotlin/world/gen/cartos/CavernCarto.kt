package world.gen.cartos

import util.CARDINALS
import util.Dice
import util.Rect
import util.XY
import world.Building
import world.level.EnclosedLevel
import world.terrains.Terrain.Type.*

class CavernCarto(
    level: EnclosedLevel,
    val building: Building
) : Carto(0, 0, building.floorWidth() - 1, building.floorHeight() -1, level.chunk!!, level) {

    fun carveLevel(
        worldDest: XY
    ) {
        carveRoom(Rect(x0, y0, x1, y1), 0, TERRAIN_CAVEWALL)

        when (Dice.oneTo(3)) {
            1 -> carveCellular()
            2 -> carveCellularSmoother()
            else -> carveWorm()
        }
        carveWorm()

        addWorldPortal(building, worldDest)
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

    private fun carveWorm() {
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
    }
}

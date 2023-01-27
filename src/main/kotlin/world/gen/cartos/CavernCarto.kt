package world.gen.cartos

import util.Rect
import util.XY
import world.Building
import world.level.EnclosedLevel
import world.terrains.Terrain

class CavernCarto(
    level: EnclosedLevel,
    val building: Building
) : Carto(0, 0, building.floorWidth() - 1, building.floorHeight() -1, level.chunk!!, level) {

    fun carveLevel(
        worldDest: XY
    ) {
        carveRoom(Rect(x0, y0, x1, y1), 0, Terrain.Type.TERRAIN_CAVEWALL)
        carveRoom(Rect(x0 + 2, y0 + 2, x1 - 2, y1 - 2), 0, Terrain.Type.TERRAIN_CAVEFLOOR)

        addWorldPortal(building, worldDest)
    }

}

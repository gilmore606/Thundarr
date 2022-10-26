package actors.actions

import actors.Actor
import util.XY
import world.Level
import world.terrains.Terrain

class BumpTerrain(
    private val xy: XY
) : Action(0.1f) {

    override fun execute(actor: Actor, level: Level) {
        Terrain.get(level.getTerrain(xy.x, xy.y))
            .onBump(actor, level.getTerrainData(xy.x, xy.y))
    }

}

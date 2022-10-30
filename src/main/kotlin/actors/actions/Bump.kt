package actors.actions

import actors.Actor
import actors.animations.Whack
import util.XY
import world.Level
import world.terrains.Terrain

class Bump(
    private val x: Int,
    private val y: Int,
    private val dir: XY
) : Action(0.1f) {

    override fun execute(actor: Actor, level: Level) {
        actor.animation = actors.animations.Bump(dir)
        Terrain.get(level.getTerrain(x + dir.x, y + dir.y))
            .onBump(actor, level.getTerrainData(x + dir.x, y + dir.y))
    }

}

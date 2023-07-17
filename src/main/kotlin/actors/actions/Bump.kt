package actors.actions

import actors.actors.Actor
import kotlinx.serialization.Serializable
import util.XY
import world.level.Level
import world.terrains.Terrain

@Serializable
class Bump(
    private val x: Int,
    private val y: Int,
    private val dir: XY
) : Action(0.1f) {
    override fun name() = "move"

    override fun execute(actor: Actor, level: Level) {
        actor.animation = actors.animations.Bump(dir)
        Terrain.get(level.getTerrain(x + dir.x, y + dir.y))
            .onBump(actor, x + dir.x, y + dir.y, level.getTerrainData(x + dir.x, y + dir.y))
    }

}

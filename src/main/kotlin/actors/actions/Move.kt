package actors.actions

import actors.Actor
import render.sparks.Scoot
import util.XY
import world.Level
import world.terrains.Terrain

class Move(
    private val dir: XY
) : Action(1.0f) {

    override fun execute(actor: Actor, level: Level) {
        if (level.isWalkableFrom(actor.xy, dir)) {
            actor.level?.addSpark(Scoot(dir, actor.glyph()).at(actor.xy.x, actor.xy.y))
            actor.moveTo(level, actor.xy.x + dir.x, actor.xy.y + dir.y)
        } else {
            Terrain.get(level.getTerrain(actor.xy.x + dir.x, actor.xy.y + dir.y))
                .onBump(actor, level.getTerrainData(actor.xy.x + dir.x, actor.xy.y + dir.y))
        }
    }

}

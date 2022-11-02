package actors.actions

import actors.Actor
import render.sparks.Projectile
import render.sparks.ProjectileShadow
import render.sparks.Smoke
import things.Thing
import world.Level

class Throw(
    private val thing: Thing,
    private val x: Int,
    private val y: Int
) : Action(1.0f) {

    override fun execute(actor: Actor, level: Level) {
        level.addSpark(Projectile(thing.glyph(), x, y, 40f) {
            thing.moveTo(level, x, y)
            level.addSpark(Smoke().at(x, y))
            level.actorAt(x, y)?.also { it.takeDamage(thing.thrownDamage()) }
        }.at(actor.xy.x, actor.xy.y))

        level.addSpark(ProjectileShadow(x, y, 40f).at(actor.xy.x, actor.xy.y))
    }

}

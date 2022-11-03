package actors.actions

import actors.Actor
import actors.animations.Hop
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
        actor.animation = Hop()
        level.addSpark(Projectile(thing.glyph(), x, y, 40f) {
            level.addSpark(Smoke().at(x, y))
            thing.onThrownAt(actor, level, x, y)
        }.at(actor.xy.x, actor.xy.y))

        level.addSpark(ProjectileShadow(x, y, 40f).at(actor.xy.x, actor.xy.y))
    }

}

package actors.actions

import actors.Actor
import actors.animations.Hop
import actors.stats.skills.Throw
import audio.Speaker
import render.sparks.Projectile
import render.sparks.ProjectileShadow
import render.sparks.Smoke
import things.Thing
import ui.panels.Console
import util.Dice
import util.XY
import util.distanceBetween
import world.Level

class Throw(
    private val thing: Thing,
    private var x: Int,
    private var y: Int
) : Action(1.0f) {

    override fun execute(actor: Actor, level: Level) {
        actor.animation = Hop()
        Speaker.world(Speaker.SFX.MISS, source = actor.xy)

        val distance = distanceBetween(actor.xy.x, actor.xy.y, x, y)
        val bonus = thing.thrownAccuracy() - (distance * 0.4f) + 2f
        val roll = Throw.resolve(actor, bonus)
        if (roll < 0) {
            x += Dice.range(-1, 1)
            y += Dice.range(-1, 1)
        }

        level.addSpark(Projectile(thing.glyph(), x, y, 40f) {
            level.addSpark(Smoke().at(x, y))
            resolveHit(actor, level, roll)
        }.at(actor.xy.x, actor.xy.y))

        level.addSpark(ProjectileShadow(x, y, 40f).at(actor.xy.x, actor.xy.y))
    }

    private fun resolveHit(actor: Actor, level: Level, roll: Float) {
        Speaker.world(thing.thrownHitSound(), source = XY(x,y))
        level.addSpark(Smoke().at(x, y))
        level.actorAt(x, y)?.also { target ->
            var result = roll
            if (target.canSee(actor)) {
                result = target.tryDodge(actor, thing, roll)
            }
            if (result >= 0) {
                Console.sayAct("%Di hits %dd!", "%Dn throws %ii at %dd, hitting %do!", actor, target, thing)
                val damage = thing.thrownDamage(actor, result)
                target.receiveDamage(damage, actor)
                thing.onThrownOn(actor)
                return
            } else {
                Console.sayAct("%Di misses %dd.", "%Dn throws %ii at %dd, but misses.", actor, target, thing)
            }
        }
        thing.onThrownAt(level, x, y)
    }

}

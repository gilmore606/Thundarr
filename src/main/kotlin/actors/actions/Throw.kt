package actors.actions

import actors.Actor
import actors.animations.Hop
import actors.stats.skills.Throw
import audio.Speaker
import render.sparks.Projectile
import render.sparks.ProjectileShadow
import render.sparks.Smoke
import things.Smashable
import things.Thing
import ui.panels.Console
import util.Dice
import util.XY
import util.distanceBetween
import world.level.Level

class Throw(
    private val thing: Thing,
    private val x: Int,
    private val y: Int
) : Action(1.0f) {
    override fun name() = "throw"

    override fun execute(actor: Actor, level: Level) {
        actor.animation = Hop()
        Speaker.world(Speaker.SFX.MISS, source = actor.xy)

        val distance = distanceBetween(actor.xy.x, actor.xy.y, x, y)
        val bonus = thing.thrownAccuracy() - (distance * 0.4f) + 2f
        val roll = Throw.resolve(actor, bonus)
        var hitx = x
        var hity = y
        if (roll < 0) {
            hitx += Dice.range(-1, 1)
            hity += Dice.range(-1, 1)
            if (!level.isWalkableAt(App.player, hitx, hity)) {
                hitx = x
                hity = y
            }
        }

        level.addSpark(Projectile(thing.glyph(), x, y, 40f) {
            resolveHit(actor, level, hitx, hity, roll)
        }.at(actor.xy.x, actor.xy.y))

        level.addSpark(ProjectileShadow(x, y, 40f).at(actor.xy.x, actor.xy.y))
    }

    private fun resolveHit(actor: Actor, level: Level, hitX: Int, hitY: Int, roll: Float) {
        Speaker.world(thing.thrownHitSound(), source = XY(hitX,hitY))
        level.addSpark(Smoke().at(hitX, hitY))
        level.actorAt(hitX, hitY)?.also { target ->
            var result = roll
            if (target.canSee(actor)) {
                result = target.tryDodge(actor, thing, roll)
            }
            if (result >= 0) {
                Console.sayAct("%Di hits %dd!", "%Dn throws %ii at %dd, hitting %do!", actor, target, thing)
                val damage = thing.thrownDamage(actor, result)
                target.receiveDamage(damage, actor)
                thing.onThrownOn(target)
                return
            } else {
                Console.sayAct("%Di misses %dd.", "%Dn throws %ii at %dd, but misses.", actor, target, thing)
                target.receiveAggression(actor)
            }
        } ?: run {
            if (roll >= 0) {
                level.thingsAt(hitX, hitY).filter { it is Smashable }.randomOrNull()?.also { target ->
                    if (roll > (target as Smashable).sturdiness()) {
                        Console.sayAct("", "%Dn nails %dd, smashing it!", thing, target)
                        target.onSmashSuccess()
                    } else {
                        Console.sayAct("", "%Dn bounces off %dd.", thing, target)
                    }
                }
            }
        }
        thing.onThrownAt(level, hitX, hitY)
    }

}

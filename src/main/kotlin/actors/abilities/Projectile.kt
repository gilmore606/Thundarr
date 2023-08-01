package actors.abilities

import actors.actors.Actor
import actors.animations.Animation
import actors.animations.Hop
import actors.stats.skills.Dodge
import actors.stats.skills.Throw
import audio.Speaker
import kotlinx.serialization.Serializable
import render.sparks.ProjectileShadow
import render.sparks.ProjectileSpark
import render.sparks.Smoke
import render.tilesets.Glyph
import things.Damage
import ui.panels.Console
import util.Dice
import util.distanceBetween
import world.level.Level

@Serializable
class Projectile(
    val projectileCooldown: Double = 0.0,
    val projectileChance: Float = 1f,
    val range: Float,
    val spec: Spec
) : Ability(projectileCooldown, projectileChance) {

    @Serializable
    class Spec(
        val glyph: Glyph,
        val hitMsg: String = "%Dn throws something and hits %dd's %part!",
        val missMsg: String = "%Dn throws something at %dd, but misses.",
        val bounceMsg: String = "%Dn throws something, which bounces off %dd's %part.",
        val damageType: Damage = Damage.CRUSH,
        val damage: Float = 2f,
        val castShadow: Boolean = true,
        val speed: Float = 30f,
        val shootAnimation: Animation = Hop(),
        val shootSound: Speaker.SFX = Speaker.SFX.MISS,
        val hitSound: Speaker.SFX = Speaker.SFX.HIT,
    )

    override fun shouldQueue(actor: Actor, target: Actor): Boolean {
        val distance = distanceBetween(actor.xy, target.xy)
        return (distance > 1f) && (distance <= range)
    }

    override fun execute(actor: Actor, level: Level, target: Actor) {
        actor.animation = spec.shootAnimation
        Speaker.world(spec.shootSound, source = actor.xy)
        level.addSpark(ProjectileSpark(spec.glyph, target.xy.x, target.xy.y, spec.speed) {
            resolveHit(actor, level, target)
        }.at(actor.xy.x, actor.xy.y))
        if (spec.castShadow) {
            level.addSpark(ProjectileShadow(target.xy.x, target.xy.y, spec.speed).at(actor.xy.x, actor.xy.y))
        }
    }

    private fun resolveHit(actor: Actor, level: Level, target: Actor) {
        val distance = distanceBetween(actor.xy, target.xy)
        val part = target.randomBodypart()
        var bonus = 2f - (distance * 0.4f)
        if (target.canActivelyDefend(actor)) {
            bonus -= (Dodge.get(target) - 8f) * 0.5f
        }
        val roll = Throw.resolve(actor, bonus)
        if (roll < 0) {
            Console.sayAct("", spec.missMsg, actor, target)
        } else {
            val damage = Dice.float(spec.damage * 0.5f, spec.damage)
            val finalDamage = part.reduceDamage(target, spec.damageType, damage)
            level.addSpark(Smoke().at(target.xy.x, target.xy.y))
            Speaker.world(spec.hitSound, source = actor.xy)
            if (finalDamage <= 0f) {
                Console.sayAct("", spec.bounceMsg.replace("%part", part.name), actor, target)
            } else {
                Console.sayAct("", spec.hitMsg.replace("%part", part.name), actor, target)
                target.receiveDamage(damage, spec.damageType, part, actor)
            }
        }
    }

}

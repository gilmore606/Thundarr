package actors.actions

import actors.actors.Actor
import actors.actions.events.Event
import actors.animations.Whack
import actors.bodyparts.Bodypart
import actors.stats.skills.Dodge
import actors.stats.skills.Fight
import audio.Speaker
import kotlinx.serialization.Serializable
import render.sparks.Pow
import render.sparks.Smoke
import things.Weapon
import ui.panels.Console
import util.Dice
import util.XY
import world.level.Level

@Serializable
class Attack(
    val targetID: String,
    private val dir: XY,
    private val targetBodypart: Bodypart? = null
): Action(1.0f), Event {
    override fun name() = "attack"

    override fun durationFor(actor: Actor) = super.durationFor(actor) * actor.meleeWeapon().speed()

    override fun execute(actor: Actor, level: Level) {
        App.level.director.getActor(targetID)?.also { target ->
            actor.animation = Whack(dir)
            broadcast(level, actor, target.xy)
            target.receiveAggression(actor)

            val weapon = actor.meleeWeapon()
            val targetWeapon = target.meleeWeapon()
            val canActivelyDefend = target.canActivelyDefend(actor)

            val mySkill = Fight.get(actor) + weapon.skill().get(actor)
            val hisSkill = if (canActivelyDefend) {
                                Fight.get(target) + targetWeapon.skill().get(target)
                            } else 0f

            val difficulty = (actor.xpLevel - target.xpLevel) / 2 +
                    (mySkill - hisSkill) / 2 +
                    weapon.accuracy() -
                    actor.armorEncumbrance() +
                    target.armorEncumbrance() +
                    (targetBodypart?.toHit ?: 0)

            val roll = (weapon.skill().resolve(actor, difficulty) + Fight.resolve(actor, difficulty)) / 2

            if (roll < 0) {

                // MISS
                target.receiveAggression(actor)
                level.addSpark(Smoke().at(actor.xy.x + dir.x, actor.xy.y + dir.y))
                Console.sayAct(weapon.missSelfMsg(), weapon.missOtherMsg(), actor, target, weapon)
                Speaker.world(weapon.missSound(), source = actor.xy)

            } else if (canActivelyDefend && Dodge.resolve(target, roll - 3) > 0) {

                // DODGE
                Console.sayAct("%Dd dodges your %i attack.", "%Dd dodges %dn's %i.", actor, target, weapon)

            } else {

                val bodypart = targetBodypart ?: target.randomBodypart()
                val deflect = bodypart.getDeflect(target)
                if (roll < deflect) {

                    // DEFLECT
                    Console.sayAct(
                        partSub("Your %i glances harmlessly off %dd's %part.", bodypart),
                        partSub("%Dn's %i glances off %dd's %part.", bodypart),
                        actor, target, weapon)
                    Speaker.world(weapon.bounceSound(), source = actor.xy)

                } else {

                    // HIT
                    var damage = bodypart.reduceDamage(target, weapon.damageType(), weapon.rollDamage(roll))
                    if (damage > 0f) {
                        damage = weapon.damageType().addDamage(target, bodypart, damage)
                    }
                    if (damage <= 0f) {

                        // BOUNCE
                        Console.sayAct(
                            partSub("Your %i bounces harmlessly off %dd's %part.", bodypart),
                            partSub("%Dn's %i bounces off %dd's %part.", bodypart),
                            actor, target, weapon)
                        Speaker.world(weapon.bounceSound(), source = actor.xy)

                    } else {

                        // DAMAGE INFLICTED
                        level.addSpark(Pow().at(actor.xy.x + dir.x, actor.xy.y + dir.y))
                        Speaker.world(weapon.hitSound(), source = actor.xy)
                        Console.sayAct(partSub(weapon.hitSelfMsg(), bodypart), partSub(weapon.hitOtherMsg(), bodypart), actor, target, weapon)
                        target.receiveDamage(damage, actor)
                    }
                }
            }
        }

    }

    private fun partSub(m: String, part: Bodypart) = m.replace("%part", part.name)

}

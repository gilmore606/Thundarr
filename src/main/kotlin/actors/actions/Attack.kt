package actors.actions

import actors.Actor
import actors.animations.Whack
import audio.Speaker
import render.sparks.Pow
import render.sparks.Smoke
import ui.panels.Console
import util.Dice
import util.XY
import world.Level

class Attack(
    private val target: Actor,
    private val dir: XY
): Action(1.0f) {

    override fun durationFor(actor: Actor) = super.durationFor(actor) * actor.meleeWeapon().speed()

    override fun execute(actor: Actor, level: Level) {
        actor.animation = Whack(dir)

        val weapon = actor.meleeWeapon()
        val bonus = weapon.accuracy() + weapon.skill().bonus(actor)
        val dodgeMod = target.tryDodge(actor, weapon, bonus)
        val roll = weapon.skill().resolve(actor, dodgeMod)

        if (roll >= 0) {
            val damage = weapon.damage() + Dice.float(0f, weapon.damage())
            val dealt = target.receiveDamage(damage, actor)

            if (dealt > 0f) {
                level.addSpark(Pow().at(actor.xy.x + dir.x, actor.xy.y + dir.y))
                Console.sayAct(weapon.hitSelfMsg(), weapon.hitOtherMsg(), actor, target, weapon)
                Speaker.world(weapon.hitSound(), source = actor.xy)
            } else {
                Console.sayAct("Your %i bounces harmlessly off %dd.", "%Dn's %i bounces harmlessly off %dd.", actor, target, weapon)
                Speaker.world(weapon.bounceSound(), source = actor.xy)
            }
        } else {
            target.receiveAggression(actor)
            level.addSpark(Smoke().at(actor.xy.x + dir.x, actor.xy.y + dir.y))
            Console.sayAct(weapon.missSelfMsg(), weapon.missOtherMsg(), actor, target, weapon)
            Speaker.world(weapon.missSound(), source = actor.xy)
        }
    }

}

package actors.actions

import actors.Actor
import actors.animations.Whack
import actors.stats.Strength
import actors.statuses.Dazed
import actors.statuses.Stunned
import audio.Speaker
import render.sparks.Smoke
import things.Smashable
import ui.panels.Console
import util.Dice
import util.XY
import world.Entity
import world.level.Level

class Smash(
    private val target: Smashable
): Action(1.0f) {

    override fun name() = target.smashVerbName()

    override fun execute(actor: Actor, level: Level) {
        target.xy()?.also { targetLoc ->
            actor.animation = Whack(XY(actor.xy.x - targetLoc.x, actor.xy.y - targetLoc.y))
            level.addSpark(Smoke().at(targetLoc.x, targetLoc.y))
            Speaker.world(Speaker.SFX.HIT, source = actor.xy)
            val roll = Strength.resolve(actor, 0f - target.sturdiness())
            if (roll >= 0) {
                level.addSpark(Smoke().at(targetLoc.x, targetLoc.y))
                Console.sayAct(target.smashSuccessSelfMsg(), target.smashSuccessOtherMsg(), actor, target as Entity)
                target.onSmashSuccess()
            } else {
                Console.sayAct(target.smashFailSelfMsg(), target.smashFailOtherMsg(), actor, target as Entity)
                if (Dice.chance(0.5f)) {
                    actor.addStatus(Stunned())
                }
                if (Dice.chance(0.5f)) {
                    actor.addStatus(Dazed())
                }
                if (Dice.chance(0.3f)) {
                    actor.receiveDamage(Dice.float(1f, 3f))
                }
                target.onSmashFail()
            }
        }

    }

}

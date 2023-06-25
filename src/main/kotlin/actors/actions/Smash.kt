package actors.actions

import actors.Actor
import actors.actions.events.Event
import actors.animations.Whack
import actors.stats.Strength
import actors.statuses.Dazed
import actors.statuses.Stunned
import audio.Speaker
import kotlinx.serialization.Serializable
import render.sparks.Smoke
import things.Smashable
import things.Thing
import ui.panels.Console
import util.Dice
import util.XY
import world.Entity
import world.level.Level

@Serializable
class Smash(
    private val targetKey: Thing.Key,
    private val verbName: String,
): Action(1.0f), Event {

    override fun name() = verbName

    override fun execute(actor: Actor, level: Level) {
        targetKey.getThing(level)?.also { target ->
            if (target is Smashable) {
                target.xy().also { targetLoc ->
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
                    broadcast(level, actor, target.xy())
                }
            }
        }

    }

}

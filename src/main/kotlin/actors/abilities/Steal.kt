package actors.abilities

import actors.actors.Actor
import kotlinx.serialization.Serializable
import util.Dice
import world.level.Level

@Serializable
class Steal(
    val stealCooldown: Double = 0.0,
    val stealChance: Float = 1f,
) : Ability(stealCooldown, stealChance) {

    override fun shouldQueue(actor: Actor, target: Actor) = actor.isNextTo(target)

    override fun execute(actor: Actor, level: Level, target: Actor) {

    }
}

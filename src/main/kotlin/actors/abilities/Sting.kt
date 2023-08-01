package actors.abilities

import actors.actors.Actor
import kotlinx.serialization.Serializable
import util.Dice
import world.level.Level

@Serializable
class Sting(
    val stingCooldown: Double = 0.0,
    val stingChance: Float = 1f,
) : Ability(stingCooldown, stingChance) {

    override fun shouldQueue(actor: Actor, target: Actor) = actor.isNextTo(target)

    override fun execute(actor: Actor, level: Level, target: Actor) {

    }
}

package actors.abilities

import actors.actors.Actor
import kotlinx.serialization.Serializable
import util.Dice
import world.level.Level

@Serializable
class Sting(
    val chance: Float = 1f,
) : Ability() {

    override fun shouldQueue(actor: Actor, target: Actor) = actor.isNextTo(target) && Dice.chance(chance)

    override fun execute(actor: Actor, level: Level, target: Actor) {

    }
}

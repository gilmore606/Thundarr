package actors.abilities

import actors.actors.Actor
import kotlinx.serialization.Serializable
import util.Dice
import util.distanceBetween
import world.level.Level

@Serializable
class Scare(
    val chance: Float = 1f,
    val range: Float = 1f,
) : Ability() {

    override fun shouldQueue(actor: Actor, target: Actor) = (distanceBetween(actor.xy, target.xy) <= range) && Dice.chance(chance)

    override fun execute(actor: Actor, level: Level, target: Actor) {

    }
}

package actors.abilities

import actors.actors.Actor
import kotlinx.serialization.Serializable
import world.level.Level

@Serializable
class Rushdown(
    val rushdownCooldown: Double = 0.0,
    val rushdownChance: Float = 1f,
) : Ability(rushdownCooldown, rushdownChance) {

    override fun shouldQueue(actor: Actor, target: Actor): Boolean {
        return false
    }

    override fun execute(actor: Actor, level: Level, target: Actor) {

    }
}

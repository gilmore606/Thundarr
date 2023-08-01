package actors.abilities

import actors.actors.Actor
import kotlinx.serialization.Serializable
import world.level.Level

@Serializable
class Bloodsuck(
    val bloodsuckCooldown: Double = 0.0,
    val bloodsuckChance: Float = 1f,
) : Ability(bloodsuckCooldown, bloodsuckChance) {

    override fun shouldQueue(actor: Actor, target: Actor): Boolean {
        return false
    }

    override fun execute(actor: Actor, level: Level, target: Actor) {

    }
}

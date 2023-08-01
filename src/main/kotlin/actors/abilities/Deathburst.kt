package actors.abilities

import actors.actors.Actor
import kotlinx.serialization.Serializable
import world.level.Level

@Serializable
class Deathburst(
    val deathburstChance: Float = 1f,
) : Ability(0.0, deathburstChance) {
    override fun shouldQueue(actor: Actor, target: Actor) = false

    override fun execute(actor: Actor, level: Level, target: Actor) { }

    override fun onDie(actor: Actor) {

    }
}

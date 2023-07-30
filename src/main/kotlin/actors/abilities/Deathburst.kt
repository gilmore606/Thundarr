package actors.abilities

import actors.actors.Actor
import kotlinx.serialization.Serializable
import world.level.Level

@Serializable
class Deathburst : Ability() {
    override fun shouldQueue(actor: Actor, target: Actor) = false

    override fun execute(actor: Actor, level: Level, target: Actor) {
        TODO("Not yet implemented")
    }
}

package actors.abilities

import actors.actors.Actor
import kotlinx.serialization.Serializable
import world.level.Level

@Serializable
class Rushdown : Ability() {

    override fun shouldQueue(actor: Actor, target: Actor): Boolean {
        return false
    }

    override fun execute(actor: Actor, level: Level, target: Actor) {
        TODO("Not yet implemented")
    }
}

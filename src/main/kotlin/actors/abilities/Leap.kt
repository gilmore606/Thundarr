package actors.abilities

import actors.actors.Actor
import kotlinx.serialization.Serializable
import ui.panels.Console
import world.level.Level

@Serializable
class Leap : Ability() {

    override fun cooldown() = 3.0

    override fun shouldQueue(actor: Actor, target: Actor) = true

    override fun execute(actor: Actor, level: Level, target: Actor) {
        Console.sayAct("You leap!", "%Dn leaps!", actor)
    }

}

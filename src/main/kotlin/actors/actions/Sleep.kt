package actors.actions

import actors.Actor
import actors.Player
import kotlinx.serialization.Serializable
import ui.panels.Console
import util.Dice
import world.level.Level

@Serializable
class Sleep : Action(5f) {

    override fun name() = "sleep"

    override fun execute(actor: Actor, level: Level) {
        actor.onSleep(duration)
    }
}

package actors.actions

import actors.Actor
import util.log
import world.Level

class WorldAction : Action(1f) {
    override fun execute(actor: Actor, level: Level) {
        App.passTime(1f * level.timeScale())
    }
}

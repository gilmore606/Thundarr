package actors.actions

import actors.Actor
import actors.Player
import util.XY
import world.Level
import ui.panels.ConsolePanel

class Move(
    private val dir: XY
) : Action(1.0f) {

    override fun execute(actor: Actor, level: Level) {
        if (level.isWalkableFrom(actor.xy, dir)) {
            actor.moveTo(actor.xy.x + dir.x, actor.xy.y + dir.y)
        } else {
            if (actor is Player) ConsolePanel.say("You bump into a wall.")
        }
    }

}

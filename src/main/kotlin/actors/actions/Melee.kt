package actors.actions

import actors.Actor
import actors.animations.Whack
import ui.panels.Console
import util.XY
import util.log
import world.Level

class Melee(
    private val target: Actor,
    private val dir: XY
): Action(1.0f) {

    override fun execute(actor: Actor, level: Level) {
        actor.animation = Whack(dir)
        Console.say("Whack.")
    }

}

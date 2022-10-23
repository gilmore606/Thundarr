package actors.actions

import actors.Actor
import actors.Player
import things.Thing
import things.ThingHolder
import ui.panels.ConsolePanel
import util.aOrAn
import world.Level

class Drop(
    private val thing: Thing,
    private val dest: ThingHolder
) : Action(0.3f) {

    override fun execute(actor: Actor, level: Level) {
        thing.moveTo(dest)
        if (actor is Player) {
            ConsolePanel.say("You drop " + thing.name().aOrAn() + ".")
        }
    }

}

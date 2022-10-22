package actors.actions

import actors.Actor
import actors.Player
import things.Thing
import things.ThingHolder
import ui.panels.ConsolePanel
import util.aOrAn
import world.Level

class Get(
    private val thing: Thing,
    private val from: ThingHolder
) : Action(0.5f) {

    override fun execute(actor: Actor, level: Level) {
        thing.moveTo(from, to = actor)
        if (actor is Player) {
            ConsolePanel.say("You pick up " + thing.name().aOrAn() + ".")
        }
    }

}

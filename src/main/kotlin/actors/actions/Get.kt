package actors.actions

import actors.Actor
import things.Thing
import things.ThingHolder
import ui.panels.Console
import world.Level

class Get(
    private val thing: Thing,
    private val from: ThingHolder
) : Action(0.5f) {

    override fun execute(actor: Actor, level: Level) {
        thing.moveTo(actor)
        Console.sayAct("You pick up %id.", "%DN picks up %id.", actor, thing)
    }

}

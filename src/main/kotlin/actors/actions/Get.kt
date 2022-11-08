package actors.actions

import actors.Actor
import things.Container
import things.Thing
import ui.panels.Console
import world.level.Level

class Get(
    private val thing: Thing
) : Action(0.5f) {
    override fun name() = "get things"

    override fun execute(actor: Actor, level: Level) {
        var wasContainer: Container? = null
        if (thing.holder is Container) wasContainer = thing.holder as Container

        thing.moveTo(actor)

        wasContainer?.also { container ->
            Console.sayAct("You get %id from " + container.dname() + ".", "%DN gets %id from " + container.dname() + ".", actor, thing)
        } ?: run {
            Console.sayAct("You pick up %id.", "%DN picks up %id.", actor, thing)
        }
    }

}

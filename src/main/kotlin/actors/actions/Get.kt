package actors.actions

import actors.Actor
import kotlinx.serialization.Serializable
import things.Container
import things.Thing
import ui.panels.Console
import world.level.Level

@Serializable
class Get(
    private val thingKey: Thing.Key
) : Action(0.5f) {
    override fun name() = "get things"

    override fun execute(actor: Actor, level: Level) {
        thingKey.getThing(level)?.also { thing ->
            var wasContainer: Container? = null
            if (thing.holder is Container) wasContainer = thing.holder as Container

            thing.moveTo(actor)

            wasContainer?.also { container ->
                Console.sayAct(
                    "You get %id from " + container.dname() + ".",
                    "%DN gets %id from " + container.dname() + ".",
                    actor, thing
                )
            } ?: run {
                Console.sayAct("You pick up %id.", "%DN picks up %id.", actor, thing)
            }
        }
    }

}

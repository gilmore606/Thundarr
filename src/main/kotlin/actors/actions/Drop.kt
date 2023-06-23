package actors.actions

import actors.Actor
import kotlinx.serialization.Serializable
import things.Container
import things.Thing
import things.ThingHolder
import ui.panels.Console
import world.level.Level

@Serializable
class Drop(
    private val thingKey: Thing.Key,
    private val destKey: ThingHolder.Key,
) : Action(0.3f) {
    override fun name() = "drop things"

    override fun execute(actor: Actor, level: Level) {
        thingKey.getThing(level)?.also { thing ->
            destKey.getHolder(level)?.also { dest ->
                thing.onDropping(actor, dest)
                thing.moveTo(dest)

                if (dest is Container) {
                    Console.sayAct("You put %id into " + dest.dname() + ".", "%DN puts %id into " + dest.dname() + ".", actor, thing)
                } else {
                    Console.sayAct("You drop %id.", "%DN drops %id.", actor, thing)
                }
            }
        }
    }

}

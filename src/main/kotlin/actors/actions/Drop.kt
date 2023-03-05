package actors.actions

import actors.Actor
import things.Container
import things.Thing
import things.ThingHolder
import ui.panels.Console
import world.level.Level

class Drop(
    private val thing: Thing,
    private val dest: ThingHolder
) : Action(0.3f) {
    override fun name() = "drop things"

    override fun execute(actor: Actor, level: Level) {
        thing.onDropping(actor, dest)
        thing.moveTo(dest)

        if (dest is Container) {
            Console.sayAct("You put %id into " + dest.dname() + ".", "%DN puts %id into " + dest.dname() + ".", actor, thing)
        } else {
            Console.sayAct("You drop %id.", "%DN drops %id.", actor, thing)
        }
    }

}

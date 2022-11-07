package actors.actions

import actors.Actor
import things.Thing
import world.level.Level

class Use(
    private val thing: Thing,
    duration: Float,
    private val toDo: (Actor, Level)->Unit
): Action(duration) {

    override fun execute(actor: Actor, level: Level) {
        toDo(actor, level)
    }

}

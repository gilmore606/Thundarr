package actors.actions

import actors.Actor
import things.Thing
import util.XY
import world.level.Level

class Use(
    private val thing: Thing,
    duration: Float,
    private val toDo: (Actor, Level, Int, Int)->Unit,
    private val x: Int,
    private val y: Int
): Action(duration) {
    override fun name() = "use things"

    override fun execute(actor: Actor, level: Level) {
        toDo(actor, level, x, y)
    }

}

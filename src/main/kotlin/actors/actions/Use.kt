package actors.actions

import actors.Actor
import things.Thing
import util.XY
import world.level.Level

class Use(
    val useTag: Thing.UseTag,
    val thing: Thing,
    duration: Float = 1f,
): Action(duration) {
    override fun name() = "use things"

    override fun execute(actor: Actor, level: Level) {
        thing.uses()[useTag]?.toDo?.invoke(actor, level, thing.xy().x, thing.xy().y)
    }

}

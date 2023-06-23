package actors.actions

import actors.Actor
import kotlinx.serialization.Serializable
import things.Thing
import util.XY
import world.level.Level

@Serializable
class Use(
    val useTag: Thing.UseTag,
    val thingKey: Thing.Key,
    private val useDuration: Float = 1f,
): Action(useDuration) {
    override fun name() = "use things"

    override fun execute(actor: Actor, level: Level) {
        thingKey.getThing(level)?.also { thing ->
            thing.uses()[useTag]?.toDo?.invoke(actor, level, thing.xy().x, thing.xy().y)
        }
    }

}

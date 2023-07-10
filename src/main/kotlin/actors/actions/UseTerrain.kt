package actors.actions

import actors.Actor
import actors.actions.events.Event
import kotlinx.serialization.Serializable
import things.Thing
import util.XY
import world.level.Level
import world.terrains.Terrain

@Serializable
class UseTerrain(
    val useTag: Thing.UseTag,
    val xy: XY,
    private val useDuration: Float = 2f,
) : Action(useDuration), Event {
    override fun name() = "use terrain"
    override fun toString() = "UseTerrain($useTag,$xy)"
    override fun eventSenses() = setOf(Event.Sense.VISUAL)

    override fun execute(actor: Actor, level: Level) {
        Terrain.get(level.getTerrain(xy.x, xy.y)).uses()[useTag]?.also { use ->
            use.toDo.invoke(actor, level, xy.x, xy.y)
            broadcast(level, actor, xy)
        }
    }
}

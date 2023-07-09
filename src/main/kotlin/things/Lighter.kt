package things

import actors.Actor
import kotlinx.serialization.Serializable
import render.Screen
import render.tilesets.Glyph
import ui.modals.DirectionModal
import ui.panels.Console
import util.DIRECTIONS
import util.NO_DIRECTION
import util.XY
import util.hasOneWhere
import world.level.Level
import world.stains.Fire

@Serializable
class Lighter : Portable() {
    override val tag = Tag.THING_LIGHTER
    override fun name() = "lighter"
    override fun description() = "A brass cigarette lighter.  Handy for starting fires."
    override fun glyph() = Glyph.LIGHTER
    override fun category() = Category.TOOL
    override fun weight() = 0.02f
    override fun uses() = mapOf(
        UseTag.USE to Use("light fire nearby", 2.0f,
            canDo = { actor,x,y,targ ->
                var canDo = false
                if (actor.xy.x == x && actor.xy.y == y) {
                    DIRECTIONS.forEach { if (hasTargetAt(it.x + x, it.y + y)) canDo = true }
                } else canDo = hasTargetAt(x,y)
                canDo && isHeldBy(actor)
            },
            toDo = { actor, level, x, y ->
                if (actor.xy.x == x && actor.xy.y == y) askDirection(actor, level)
                else lightFireAt(actor, level, XY(x,y))
            })
    )
    override fun toolbarName() = "light fire nearby"
    override fun toolbarUseTag() = UseTag.USE
    override fun spawnContainers() = mutableListOf(Tag.THING_TRUNK, Tag.THING_WRECKEDCAR, Tag.THING_BONEPILE, Tag.THING_TABLE)

    private fun hasTargetAt(x: Int, y: Int): Boolean = holder?.level?.thingsAt(x, y)?.hasOneWhere { it.flammability() > 0f } ?: false

    private fun askDirection(actor: Actor, level: Level) {
        Screen.addModal(DirectionModal("Light a fire in which direction?")
        { xy ->
            if (xy == NO_DIRECTION) {
                Console.say("Are you crazy?  You'd be standing in a fire!")
            } else {
                lightFireAt(actor, level, XY(actor.xy.x + xy.x, actor.xy.y + xy.y))
            }
        })
    }
    private fun lightFireAt(actor: Actor, level: Level, xy: XY) {
        level.addStain(Fire(), xy.x, xy.y)
        Console.sayAct("You start a fire.", "%Dn lights a fire.", actor)
    }
}

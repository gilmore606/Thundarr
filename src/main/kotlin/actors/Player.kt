package actors

import App
import actors.actions.Action
import actors.actions.Move
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import ui.panels.Console
import util.XY
import world.Entity
import world.Level

@Serializable
class Player : Actor() {

    var willAggro: Boolean = false

    override fun glyph() = Glyph.PLAYER
    override fun name() = "Thundarr"
    override fun gender() = Entity.Gender.MALE
    override fun description() = "A stalwart blond barbarian in a leather tunic."

    override fun canAct() = queuedActions.isNotEmpty()
    override fun isActing() = true
    override fun defaultAction(): Action? = null

    override fun statusGlyph(): Glyph? {
        if (willAggro) {
            return Glyph.HOSTILE_ICON
        }
        return null
    }

    fun tryMove(dir: XY) {
        level?.also { level ->
            if (level.isWalkableFrom(xy, dir)) {
                queue(Move(dir))
            } else {
                level.bumpActionTo(xy.x, xy.y, dir)?.also { queue(it) }
            }
        }
    }

    fun toggleAggro() {
        if (willAggro) {
            Console.say("You calm down.")
        } else {
            Console.say("You boil over with rage, ready to smash the next creature you approach!")
        }
        willAggro = !willAggro
    }

    override fun die() {

    }
}

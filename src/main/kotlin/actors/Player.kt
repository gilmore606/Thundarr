package actors

import App
import actors.actions.Action
import actors.actions.Move
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Thing
import ui.panels.Console
import util.XY
import util.englishList
import util.plural
import world.Entity
import world.Level
import world.path.Pather

@Serializable
open class Player : Actor() {

    var willAggro: Boolean = false
    var thrownTag: String = ""
    override fun glyph() = Glyph.PLAYER
    override fun name() = "Thundarr"
    override fun gender() = Entity.Gender.MALE
    override fun description() = "A stalwart blond barbarian in a leather tunic."

    override fun hasActionJuice() = queuedActions.isNotEmpty()
    override fun wantsToAct() = true
    override fun defaultAction(): Action? = null

    init {
        Pather.subscribe(this, this, 100f)
    }

    override fun statusGlyph(): Glyph? {
        if (willAggro) {
            return Glyph.HOSTILE_ICON
        }
        return null
    }

    override fun onMove() {
        super.onMove()
        level?.also { level ->
            val things = level.thingsAt(xy.x, xy.y)
            if (things.isNotEmpty()) {
                Console.say("You see " + things.englishList() + " here.")
            }
        }
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

    override fun willAggro(target: Actor) = willAggro

    fun toggleAggro() {
        if (willAggro) {
            Console.say("You calm down.")
        } else {
            Console.say("You boil over with rage, ready to smash the next creature you approach!")
        }
        willAggro = !willAggro
    }

    fun readyForThrowing(tag: String) {
        thrownTag = tag
        Console.say("You'll throw " + thrownTag.plural() + " now.")
    }

    fun getThrown(): Thing? {
        if (thrownTag == "") return null
        return contents.firstOrNull { it.thingTag() == thrownTag }
    }

    override fun die() {

    }
}

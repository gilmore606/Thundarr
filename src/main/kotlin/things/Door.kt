package things

import actors.Actor
import actors.actions.Action
import actors.actions.Use
import audio.Speaker
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import ui.panels.Console
import util.XY

@Serializable
sealed class Door : Thing() {

    var isOpen = false
    var isLocked = false

    open fun openGlyph() = Glyph.DOOR_OPEN
    open fun closedGlyph() = Glyph.DOOR_CLOSED

    open fun isOpenable() = !isLocked
    open fun isLockable() = false

    override fun isPortable() = false
    override fun isBlocking() = !isOpen
    override fun isOpaque() = !isOpen
    override fun glyph() = if (isOpen) openGlyph() else closedGlyph()

    override fun name() = "door"
    override fun description() = "A wooden door braced with metal."

    override fun uses() = mapOf(
        UseTag.OPEN to Use("open " + name(), 1.0f,
            canDo = { actor,x,y,targ -> isNextTo(actor) && !isOpen },
            toDo = { actor,level,x,y ->
                doOpen()
            }),
        UseTag.CLOSE to Use("close " + name(), 1.0f,
            canDo = { actor,x,y,targ -> isNextTo(actor) && isOpen && !isObstructed() },
            toDo = { actor,level,x,y ->
                doClose()
            }),
        UseTag.DESTROY to Use("kick " + name(), 1.0f,
            canDo = { actor,x,y,targ -> isNextTo(actor) && !isOpen },
            toDo = { actor,level,x,y ->
                doKick(actor)
            }),
        UseTag.USE to Use("knock on " + name(), 1.0f,
            canDo = { actor,x,y,targ -> isNextTo(actor) && !isOpen },
            toDo = { actor,level,x,y ->
                doKnock(actor)
            }),
        UseTag.SWITCH_ON to Use("lock " + name(), 1.0f,
            canDo = { actor,x,y,targ -> isLockable() && isNextTo(actor) && !isOpen && !isLocked },
            toDo = { actor,level,x,y ->

            }),
        UseTag.SWITCH_OFF to Use("unlock " + name(), 1.0f,
            canDo = { actor,x,y,targ -> isLockable() && isNextTo(actor) && !isOpen && isLocked },
            toDo = { actor,level,x,y ->

            })
    )

    override fun bumpAction(): Action? {
        if (!isOpen && isOpenable() && App.player.dangerMode) {
            return Use(UseTag.OPEN, this, 1f, uses()[UseTag.OPEN]!!.toDo, xy()!!.x, xy()!!.y)
        }
        return null
    }

    private fun isObstructed() = xy()?.let { xy -> level()!!.actorAt(xy.x, xy.y) != null || level()!!.thingsAt(xy.x, xy.y).size > 1 } ?: false

    private fun doOpen() {
        isOpen = true
        level()?.onAddThing(xy()!!.x, xy()!!.y, this)
        Speaker.world(Speaker.SFX.DOOR_OPEN, source = this.xy())
    }

    private fun doClose() {
        isOpen = false
        level()?.onAddThing(xy()!!.x, xy()!!.y, this)
        Speaker.world(Speaker.SFX.DOOR_CLOSE, source = this.xy())
    }

    private fun doKnock(actor: Actor) {
        Console.sayAct("You knock politely at %dd.", "%Dn knocks on %dd.", actor, this)
        val soundSource = XY(xy()!!.x - (xy()!!.x - actor.xy.x), xy()!!.y - (xy()!!.y - actor.xy.y))
        Console.sayAct("You hear a knocking at %dn.", "", this, reach = Console.Reach.AUDIBLE, source = soundSource)
    }

    private fun doKick(actor: Actor) {

    }
}

@Serializable
class ModernDoor : Door() {
    override fun description() = "A dull metal sliding door, pitted with corrosion."
}

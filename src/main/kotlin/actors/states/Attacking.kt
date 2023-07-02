package actors.states

import actors.NPC
import actors.actions.Action
import actors.actions.Attack
import actors.actions.Say
import actors.actions.events.Event
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.XY
import util.distanceBetween
import world.path.Pather

@Serializable
class Attacking(
    val targetID: String,
    val origin: XY? = null,
    val maxChaseRange: Int = 0,
    val giveUpText: String? = null,
) : State() {

    var lastLocation: XY? = null

    override fun toString() = "Attacking (target $targetID)"

    override fun allowsConversation() = false

    override fun onEnter(npc: NPC) {
        App.level.director.getActor(targetID)?.also { target ->
            Pather.subscribe(npc, target, npc.visualRange().toInt())
        }
    }

    override fun onLeave(npc: NPC) {
        App.level.director.getActor(targetID)?.also { target ->
            Pather.unsubscribe(npc, target)
        }
    }

    override fun considerState(npc: NPC) {
        npc.apply {
            origin?.also { origin ->
                if (distanceBetween(origin, xy) > maxChaseRange) {
                    giveUp(npc)
                    return
                }
            }
            getActor(targetID)?.also { target ->
                if (!canSee(target)) {
                    lastLocation?.also { lastLocation ->
                        this@Attacking.lastLocation = null
                        say(seekTargetMsg())
                        pushState(Seeking(lastLocation, targetID, origin, maxChaseRange, giveUpText))
                        return
                    } ?: run {
                        say(lostTargetMsg())
                        giveUp(npc)
                        return
                    }
                }
            }
        }
    }

    override fun pickAction(npc: NPC): Action {
        npc.apply {
            getActor(targetID)?.also { target ->
                if (entitiesNextToUs().contains(target)) {
                    return Attack(target.id, target.xy - xy)
                } else if (canSee(target)) {
                    lastLocation = target.xy.copy()
                    stepToward(target)?.also { return it }
                } else if (lastLocation != null) {
                    if (xy == lastLocation) {
                        giveUp(npc)
                    } else {
                        stepToward(lastLocation!!)?.also { return it }
                    }
                }
            }
        }
        return super.pickAction(npc)
    }

    private fun giveUp(npc: NPC) {
        giveUpText?.also { npc.queue(Say(it)) }
        npc.popState()
    }

    override fun drawStatusGlyphs(drawIt: (Glyph) -> Unit) {
        if (targetID == App.player.id) {
            drawIt(Glyph.HOSTILE_ICON)
        } else {
            drawIt(Glyph.HOSTILE_OTHER_ICON)
        }
    }

    override fun idleBounceMs() = 500
    override fun idleBounce() = -0.06f
}

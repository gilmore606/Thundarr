package actors.states

import actors.actors.NPC
import actors.actions.Action
import actors.actions.Say
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.XY
import util.distanceBetween
import path.Pather

@Serializable
class Seeking(
    val lastLocation: XY,
    val targetID: String,
    val origin: XY? = null,
    val maxChaseRange: Int = 0,
    val giveUpText: String? = null,
) : State() {

    override fun toString() = "Seeking (target $targetID)"

    override fun onEnter(npc: NPC) {
        Pather.subscribe(npc, lastLocation, npc.visualRange().toInt())
    }

    override fun onLeave(npc: NPC) {
        Pather.unsubscribe(npc, lastLocation)
    }

    override fun considerState(npc: NPC) {
        npc.apply {
            origin?.also { origin ->
                if (distanceBetween(origin, npc.xy) > maxChaseRange) {
                    giveUp(npc)
                    return
                }
            }
            getActor(targetID)?.also { target ->
                if (canSee(target)) {
                    say(listOf("Aha!", "Got you!", "You can't escape!").random())
                    popState()
                    return
                }
            }
            if (xy == lastLocation) {
                popState()
                return
            }
        }
    }

    private fun giveUp(npc: NPC) {
        giveUpText?.also { npc.queue(Say(it)) }
        npc.popState()
    }

    override fun pickAction(npc: NPC): Action {
        npc.apply {
            stepToward(lastLocation)?.also { return it }
        }
        return super.pickAction(npc)
    }

    override fun drawStatusGlyph(drawIt: (Glyph) -> Unit): Boolean {
        if (targetID == App.player.id) {
            drawIt(Glyph.HOSTILE_ICON)
        } else {
            drawIt(Glyph.HOSTILE_OTHER_ICON)
        }
        return true
    }

    override fun idleBounceMs() = 600

}

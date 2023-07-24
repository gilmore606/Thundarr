package actors.states

import actors.actors.NPC
import actors.actions.Action
import actors.actions.Attack
import actors.actions.Say
import actors.actions.UseAbility
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.XY
import util.distanceBetween
import path.Pather

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

                npc.abilities.forEach { ability ->
                    if (ability.canQueue(npc, target) && ability.shouldQueue(npc, target)) {
                        return UseAbility(ability.id, targetID, ability.durationFor(npc))
                    }
                }

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
            } ?: run { giveUp(npc) }
        }
        return super.pickAction(npc)
    }

    private fun giveUp(npc: NPC) {
        giveUpText?.also { npc.queue(Say(it)) }
        npc.popState()
    }

    override fun drawStatusGlyph(drawIt: (Glyph) -> Unit): Boolean {
        if (targetID == App.player.id) {
            drawIt(Glyph.HOSTILE_ICON)
        } else {
            drawIt(Glyph.HOSTILE_OTHER_ICON)
        }
        return true
    }

    override fun idleBounceMs() = 500
    override fun idleBounce() = -0.06f
}

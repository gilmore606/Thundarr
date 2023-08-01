package actors.abilities

import actors.actors.Actor
import actors.animations.Leap
import kotlinx.serialization.Serializable
import ui.panels.Console
import util.Dice
import util.XY
import util.forXY
import world.Chunk
import world.level.Level

@Serializable
class Leap(
    val leapCooldown: Double = 0.0,
    val leapChance: Float = 1f,
    val range: Int,
) : Ability(leapCooldown, leapChance) {

    override fun shouldQueue(actor: Actor, target: Actor): Boolean {
        // Leap away if wounded
        if (actor.isNextTo(target) && actor.healthiness() < 0.5f) return true
        // Leap away randomly sometimes
        if (Dice.chance(0.2f)) return true
        return false
    }

    override fun execute(actor: Actor, level: Level, target: Actor) {
        pickDestination(actor)?.also { dest ->
            actor.animation = Leap(actor.xy, dest)
            actor.moveTo(level, dest)
            Console.sayAct("You leap!", "%Dn leaps!", actor)
        }
    }

    private fun pickDestination(actor: Actor): XY? {
        val poss = mutableSetOf<XY>()
        val roofed = actor.level?.isRoofedAt(actor.xy.x, actor.xy.y) ?: Chunk.Roofed.OUTDOOR
        forXY(actor.xy.x - range, actor.xy.y - range, actor.xy.x + range, actor.xy.y + range) { dx, dy ->
            if (dx != actor.xy.x && dy != actor.xy.y) {
                if (actor.level?.isWalkableAt(actor, dx, dy) == true) {
                    if (actor.level?.isRoofedAt(dx, dy) == roofed) {
                        poss.add(XY(dx, dy))
                    }
                }
            }
        }
        if (poss.isNotEmpty()) return poss.random()
        return null
    }

}

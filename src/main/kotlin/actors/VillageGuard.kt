package actors

import actors.actions.events.Event
import actors.states.Attacking
import actors.states.IdlePatrol
import actors.states.State
import actors.stats.Brains
import actors.stats.Senses
import actors.stats.Speed
import actors.stats.Strength
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.*
import world.Chunk
import world.gen.features.Habitation
import world.gen.features.Village

@Serializable
class VillageGuard(
    val bounds: Rect,
    val villageName: String,
    val flavor: Habitation.Flavor,
) : Citizen() {

    val maxChaseRange = 60
    val boundsCenter = XY(bounds.x0 + (bounds.x1 - bounds.x0) / 2, bounds.y0 + (bounds.y1 - bounds.y0) / 2)

    override fun toString() = name() + "(" + id + ")"
    override fun name() = "guard"
    override fun glyph() = Glyph.SHIELD_GUARD
    override fun description() = "A village guard."
    override fun isHuman() = true
    override fun onSpawn() {
        Strength.set(this, 14f)
        Speed.set(this, 12f)
        Brains.set(this, 10f)
        Senses.set(this, 12f)
    }
    override fun armorTotal() = 2.0f
    override fun idleState() = IdlePatrol(
        0.7f,
        bounds,
        stayOutdoors = true
    )
    override fun canSwimShallow() = true

    override fun canSpawnAt(chunk: Chunk, x: Int, y: Int): Boolean = !chunk.isRoofedAt(x, y)

    override fun meetPlayerMsg() = listOf(
        "Hail traveller.",
        "Watch yourself, barbarian.",
        "Welcome to $villageName.",
        "$villageName is a peaceful town."
    ).random()

    override fun couldHaveLore() = false

    private val lantern = LightColor(0.4f, 0.2f, 0.0f)
    private val lightStartTime = DayTime.betweenHoursOf(20, 21)
    private val lightEndTime = DayTime.betweenHoursOf(6, 7)

    override fun light(): LightColor? {
        if (App.gameTime.isAfter(lightStartTime) ||
            App.gameTime.isBefore(lightEndTime)) return lantern
        return null
    }

    override fun hostileResponseState(enemy: Actor): State? {
        if (distanceBetween(enemy.xy, boundsCenter) < maxChaseRange) {
            return Attacking(enemy.id, boundsCenter, maxChaseRange, listOf(
                "And don't come back!",
                "Stay out of $villageName!",
                "Trouble us no more!",
                "And stay out!",
            ).random())
        }
        return null
    }

}

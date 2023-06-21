package actors

import actors.states.Attacking
import actors.states.IdlePatrol
import actors.states.IdleWander
import actors.states.State
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.*
import world.Chunk

@Serializable
class VillageGuard(
    val bounds: Rect,
    val villageName: String,
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

    private val lantern = LightColor(0.5f, 0.4f, 0.0f)
    private val lightStartHour = 20
    private val lightEndHour = 6
    private val lightMinute = Dice.zeroTil(59)

    override fun light(): LightColor? {
        if (App.gameTime.isAfter(lightStartHour, lightMinute) ||
            App.gameTime.isBefore(lightEndHour, lightMinute)) return lantern
        return null
    }

    override fun hostileResponseState(enemy: Actor): State? {
        if (distanceBetween(enemy.xy, boundsCenter) < maxChaseRange) {
            return Attacking(enemy.id, boundsCenter, maxChaseRange)
        }
        return null
    }

}

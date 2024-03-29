package actors.actors

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

@Serializable
class VillageGuard(
    val bounds: Rect,
    val villageName: String,
    val flavor: Habitation.Flavor = Habitation.Flavor.HUMAN,
) : Citizen(), RacedCitizen {

    val maxChaseRange = 60
    val boundsCenter = XY(bounds.x0 + (bounds.x1 - bounds.x0) / 2, bounds.y0 + (bounds.y1 - bounds.y0) / 2)

    var customGlyph: Glyph = Glyph.BLANK
    override fun glyph() = customGlyph

    override fun toString() = name() + "(" + id + ")"
    override val tag = Tag.VILLAGE_GUARD
    override fun name() = "guard"
    override fun description() = "A village guard."
    override fun onSpawn() {
        initStats(14, 12, 10, 12, 14, 5, 2)
    }
    override fun hpMax() = 40f
    override fun skinArmor() = 2.0f
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

    override fun commentLines() = listOf(
        "I can't let idle chitchat distract me.",
        "We don't want any trouble.",
        "I put my life on the line for $villageName.",
        "$villageName may not be much, but it's worth defending.",
        "I wish I could buy a new uniform.",
        "Don't distract me, traveller.",
    )

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

    override fun setSkin(skin: Villager.Skin) {
        customGlyph = skin.guardGlyph
    }
}

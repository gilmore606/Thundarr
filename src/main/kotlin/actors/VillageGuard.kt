package actors

import actors.states.IdlePatrol
import actors.states.IdleWander
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.Dice
import util.LightColor
import util.Rect

@Serializable
class VillageGuard(
    val bounds: Rect,
    val villageName: String,
) : Citizen() {
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
        bounds
    )
    override fun canSwimShallow() = true

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
        val h = App.gameTime.hour
        val m = App.gameTime.minute
        if ((h > lightStartHour && m > lightMinute) || (h < lightEndHour && m < lightMinute)) return lantern
        return null
    }

}

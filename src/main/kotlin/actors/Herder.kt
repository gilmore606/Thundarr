package actors

import actors.actions.Action
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.Dice
import util.LightColor
import world.Entity
import kotlin.random.Random

@Serializable
class Herder : NPC() {

    val lantern = LightColor(0.5f, 0.4f, 0.0f)

    override fun glyph() = Glyph.HERDER
    override fun name() = "herdsman"
    override fun gender() = Entity.Gender.MALE
    override fun description() = "A peasant farm worker.  He looks oppressed, possibly by wizards."
    override fun onSpawn() {
        Strength.set(this, 9f)
        Speed.set(this, 11f)
        Brains.set(this, 11f)
    }
    override fun becomeHostileMsg(): List<String> = listOf("%Dn yelps in dismay at your sudden brutality!")
    override fun converseLines() = listOf(
        "Shoveling ox poo all day for a wizard.  It's a living.",
        "The love of an ox, is not like that of a square.  You know what I mean?",
        "You sure are a big guy!",
        "Shouldn't you be doing your job for the wizard?",
        "They say ox vaginas are the most similar to human vaginas.  But it's not true, not at all."
    )

    override fun pickAction(): Action {
        if (awareness != Awareness.HIBERNATED && Dice.chance(0.5f)) {
            wander()?.also { return it }
        }
        return super.pickAction()
    }

    override fun light() = lantern
    private var flicker = 1f
    override fun flicker() = flicker
    override fun doOnRender(delta: Float) {
        if (awareness != Awareness.HIBERNATED && System.currentTimeMillis() % 5 == 1L) {
            flicker = Random.nextFloat() * 0.12f + 0.88f
        }
    }
}

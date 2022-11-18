package actors

import actors.actions.Action
import actors.actions.Converse
import actors.actions.Attack
import actors.states.Hibernated
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import actors.stats.skills.Dodge
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.Dice
import util.LightColor
import util.XY
import util.isEveryFrame
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
        Speed.set(this, 16f)
        Brains.set(this, 11f)
        Dodge.set(this, 4f)
    }
    override fun becomeHostileMsg() = listOf("%Dn yelps in dismay at your sudden brutality!").random()
    override fun converseLines() = listOf(
        "Shoveling ox poo all day for a wizard.  It's a living.",
        "The love of an ox, is not like that of a square.  You know what I mean?",
        "You sure are a big guy!",
        "Shouldn't you be doing your job for the wizard?",
        "They say ox vaginas are the most similar to human vaginas.  But it's not true, not at all."
    )

    override fun light() = lantern
    private var flicker = 1f
    override fun flicker() = flicker
    override fun doOnRender(delta: Float) {
        if (state !is Hibernated && isEveryFrame(5)) {
            flicker = Random.nextFloat() * 0.12f + 0.88f
        }
    }
}

package actors

import actors.states.IdleInRoom
import actors.states.Looting
import actors.stats.Brains
import actors.stats.Speed
import actors.stats.Strength
import actors.stats.skills.Dodge
import actors.stats.skills.Fight
import audio.Speaker
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Cheese
import things.Teeth
import things.Thing
import util.Dice

@Serializable
class Ratlord(
    val wizardName: String
) : NPC() {
    companion object {
        val weapon = Teeth()
    }
    override fun glyph() = Glyph.RATLORD
    override fun name() = "rat-lord"
    override fun description() = "A man-sized hunched gray rodent wearing bronze armor.  It has an air of arrogant authority."
    override fun onSpawn() {
        hpMax = 25f
        hp = 25f
        Strength.set(this, 11f)
        Speed.set(this, 10f)
        Brains.set(this, 9f)
        Fight.set(this, 3f)
    }
    override fun meleeWeapon() = Ratman.weapon
    override fun isHostile() = true
    override fun idleState() = IdleInRoom()

    override fun converseLines() = listOf(
        "Ttthhhee rebelliouthh one!  Killl him!",
        "You dare to defy $wizardName?  Die!",
        "The ethhcaped thhlave!  He itthh here!",
        "The penalty for ethcape ith death!"
    )
    override fun talkSound(actor: Actor) = Speaker.SFX.RAT
    override fun meetPlayerMsg() = this.dnamec() + " says, \"" + converseLines().random() + "\""
    fun findCheeseLines() = listOf(
        "Arrgg cheethe!  I can't rethithht!",
        "What?!  Cheethe rationth!",
        "Unclaimed cheethe!  I'll take that."
    )


    override fun considerState() {
        if (state !is Looting) {
            val cheeses = entitiesSeen { it is Cheese }
            if (cheeses.isNotEmpty()) {
                val cheese = cheeses.keys.random()
                pushState(Looting(cheese.xy()!!, (cheese as Thing).thingTag()))
                say(findCheeseLines().random())
            }
        }
    }
}

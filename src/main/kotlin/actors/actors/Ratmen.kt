package actors.actors

import actors.states.IdleInRoom
import actors.states.Looting
import audio.Speaker
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Cheese
import things.Clothing
import things.Thing

@Serializable
sealed class GenericRatman(
    val wizardName: String
) : NPC() {
    override fun isSentient() = true
    override fun unarmedWeapons() = setOf(teeth, claws)
    override fun idleState() = IdleInRoom()

    override fun talkSound(actor: Actor) = Speaker.SFX.RAT
    override fun meetPlayerMsg() = this.dnamec() + " says, \"" + commentLines().random() + "\""

    override fun commentLines() = listOf(
        "Ttthhhee rebelliouthh one!  Killl him!",
        "You dare to defy $wizardName?  Die!",
        "The ethhcaped thhlave!  He itthh here!",
        "The penalty for ethcape ith death!"
    )

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
                pushState(Looting(cheese.xy()!!, (cheese as Thing).tag))
                say(findCheeseLines().random())
            }
        }
    }
}

@Serializable
class Ratlord(
    val ratlordWizardName: String
) : GenericRatman(ratlordWizardName) {
    override val tag = Tag.RATLORD
    override fun glyph() = Glyph.RATLORD
    override fun name() = "rat-lord"
    override fun description() = "A man-sized hunched gray rodent wearing bronze armor.  It has an air of arrogant authority."
    override fun hpMax() = 24f
    override fun onSpawn() {
        initStats(11, 10, 9, 11, 8, 3, 1)
    }
    override fun unarmedDamage() = 6f
    override fun skinArmorMaterial() = Clothing.Material.METAL
    override fun skinArmor() = 2f
}

@Serializable
class Ratman(
    val ratmanWizardName: String
) : GenericRatman(ratmanWizardName) {

    override val tag = Tag.RATMAN
    override fun glyph() = Glyph.RATMAN
    override fun name() = "rat-man"
    override fun description() = "A man-sized hunched gray rodent. Its eyes glitter with hatred."
    override fun hpMax() = 16f
    override fun onSpawn() {
        initStats(10, 9, 7, 11, 6, 2, 1)
    }
    override fun unarmedDamage() = 4f
    override fun skinArmorMaterial() = Clothing.Material.HIDE
    override fun skinArmor() = 1f
}

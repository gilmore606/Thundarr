package actors

import actors.states.IdleWander
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.Dice
import util.Madlib
import world.Entity

@Serializable
class Villager() : Citizen() {

    private val customGender = if (Dice.flip()) Entity.Gender.MALE else Entity.Gender.FEMALE
    private val customName = Madlib.villagerName(customGender)
    private val customGlyph = when (Dice.zeroTo(3)) {
        0 -> Glyph.PEASANT_1
        1 -> Glyph.PEASANT_2
        2 -> Glyph.PEASANT_3
        else -> Glyph.PEASANT_4
    }

    override fun gender() = customGender
    override fun name() = customName
    override fun hasProperName() = true
    override fun glyph() = customGlyph
    override fun description() = "A peasant villager in shabby handmade clothes, with weathered skin and a bleak expression."
    override fun isHuman() = true

    override fun idleState() = IdleWander(0.5f)

    override fun meetPlayerMsg() = "Welcome to ${village?.name ?: "our town"}."
    override fun converseLines() = listOf(meetPlayerMsg())

}

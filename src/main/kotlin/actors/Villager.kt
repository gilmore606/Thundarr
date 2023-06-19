package actors

import actors.states.IdleVillager
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.*
import world.Entity
import world.path.Pather

@Serializable
class Villager() : Citizen() {

    @Serializable
    data class WorkArea(
        val name: String,
        val rect: Rect,
        val comments: Set<String>
    ) {
        fun contains(xy: XY) = rect.contains(xy)
    }

    var homeArea: WorkArea = WorkArea("",Rect(0,0,0,0),setOf())
    var targetArea: WorkArea = WorkArea("",Rect(0,0,0,0),setOf())

    fun setTarget(newTarget: WorkArea) {
        targetArea = newTarget
        Pather.unsubscribeAll(this)
        if (!targetArea.contains(xy)) {
            log.info("$this changing target to ${targetArea.name}")
            Pather.subscribe(this, targetArea.rect, 25f)
        }
    }

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

    override fun idleState() = IdleVillager(
        Dice.range(16, 20),
        Dice.range(21, 23),
        Dice.range(4, 6),
        Dice.range(7, 9),
    )

    override fun meetPlayerMsg() = "Welcome to ${village?.name ?: "our town"}."
    override fun converseLines() = targetArea.comments.toList()

}

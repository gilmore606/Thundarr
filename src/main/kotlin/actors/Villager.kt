package actors

import actors.states.IdleVillager
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.*
import world.Entity
import world.path.Pather

@Serializable
class Villager() : Citizen() {

    companion object {
        val defaultArea = WorkArea("", Rect(0,0,0,0),setOf())
    }

    @Serializable
    data class WorkArea(
        val name: String,
        val rect: Rect,
        val comments: Set<String>
    ) {
        fun contains(xy: XY) = rect.contains(xy)
        fun isAdjacentTo(xy: XY) = rect.isAdjacentTo(xy)
    }

    var homeArea = defaultArea
    var targetArea = defaultArea
    var previousTargetArea = defaultArea

    override fun toString() = name()

    fun setTarget(newTarget: WorkArea) {
        if (newTarget != targetArea) {
            previousTargetArea = targetArea
            targetArea = newTarget
            Pather.unsubscribeAll(this)
            if (!targetArea.contains(xy)) {
                log.info("$this changing target to ${targetArea.name}")
                Pather.subscribe(this, targetArea.rect, 36f)
            } else {
                log.info("$this targeting ${targetArea.name} but already there")
            }
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
        Dice.range(17, 20),
        Dice.range(21, 23),
        Dice.range(4, 5),
        Dice.range(6, 8),
    )

    override fun meetPlayerMsg() = "Welcome to ${village?.name ?: "our town"}."

}

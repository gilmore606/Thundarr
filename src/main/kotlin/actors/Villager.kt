package actors

import actors.states.IdleVillager
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.*
import world.Entity
import world.level.Level
import world.path.Pather

@Serializable
class Villager(
    val bedLocation: XY,
) : Citizen() {

    companion object {
        val defaultArea = WorkArea("", Rect(0,0,0,0),setOf())
    }

    @Serializable
    data class WorkArea(
        val name: String,
        val rect: Rect,
        val comments: Set<String>,
        val needsOwner: Boolean = false
    ) {
        override fun toString() = "$name ($rect)"
        fun contains(xy: XY) = rect.contains(xy)
        fun isAdjacentTo(xy: XY) = rect.isAdjacentTo(xy)
        fun villagerCount(level: Level?): Int {
            var count = 0
            level?.also { level ->
                for (ix in rect.x0..rect.x1) {
                    for (iy in rect.y0..rect.y1) {
                        if (level.actorAt(ix, iy) is Villager) count++
                    }
                }
            }
            return count
        }
    }

    var homeArea = defaultArea
    var jobArea: WorkArea? = null

    var targetArea = defaultArea
    var previousTargetArea = defaultArea

    override fun toString() = name()

    fun setTarget(newTarget: WorkArea) {
        if (newTarget == defaultArea) return
        if (newTarget != targetArea) {
            previousTargetArea = if (targetArea == defaultArea) homeArea else targetArea
            targetArea = newTarget
            Pather.unsubscribeAll(this)
        }
        if (!targetArea.contains(xy)) {
            Pather.subscribe(this, targetArea.rect, 48f)
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

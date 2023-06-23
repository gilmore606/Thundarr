package actors

import actors.actions.Say
import actors.states.Fleeing
import actors.states.IdleVillager
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.*
import world.Entity
import world.gen.features.Village
import world.level.Level
import world.path.Pather

@Serializable
class Villager(
    val bedLocation: XY,
    val flavor: Village.Flavor,
    val isChild: Boolean = false,
) : Citizen() {

    companion object {
        val defaultArea = WorkArea("", Rect(0,0,0,0),setOf())
    }

    @Serializable
    data class WorkArea(
        val name: String,
        val rect: Rect,
        val comments: Set<String>,
        val needsOwner: Boolean = false,
        val signXY: XY? = null,
        val signText: String? = null,
        val announceJobMsg: String? = null,
        val childOK: Boolean = true,
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
    var fulltimeJobArea: WorkArea? = null

    var targetArea = defaultArea
    var previousTargetArea = defaultArea
    var nextJobChangeHour: Int = 0
    var nextJobChangeMin: Int = 0

    override fun toString() = name()

    fun setTarget(newTarget: WorkArea) {
        if (newTarget == defaultArea) return
        if (newTarget != targetArea) {
            previousTargetArea = if (targetArea == defaultArea) homeArea else targetArea
            targetArea = newTarget
            Pather.unsubscribeAll(this)
        }
        if (!targetArea.contains(xy)) {
            Pather.subscribe(this, targetArea.rect, 48)
        }
    }

    fun pickJob() {
        if (targetArea == fulltimeJobArea) return
        village?.also { village ->
            val jobArea = fulltimeJobArea ?: village.workAreas.filter { !isChild || it.childOK }.randomOrNull() ?: homeArea
            jobArea.announceJobMsg?.also { msg -> queue(Say(msg)) }
            setTarget(jobArea)
            nextJobChangeHour = App.gameTime.hour + Dice.range(2, 4)
            nextJobChangeMin = Dice.oneTo(58)
        } ?: run { fulltimeJobArea?.also { setTarget(it) } }
    }

    private val customGender = if (Dice.flip()) Entity.Gender.MALE else Entity.Gender.FEMALE
    private val customName = Madlib.villagerName(customGender) + if (isChild) {
        if (customGender == Entity.Gender.MALE) "ie" else "ki"
    } else ""

    private val customGlyph = if (isChild) Glyph.PEASANT_CHILD else when (Dice.zeroTo(3)) {
        0 -> Glyph.PEASANT_1
        1 -> Glyph.PEASANT_2
        2 -> Glyph.PEASANT_3
        else -> Glyph.PEASANT_4
    }

    override fun gender() = customGender
    override fun name() = customName
    override fun hasProperName() = true
    override fun glyph() = customGlyph
    override fun description() = if (isChild) {
        "Like a villager, but small and mischevious.  Its face is smeared with mud."
    } else {
        "A peasant villager in shabby handmade clothes, with weathered skin and a bleak expression."
    }
    override fun isHuman() = true

    override fun idleState() = IdleVillager(
        Dice.range(17, 20),
        Dice.range(21, 23),
        Dice.range(4, 5),
        Dice.range(6, 8),
    )
    override fun hostileResponseState(enemy: Actor) = Fleeing(enemy.id)

    override fun meetPlayerMsg() = if (isChild) {
        "Hello!"
    } else {
        "Welcome to ${village?.name ?: "our town"}."
    }

}

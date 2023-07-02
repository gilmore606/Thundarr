package actors

import actors.actions.Get
import actors.actions.Say
import actors.actions.events.Event
import actors.states.Fleeing
import actors.states.IdleVillager
import actors.stats.Brains
import actors.stats.Senses
import actors.stats.Speed
import actors.stats.Strength
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import render.tilesets.Glyph.*
import things.Container
import things.Door
import ui.modals.ConverseModal
import util.*
import world.Entity
import world.gen.features.Habitation
import world.level.Level
import world.path.Pather
import world.quests.Quest

@Serializable
class Villager(
    val bedLocation: XY,
    val flavor: Habitation.Flavor,
    val isChild: Boolean = false,
) : Citizen() {

    enum class Skin(
        val maleGlyphs: Set<Pair<Glyph, Glyph>>,
        val femaleGlyphs: Set<Pair<Glyph, Glyph>>,
        val childGlyph: Glyph
    ) {
        PALE(
            setOf(
                Pair(PORTRAIT_PALE_M_1, PEASANT_PALE_RED),
                Pair(PORTRAIT_PALE_M_2, PEASANT_PALE_BLOND),
                Pair(PORTRAIT_PALE_M_3, PEASANT_PALE_RED),
                Pair(PORTRAIT_PALE_M_4, PEASANT_PALE_BLOND),
                Pair(PORTRAIT_PALE_M_5, PEASANT_PALE_DARK),
                Pair(PORTRAIT_PALE_M_6, PEASANT_PALE_DARK),
                Pair(PORTRAIT_PALE_M_7, PEASANT_PALE_BLOND),
                Pair(PORTRAIT_PALE_M_8, PEASANT_PALE_DARK),
            ),
            setOf(
                Pair(PORTRAIT_PALE_W_1, PEASANT_PALE_RED),
                Pair(PORTRAIT_PALE_W_2, PEASANT_PALE_BLOND),
                Pair(PORTRAIT_PALE_W_3, PEASANT_PALE_BLOND),
                Pair(PORTRAIT_PALE_W_4, PEASANT_PALE_DARK),
                Pair(PORTRAIT_PALE_W_5, PEASANT_PALE_RED),
                Pair(PORTRAIT_PALE_W_6, PEASANT_PALE_DARK),
                Pair(PORTRAIT_PALE_W_7, PEASANT_PALE_GREEN),
                Pair(PORTRAIT_PALE_W_8, PEASANT_PALE_BLOND),
            ),
            PEASANT_PALE_CHILD
        ),
        WHITE(
            setOf(
                Pair(PORTRAIT_WHITE_M_1, PEASANT_WHITE_DARK),
                Pair(PORTRAIT_WHITE_M_2, PEASANT_WHITE_DARK),
                Pair(PORTRAIT_WHITE_M_3, PEASANT_WHITE_RED),
                Pair(PORTRAIT_WHITE_M_4, PEASANT_WHITE_GREEN),
                Pair(PORTRAIT_WHITE_M_5, PEASANT_WHITE_DARK),
                Pair(PORTRAIT_WHITE_M_6, PEASANT_WHITE_DARK),
                Pair(PORTRAIT_WHITE_M_7, PEASANT_WHITE_DARK),
                Pair(PORTRAIT_WHITE_M_8, PEASANT_WHITE_RED),
            ),
            setOf(
                Pair(PORTRAIT_WHITE_W_1, PEASANT_WHITE_BLOND),
                Pair(PORTRAIT_WHITE_W_2, PEASANT_WHITE_RED),
                Pair(PORTRAIT_WHITE_W_3, PEASANT_WHITE_GREEN),
                Pair(PORTRAIT_WHITE_W_4, PEASANT_WHITE_DARK),
                Pair(PORTRAIT_WHITE_W_5, PEASANT_WHITE_BLOND),
                Pair(PORTRAIT_WHITE_W_6, PEASANT_WHITE_RED),
                Pair(PORTRAIT_WHITE_W_7, PEASANT_WHITE_DARK),
            ),
            PEASANT_WHITE_CHILD
        ),
        TAN(
            setOf(
                Pair(PORTRAIT_TAN_M_1, PEASANT_TAN_BLOND),
                Pair(PORTRAIT_TAN_M_2, PEASANT_TAN_BLOND),
                Pair(PORTRAIT_TAN_M_3, PEASANT_TAN_DARK),
                Pair(PORTRAIT_TAN_M_4, PEASANT_TAN_DARK),
                Pair(PORTRAIT_TAN_M_5, PEASANT_TAN_DARK),
                Pair(PORTRAIT_TAN_M_6, PEASANT_TAN_RED),
                Pair(PORTRAIT_TAN_M_7, PEASANT_TAN_BLOND),
                Pair(PORTRAIT_TAN_M_8, PEASANT_TAN_DARK),
            ),
            setOf(
                Pair(PORTRAIT_TAN_W_1, PEASANT_TAN_GREEN),
                Pair(PORTRAIT_TAN_W_2, PEASANT_TAN_RED),
                Pair(PORTRAIT_TAN_W_3, PEASANT_TAN_DARK),
                Pair(PORTRAIT_TAN_W_4, PEASANT_TAN_DARK),
                Pair(PORTRAIT_TAN_W_5, PEASANT_TAN_DARK),
                Pair(PORTRAIT_TAN_W_6, PEASANT_TAN_RED),
                Pair(PORTRAIT_TAN_W_7, PEASANT_TAN_GREEN),
            ),
            PEASANT_TAN_CHILD
        ),
        BLACK(
            setOf(
                Pair(PORTRAIT_BLACK_M_1, PEASANT_BLACK_BLOND),
                Pair(PORTRAIT_BLACK_M_2, PEASANT_BLACK_DARK),
                Pair(PORTRAIT_BLACK_M_3, PEASANT_BLACK_DARK),
                Pair(PORTRAIT_BLACK_M_4, PEASANT_BLACK_DARK),
                Pair(PORTRAIT_BLACK_M_5, PEASANT_BLACK_RED),
                Pair(PORTRAIT_BLACK_M_6, PEASANT_BLACK_DARK),
                Pair(PORTRAIT_BLACK_M_7, PEASANT_BLACK_GREEN),
            ),
            setOf(
                Pair(PORTRAIT_BLACK_W_1, PEASANT_BLACK_DARK),
                Pair(PORTRAIT_BLACK_W_2, PEASANT_BLACK_RED),
                Pair(PORTRAIT_BLACK_W_3, PEASANT_BLACK_BLOND),
                Pair(PORTRAIT_BLACK_W_4, PEASANT_BLACK_DARK),
                Pair(PORTRAIT_BLACK_W_5, PEASANT_BLACK_DARK),
                Pair(PORTRAIT_BLACK_W_6, PEASANT_BLACK_GREEN),
            ),
            PEASANT_BLACK_CHILD
        )
    }

    companion object {
        val defaultArea = WorkArea("", Rect(0,0,0,0),setOf())
        val skinSets = setOf<Set<Skin>>(
            setOf(Skin.PALE, Skin.WHITE),
            setOf(Skin.PALE, Skin.WHITE),
            setOf(Skin.PALE, Skin.WHITE),
            setOf(Skin.WHITE, Skin.TAN),
            setOf(Skin.WHITE, Skin.TAN),
            setOf(Skin.WHITE, Skin.TAN),
            setOf(Skin.PALE, Skin.WHITE, Skin.TAN),
            setOf(Skin.TAN, Skin.BLACK),
            setOf(Skin.TAN, Skin.BLACK),
            setOf(Skin.TAN, Skin.BLACK),
            setOf(Skin.WHITE, Skin.TAN, Skin.BLACK),
            setOf(Skin.PALE, Skin.BLACK)
        )
        val allSkins = setOf(Skin.PALE, Skin.WHITE, Skin.TAN, Skin.BLACK)
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
    ) : ConverseModal.Source {
        override fun toString() = "$name ($rect)"
        fun contains(xy: XY) = rect.contains(xy)
        fun isAdjacentTo(xy: XY) = rect.isAdjacentTo(xy)
        fun includesDoor(door: Door) = contains(door.xy()) || isAdjacentTo(door.xy())
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
        fun villagers(level: Level?): Set<Villager> {
            val villagers = mutableSetOf<Villager>()
            level?.also { level ->
                for (ix in rect.x0..rect.x1) {
                    for (iy in rect.y0..rect.y1) {
                        level.actorAt(ix, iy)?.also {
                            if (it is Villager) villagers.add(it)
                        }
                    }
                }
            }
            return villagers
        }

        override fun getConversationTopic(topic: String): ConverseModal.Scene? {
//            when (topic) {
//                "hello" -> { ConverseModal.Scene(topic, )}
//            }
            return null
        }
    }

    var homeArea = defaultArea
    var fulltimeJobArea: WorkArea? = null

    var targetArea = defaultArea
    var previousTargetArea = defaultArea
    var nextJobChangeTime: DayTime = DayTime(0,0)

    val family = mutableListOf<String>()
    val ownedThings = mutableListOf<String>()

    override fun toString() = name()

    var portraitGlyph: Glyph = Glyph.BLANK
    var customGlyph: Glyph = Glyph.BLANK
    private val customGender = if (Dice.flip()) Entity.Gender.MALE else Entity.Gender.FEMALE
    private val customName = flavor.namePrefix + Madlib.villagerName(customGender) + if (isChild) {
        if (customGender == Entity.Gender.MALE) "ie" else "ki"
    } else ""

    fun setTarget(newTarget: WorkArea) {
        if (newTarget == defaultArea) return
        if (newTarget != targetArea) {
            Pather.unsubscribe(this, targetArea.rect)
            previousTargetArea = if (targetArea == defaultArea) homeArea else targetArea
            targetArea = newTarget
        }
        if (!targetArea.contains(xy)) {
            Pather.subscribe(this, targetArea.rect, 48)
        }
    }

    fun pickJob() {
        if (targetArea == fulltimeJobArea) return
        habitation?.also { village ->
            val jobArea = fulltimeJobArea ?: village.workAreas.filter { !isChild || it.childOK }.randomOrNull() ?: homeArea
            jobArea.announceJobMsg?.also { msg -> queue(Say(msg)) }
            setTarget(jobArea)
            nextJobChangeTime = DayTime(App.gameTime.hour + Dice.range(2, 4), Dice.oneTo(58))
        } ?: run { fulltimeJobArea?.also { setTarget(it) } }
    }

    fun setSkin(skin: Villager.Skin) {
        if (isChild) {
            customGlyph = skin.childGlyph
        } else {
            (if (customGender == Entity.Gender.MALE) skin.maleGlyphs else skin.femaleGlyphs).random().apply {
                portraitGlyph = first
                customGlyph = second
            }
        }
    }

    override fun onMove() {
        if (ownedThings.isEmpty()) {
            if (homeArea.contains(this.xy)) {
                for (ix in homeArea.rect.x0 .. homeArea.rect.x1) {
                    for (iy in homeArea.rect.y0 .. homeArea.rect.y1) {
                        level?.thingsAt(ix, iy)?.forEach { thing ->
                            ownedThings.add(thing.id)
                            if (thing is Container) {
                                thing.contents().forEach { content ->
                                    ownedThings.add(content.id)
                                }
                            }
                        }
                    }
                }
            }
        }
        super.onMove()
    }

    override fun witnessEvent(culprit: Actor?, event: Event, location: XY) {
        if (event is Get) {
            if (homeArea.contains(location)) {
                if (ownedThings.contains(event.thingKey.id)) {
                    culprit?.also { culprit ->
                        say(listOf(
                            "Stop, thief!", "That's mine, you bastard!", "You can't steal from me!"
                        ).random())
                        downgradeOpinionOf(culprit)
                    }
                }
            }
        }
        super.witnessEvent(culprit, event, location)
    }

    override fun gender() = customGender
    override fun name() = customName
    override fun glyph() = customGlyph
    override fun portraitGlyph() = portraitGlyph
    override fun hasProperName() = true
    override fun isHuman() = true
    override fun onSpawn() {
        Strength.set(this, 10f)
        Speed.set(this, 10f)
        Brains.set(this, 10f)
        Senses.set(this, 10f)
    }
    override fun description() = if (isChild) {
        "Like a villager, but small and mischevious.  Its face is smeared with mud."
    } else {
        "A peasant villager in shabby handmade clothes, with weathered skin and a bleak expression."
    }

    override fun idleState() = IdleVillager(flavor.restTime(), flavor.sleepTime(), flavor.wakeTime(), flavor.workTime())

    override fun hostileResponseState(enemy: Actor) = Fleeing(enemy.id)

    override fun couldGiveQuest(quest: Quest) = !isChild
    override fun couldHaveLore() = !isChild

    override fun conversationSources() = super.conversationSources().apply {
        if (targetArea.contains(xy)) add(targetArea)
    }

    override fun meetPlayerMsg() = if (isChild) {
        "Hello!"
    } else {
        "Welcome to ${habitation?.name() ?: "our town"}."
    }

    override fun commentLines(): List<String> = if (isChild) {
        listOf(
            "You look dumb.", "Your face is a butt.", "You smell like a butt!",
            "Why is your head weird?", "You're funny."
        )
    } else if (targetArea.contains(xy)) {
        targetArea.comments.toMutableList().apply {
            questsGiven().forEach { addAll(it.commentLines()) }
            lore.forEach { addAll(it.commentLines()) }
        }
    } else listOf(
        "No time to chat, I've got places to be."
    )

}

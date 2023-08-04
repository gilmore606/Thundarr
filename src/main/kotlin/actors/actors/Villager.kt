package actors.actors

import actors.actions.Get
import actors.actions.Say
import actors.actions.events.Event
import actors.jobs.Job
import actors.jobs.VendorJob
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
import util.*
import world.Entity
import world.gen.features.Habitation
import path.Pather
import world.level.Level
import world.quests.Quest

interface RacedCitizen {
    fun setSkin(skin: Villager.Skin)
}

@Serializable
class Villager(
    val bedLocation: XY,
    val flavor: Habitation.Flavor,
    val isChild: Boolean = false,
    private val initialHomeJob: Job,
    private val initialFulltimeJob: Job? = null,
) : Citizen(), RacedCitizen {

    enum class Skin(
        val maleGlyphs: Set<Pair<Glyph, Glyph>>,
        val femaleGlyphs: Set<Pair<Glyph, Glyph>>,
        val guardGlyph: Glyph,
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
                Pair(PORTRAIT_PALE_M_9, PEASANT_PALE_GREEN)
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
                Pair(PORTRAIT_PALE_W_9, PEASANT_PALE_GREEN)
            ),
            PEASANT_PALE_GUARD,
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
                Pair(PORTRAIT_WHITE_M_9, PEASANT_WHITE_BLOND)
            ),
            setOf(
                Pair(PORTRAIT_WHITE_W_1, PEASANT_WHITE_BLOND),
                Pair(PORTRAIT_WHITE_W_2, PEASANT_WHITE_RED),
                Pair(PORTRAIT_WHITE_W_3, PEASANT_WHITE_GREEN),
                Pair(PORTRAIT_WHITE_W_4, PEASANT_WHITE_DARK),
                Pair(PORTRAIT_WHITE_W_5, PEASANT_WHITE_BLOND),
                Pair(PORTRAIT_WHITE_W_6, PEASANT_WHITE_RED),
                Pair(PORTRAIT_WHITE_W_7, PEASANT_WHITE_DARK),
                Pair(PORTRAIT_WHITE_W_8, PEASANT_WHITE_GREEN),
                Pair(PORTRAIT_WHITE_W_9, PEASANT_WHITE_BLOND)
            ),
            PEASANT_WHITE_GUARD,
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
                Pair(PORTRAIT_TAN_M_9, PEASANT_TAN_GREEN)
            ),
            setOf(
                Pair(PORTRAIT_TAN_W_1, PEASANT_TAN_GREEN),
                Pair(PORTRAIT_TAN_W_2, PEASANT_TAN_RED),
                Pair(PORTRAIT_TAN_W_3, PEASANT_TAN_DARK),
                Pair(PORTRAIT_TAN_W_4, PEASANT_TAN_DARK),
                Pair(PORTRAIT_TAN_W_5, PEASANT_TAN_DARK),
                Pair(PORTRAIT_TAN_W_6, PEASANT_TAN_RED),
                Pair(PORTRAIT_TAN_W_7, PEASANT_TAN_BLOND),
                Pair(PORTRAIT_TAN_W_8, PEASANT_TAN_GREEN),
                Pair(PORTRAIT_TAN_W_9, PEASANT_TAN_BLOND)
            ),
            PEASANT_TAN_GUARD,
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
                Pair(PORTRAIT_BLACK_M_8, PEASANT_BLACK_BLOND),
                Pair(PORTRAIT_BLACK_M_9, PEASANT_BLACK_BLOND)
            ),
            setOf(
                Pair(PORTRAIT_BLACK_W_1, PEASANT_BLACK_DARK),
                Pair(PORTRAIT_BLACK_W_2, PEASANT_BLACK_RED),
                Pair(PORTRAIT_BLACK_W_3, PEASANT_BLACK_BLOND),
                Pair(PORTRAIT_BLACK_W_4, PEASANT_BLACK_DARK),
                Pair(PORTRAIT_BLACK_W_5, PEASANT_BLACK_DARK),
                Pair(PORTRAIT_BLACK_W_6, PEASANT_BLACK_GREEN),
                Pair(PORTRAIT_BLACK_W_7, PEASANT_BLACK_GREEN),
                Pair(PORTRAIT_BLACK_W_8, PEASANT_BLACK_RED),
                Pair(PORTRAIT_BLACK_W_9, PEASANT_BLACK_BLOND)

            ),
            PEASANT_BLACK_GUARD,
            PEASANT_BLACK_CHILD
        )
    }

    companion object {
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
            setOf(Skin.BLACK),
            setOf(Skin.WHITE, Skin.TAN, Skin.BLACK),
        )
        val allSkins = setOf(Skin.PALE, Skin.WHITE, Skin.TAN, Skin.BLACK)
    }
    var homeJob = initialHomeJob
    var fulltimeJob: Job? = initialFulltimeJob

    var targetJob = homeJob
    var previousTargetJob = homeJob
    var nextJobChangeTime: DayTime = DayTime(0,0)

    val family = mutableListOf<String>()
    val ownedThings = mutableListOf<String>()

    override fun toString() = name()
    override val tag = Tag.VILLAGER

    var portraitGlyph: Glyph = Glyph.BLANK
    var customGlyph: Glyph = Glyph.BLANK
    private val customGender = if (Dice.flip()) Entity.Gender.MALE else Entity.Gender.FEMALE
    private val customName = flavor.namePrefix + Madlib.villagerName(customGender) + if (isChild) {
        if (customGender == Entity.Gender.MALE) "ie" else "ki"
    } else ""

    fun setTarget(newTarget: Job) {
        if (newTarget != targetJob) {
            previousTargetJob = targetJob
            targetJob = newTarget
            log.info("$this changed jobs to $targetJob (was $previousTargetJob)")
        }
    }

    fun pickJob() {
        if (targetJob == fulltimeJob) return
        habitation?.also { village ->
            val newJob = fulltimeJob ?: village.jobs.filter { !isChild || it.childOK }.randomOrNull() ?: homeJob
            newJob.announceJobMsg()?.also { msg -> queue(Say(msg)) }
            setTarget(newJob)
            nextJobChangeTime = DayTime(App.gameTime.hour + Dice.range(2, 4), Dice.oneTo(58))
        } ?: run { fulltimeJob?.also { setTarget(it) } }
    }

    override fun setSkin(skin: Skin) {
        if (isChild) {
            customGlyph = skin.childGlyph
        } else {
            (if (customGender == Entity.Gender.MALE) skin.maleGlyphs else skin.femaleGlyphs).random().apply {
                portraitGlyph = first
                customGlyph = second
            }
        }
    }

    override fun onMove(oldLevel: Level?) {
        // Record ownedThings to detect robbery
        if (ownedThings.isEmpty()) {
            if (homeJob.contains(this)) {
                forXY(homeJob.rect) { ix,iy ->
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
        super.onMove(oldLevel)
    }

    override fun witnessEvent(culprit: Actor?, event: Event, location: XY) {
        if (event is Get) {
            if (homeJob.contains(location)) {
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

    override fun updateConversationGlyph() {
        super.updateConversationGlyph()
        if (targetJob.contains(this) && targetJob is VendorJob && fulltimeJob == targetJob) {
            conversationGlyph = Glyph.TRADE_ICON
        } else if (conversationGlyph == null && targetJob.hasConversationFor(this)) {
            conversationGlyph = Glyph.CONVERSATION_ICON
        }
    }

    override fun conversationSources() = super.conversationSources().apply {
        if (targetJob.hasConversationFor(this@Villager)) add(1, targetJob)
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
    } else if (targetJob.contains(this)) {
        targetJob.comments(this).toMutableList().apply {
            questsGiven().forEach { addAll(it.commentLines()) }
            lore.forEach { addAll(it.commentLines()) }
        }
    } else listOf(
        "No time to chat, I've got places to be."
    )

    override fun examineInfo(): String {
        if (!App.DEBUG_VISIBLE) return super.examineInfo() else {
            val action = if (queuedActions.isNotEmpty()) queuedActions[0] else null
            return "targetJob=${targetJob}\n\ndoing=$action\n\n"
        }
    }
}

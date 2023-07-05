package actors.jobs

import actors.NPC
import actors.Villager
import kotlinx.serialization.Serializable
import things.Door
import ui.modals.ConverseModal
import util.Rect
import util.XY
import world.Entity
import world.level.Level

@Serializable
sealed class Job(
    val name: String,
    val title: String,
    val rect: Rect,
    val needsOwner: Boolean = false,
    val childOK: Boolean = true,
    val extraWorkersOK: Boolean = true,
    val comments: Set<String> = setOf(
        "Workin all day.  Beats dyin!",
        "Seems like work is never done.",
        "I try to stay busy.",
    ),
) : ConverseModal.Source {
    override fun toString() = "Job:$name($rect)"

    var signXY: XY? = null

    open fun title(worker: Villager) = title
    open fun comments(speaker: Villager): Set<String> = comments
    open fun announceJobMsg(): String? = "Time to go to the $title."
    open fun signText(): String? = null
    fun contains(xy: XY) = rect.contains(xy)
    fun contains(entity: Entity) = rect.contains(entity.xy())
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

    open fun hasConversationFor(talker: NPC) = (talker is Villager && talker.fulltimeJob == this && this.contains(talker))

    open fun converseHello(talker: NPC): ConverseModal.Scene? =
        if (talker is Villager && talker.fulltimeJob == this) converseHelloOwner() else null
    open fun converseHelloOwner(): ConverseModal.Scene? = null

    override fun getConversationTopic(talker: NPC, topic: String): ConverseModal.Scene? = when (topic) {
        "hello" -> converseHello(talker)
        else -> null
    }
}

@Serializable
class HomeJob(
    private val homeRect: Rect,
): Job(
    "home", "occupant", homeRect, needsOwner = false, childOK = true,
    comments = setOf(
        "Ah, home and hearth.",
        "It's good to be home.",
        "It's not much, but it's my safe place.",
        "A villager's home is a castle.",
        "Home is where the heart is."
    )
) {
    override fun announceJobMsg() = listOf("I'm going home", "Time to go home.", "Quittin' time!").random()
}

@Serializable
class WorkJob(
    private val workJobName: String,
    private val workerTitle: String,
    private val workRect: Rect,
    private val workNeedsOwner: Boolean = false,
    private val workChildOK: Boolean = false,
    private val workComments: Set<String> = setOf(),
) : Job(
    workJobName, workerTitle, workRect, workNeedsOwner, workChildOK, true, workComments
) {

}

@Serializable
class SchoolJob(
    private val schoolRect: Rect,
) : Job(
    "schoolhouse", "student", schoolRect, needsOwner = true, childOK = true,
) {
    override fun title(worker: Villager) = if (worker.isChild) "student" else "teacher"

    override fun comments(speaker: Villager) = if (speaker.fulltimeJob == this) setOf(
        "There's much to learn from the books of the ancients.",
        "Education is the only way to prosperity.",
    ) else setOf(
        "I like school.  I wanna be smart!",
        "School is BORING. I wanna play.",
        "Why's the moon like that anyway?",
    )

    override fun announceJobMsg() = listOf("Oh, I'm late for class.", "Time for class.", "I'm off to school.").random()

    override fun converseHelloOwner() = ConverseModal.Scene(
        "hello", "I'm a schoolteacher."
    )
}

@Serializable
class ChurchJob(
    private val churchRect: Rect,
) : Job(
    "shrine", "worshipper", churchRect, true, true, true,
) {
    override fun title(worker: Villager) = if (worker.fulltimeJob == this) "priest" else "worshipper"

    override fun comments(speaker: Villager) = setOf(
        "May the Lords of Light watch over our village.",
        "I pray every day.  But do they hear?",
        "Sometimes my faith is tested by this cruel world.",
        "Protect us from the wizards, o Lords of Light.",
    )

    override fun announceJobMsg() = listOf("I need to pray.", "The Lords of Light call to me.", "Prayer time.").random()

    override fun converseHelloOwner() = ConverseModal.Scene(
        "hello", "I minister to this village for the Lords of Light.", listOf(
            ConverseModal.Option("pray", "Will you pray for me now?") { }
        )
    )

    override fun getConversationTopic(talker: NPC, topic: String): ConverseModal.Scene? {
        super.getConversationTopic(talker, topic)?.also { return it }
        return when (topic) {
            "pray" -> ConverseModal.Scene("", "Pray with me now, barbarian.")
            else -> null
        }
    }
}

@Serializable
class FarmJob(
    private val farmName: String,
    private val farmRect: Rect,
) : Job(
    farmName, "farmer", farmRect, false, false, true,
) {
    override fun comments(speaker: Villager) = setOf(
        "Tilling the earth is a blessing and a curse.",
        "May the Lords of Light make this garden grow!",
        "We work so we can eat.  Simple as that.",
        "I pray the wizards and bandits don't steal these crops away.",
    )
    override fun announceJobMsg() = listOf("Need to get those seeds planted.", "Better go help in the garden.", "I'm going to do some work outdoors.",
        "Heading out to the fields, back later!").random()
}

@Serializable
class WellJob(
    private val wellRect: Rect,
) : Job(
    "well", "water carrier", wellRect, false, true, true,
) {
    override fun comments(speaker: Villager) = setOf(
        "Chop wood, carry water.",
        "What would we do if the well ran dry?",
        "I remember the day the wizard poisoned the well."
    )
    override fun announceJobMsg() = listOf("Need to draw water.", "Gotta fetch some water.", "I'm going to the well.").random()
}

@Serializable
class HuntingJob(
    private val huntRect: Rect
) : Job(
    "hunting ground", "hunter", huntRect, false, true, true
) {
    override fun comments(speaker: Villager) = setOf(
        "Be very quiet, you'll scare the game.",
        "The land will provide what I need.",
        "It's hard living out here, but it's free."
    )
    override fun announceJobMsg() = listOf(
        "Time to hunt.",
        "Let's see what the Lords of Light provide us today.",
        "Hunting time."
    ).random()
}

@Serializable
class MeditationJob(
    private val meditationRect: Rect
) : Job(
    "meditation", "monk", meditationRect, false, true, true
) {
    override fun comments(speaker: Villager) = setOf(
        "Hear me, Lords of Light!",
        "Speak to me, Lords of Light!",
        "Bless me Lords of Light, your humble servant.",
        "O Lords, cleanse this body of sin!"
    )
    override fun announceJobMsg() = listOf(
        "Time for meditation.",
        "I must go and pray.",
        "The Lords of Light call to me.",
    ).random()
}

@Serializable
class TavernLoiterJob(
    val tavernName: String,
    val loiterRect: Rect,
) : Job(
    "out front", "loiterer", loiterRect, false, true, true,
) {
    override fun comments(speaker: Villager) = setOf(
        "You got a smoke?",
        "Sometimes I worry I drink too much.",
        "My wife says I should quit drinking.  But why?",
        "They sure treat you right at $tavernName.",
    )
    override fun announceJobMsg() = listOf("I'm goin out for a smoke.", "Gonna step outside a minute.").random()
}

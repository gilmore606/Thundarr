package world.lore

import actors.NPC
import kotlinx.serialization.Serializable
import ui.modals.ConverseModal

@Serializable
sealed class Lore : ConverseModal.Source {

    companion object {
        val static = setOf(MoonLore1(), WizardLore1())
    }

    class Subject(
        val topic: String,
        val trigger: String,
        val question: String,
        val answer: String,
    )

    open fun helloText(): String? = null
    open fun commentLines(): Set<String> = setOf()
    abstract fun subjects(): Set<Subject>

    override fun getConversationTopic(talker: NPC, topic: String): ConverseModal.Scene? {
        if (topic == "hello") {
            helloText()?.also { helloText ->
                return ConverseModal.Scene(topic, helloText)
            }
        }
        subjects().forEach { subject ->
            if (topic == subject.topic) return ConverseModal.Scene(topic, subject.answer)
        }
        return null
    }

    override fun optionsForText(text: String): List<ConverseModal.Option> {
        val options = mutableListOf<ConverseModal.Option>()
        subjects().forEach { subject ->
            if (text.contains(subject.trigger)) {
                options.add(ConverseModal.Option(subject.topic, subject.question))
            }
        }
        return options
    }
}

@Serializable
class MoonLore1: Lore() {
    override fun helloText() = "Do you ever gaze on the moon?  I know its secrets."
    override fun subjects() = setOf(
        Subject("moon", "gaze on the moon", "Tell me about the moon.",
        "The moon is cracked in twain.  But how did it come to be so?  What was inside, do you think?"),
        Subject("moon_inside", "What was inside", "What was inside the moon?",
        "When the comet came and broke the moon, something buried deep within was exposed.  A source of terrible power."),
        Subject("moon_power", "terrible power", "Tell me about this power.",
        "An evil magic force emanates from the moon, and the wizards feed on this power.  They hungered for it, in the ancient times, and built machines to draw it down."),
        Subject("moon_machines", "built machines", "What kind of machines?",
        "Every wizard found his own way to draw the power, and hid the mechanism in an artifact.  For some, an " +
                "amulet or a gem; for others, machines or weapons.  Some have been lost, but still power the malefic link to their creators."),
        Subject("moon_link", "malefic link", "Explain about this link.",
        "The machine draws the moon's power into the wizard; this is how they oppress us.  But destroying it severs the link, and destroys his evil power."),
        Subject("moon_link_destroy", "destroys his evil", "How can they be destroyed?",
        "That I do not know.  Find these machines, and perhaps the Lords of Light will find a way.")
    )
    override fun commentLines() = setOf(
        "Will the Moon ever heal?",
        "I feel the moon's evil."
    )
}

@Serializable
class WizardLore1: Lore() {
    override fun helloText() = "Who will defeat these evil wizards who vex us?"
    override fun subjects() = setOf(
        Subject("wizards", "evil wizards", "You know something about the wizards?",
        "In my travels I've learned much of them.  Monstrous beings they seem, but they were not always as they appear now."),
        Subject("wizards_origin", "as they appear now", "Where did they come from?",
        "When the comet came, they were scientists and men of learning.  The cataclysm drove them mad for power, and unopposed, they built machines "
            + "and armies to conquer the ruined lands.  The power gives them long life.  Yet they are not invincible."),
        Subject("wizards_weakness", "not invincible", "How can they be defeated?",
        "Each of them conceals the source of his power in a talisman.  Find the talisman and destroy it, and the wizard too is destroyed."),
        Subject("wizards_talisman", "the talisman", "How can I find these talismans?",
        "Oh ho, that's not easy!  The wizards guard them jealously, as you'd expect -- yet some have been lost!  Each is different.  For each wizard "
            + "you must seek his fatal weakness.  That is all I can say.")
    )
    override fun commentLines() = setOf(
        "Curse these evil wizards!",
        "Someday the land will be free of wizardry."
    )
}

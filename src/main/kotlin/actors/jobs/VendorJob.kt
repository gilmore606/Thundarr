package actors.jobs

import actors.Villager
import kotlinx.serialization.Serializable
import ui.modals.ConverseModal
import util.Rect


@Serializable
sealed class VendorJob(
    val shopName: String,
    val vendorTitle: String,
    val shopRect: Rect,
    val patronsOK: Boolean = false
) : Job(
    shopName, vendorTitle, shopRect, needsOwner = true, childOK = false, extraWorkersOK = patronsOK,
) {
    override fun signText() = "%n's Place"
    open fun ownerComments() = setOf<String>()
    open fun patronComments() = setOf<String>()

    override fun comments(speaker: Villager) = if (speaker.fulltimeJob == this) ownerComments() else patronComments()
}

@Serializable
class ForgeJob(
    val forgeRect: Rect,
) : VendorJob(
    "forge", "blacksmith", forgeRect
) {
    override fun ownerComments() = setOf(
        "It's hot, sweaty work, but it's honest.",
        "Clang, bang!  I love the sound of smithing.",
        "The fire burns away all impurity."
    )
    override fun announceJobMsg() = listOf("Time to pound the anvil.", "Blacksmithin' time.", "Better get to the forge.").random()
    override fun signText() = listOf(
        "%n's Ironmongery",
        "%n's Smithy",
        "Ironworks by %n",
        "%n Ironworks",
        "%n's Metal Shop",
        "%n's Forge",
    ).random()

    override fun converseHelloOwner() = ConverseModal.Scene(
        "hello", "I'm the blacksmith -- I make tools and weapons."
    )
}

@Serializable
class TavernJob(
    val tavernName: String,
    val tavernRect: Rect
) : VendorJob(
    tavernName, "innkeeper", tavernRect, patronsOK = true
) {
    override fun signText() = tavernName
    override fun ownerComments() = setOf(
        "We aim to please at $name.",
        "If you need anything, just ask.",
        "These people get so rowdy!",
        "Care for a drink?  It'll take the edge off.",
    )
    override fun patronComments() = setOf(
        "I drink to forget.  But I forgot what I was forgetting.",
        "Here's to the Lords of Light!",
        "Always a good time in $name!",
        "*hic* Scuse me.",
        "You don't even know me, man!",
        "This guy?  THIS is the guy.",
    )
    override fun converseHelloOwner() = ConverseModal.Scene(
        "hello", "I'm the keeper of this tavern."
    )
}

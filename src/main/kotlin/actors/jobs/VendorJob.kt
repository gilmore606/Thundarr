package actors.jobs

import actors.NPC
import actors.Villager
import kotlinx.serialization.Serializable
import render.Screen
import things.Container
import things.Thing
import ui.modals.ConverseModal
import ui.modals.ThingsModal
import util.Rect


@Serializable
sealed class VendorJob(
    val shopName: String,
    val vendorTitle: String,
    val shopRect: Rect,
    val caseKey: Thing.Key,
    val patronsOK: Boolean = false
) : Job(
    shopName, vendorTitle, shopRect, needsOwner = true, childOK = false, extraWorkersOK = patronsOK,
) {
    override fun signText() = "%n's Place"
    open fun ownerComments() = setOf<String>()
    open fun patronComments() = setOf<String>()

    override fun comments(speaker: Villager) = if (speaker.fulltimeJob == this) ownerComments() else patronComments()

    open fun ownerConverseHelloMsg() = "I run this shop; if you want to trade, talk to me."
    open fun tradeQuestionMsg() = "Let's trade."

    override fun converseHelloOwner() = ConverseModal.Scene(
        "hello", ownerConverseHelloMsg(), listOf(ConverseModal.Option(
            "trade", tradeQuestionMsg()
        ) { vendor -> beginTrade(vendor) })
    )

    private fun beginTrade(vendor: NPC) {
        caseKey.getThing(vendor.level ?: App.level)?.also { case ->
            Screen.addModal(ThingsModal(
                App.player, case as Container, vendor
            ))
        }
    }
}

@Serializable
class ForgeJob(
    val forgeRect: Rect,
    val forgeCaseKey: Thing.Key,
) : VendorJob(
    "forge", "blacksmith", forgeRect, forgeCaseKey
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

    override fun ownerConverseHelloMsg() = "I'm the blacksmith; I make tools and weapons.  Interested in trading?"
    override fun tradeQuestionMsg() = "Let's see those tools and weapons."
}

@Serializable
class TavernJob(
    val tavernName: String,
    val tavernRect: Rect,
    val tavernCaseKey: Thing.Key,
) : VendorJob(
    tavernName, "innkeeper", tavernRect, tavernCaseKey, patronsOK = true
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

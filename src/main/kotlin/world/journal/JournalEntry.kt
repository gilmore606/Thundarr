package world.journal

import kotlinx.serialization.Serializable
import ui.modals.JournalModal
import util.wrapText

@Serializable
class JournalEntry(
    val title: String,
    val text: String,
    val time: Double = App.time,
) {
    val gameTime = GameTime(time)
    val wrapped: List<String> = wrapText(text, JournalModal.wrapWidth, JournalModal.wrapPadding)
    fun title() = title
    fun text() = text
    fun onAddMsg() = "You note down the date in your journal."
}

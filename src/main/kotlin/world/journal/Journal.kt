package world.journal

import kotlinx.serialization.Serializable
import ui.panels.Console

@Serializable
class Journal {

    val entries = mutableListOf<JournalEntry>()

    fun addEntry(entry: JournalEntry) {
        entries.add(entry)
        Console.say(entry.onAddMsg())
    }

    fun achieve(achievement: JournalEntry) {
        entries.forEach {
            if (it.title == achievement.title) {
                return
            }
        }
        addEntry(achievement)
    }
}

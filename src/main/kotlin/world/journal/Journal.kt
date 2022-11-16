package world.journal

import audio.Speaker
import kotlinx.serialization.Serializable
import render.Screen
import ui.modals.BigSplashModal
import ui.panels.Console

@Serializable
class Journal {

    val entries = mutableListOf<JournalEntry>()

    fun addEntry(entry: JournalEntry) {
        entries.add(entry)
        Console.say(entry.onAddMsg())
        Speaker.ui(Speaker.SFX.UIAWARD)
    }

    fun achieve(achievement: JournalEntry, withModal: Boolean = false) {
        entries.forEach {
            if (it.title == achievement.title) {
                return
            }
        }
        addEntry(achievement)
        if (withModal) {
            Screen.addModal(BigSplashModal(achievement.title, achievement.text, "I'll make a note of this."))
        }
    }
}

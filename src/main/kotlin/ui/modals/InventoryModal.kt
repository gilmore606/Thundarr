package ui.modals

import actors.actions.Drop
import actors.actions.Use
import com.badlogic.gdx.Input
import render.Screen
import things.Container
import things.Thing
import things.ThingHolder
import util.groundAtPlayer
import util.plural
import java.lang.Integer.max

class InventoryModal(
    private val thingHolder: ThingHolder,
    private val container: Container? = null
    ) : SelectionModal(300, 700, "- bACkPACk -", default = 0), ContextMenu.ParentModal {

    private val grouped = ArrayList<ArrayList<Thing>>().apply {
        thingHolder.contents().forEach { thing ->
            val listName = thing.listName()
            var placed = false
            forEach {
                if (it.first().listName() == listName) {
                    it.add(thing)
                    placed = true
                }
            }
            if (!placed) {
                add(ArrayList<Thing>().apply { add(thing) })
            }
        }
    }

    init {
        adjustHeight()
        selectionBoxHeight = 18
        spacing = 28
    }

    fun shownThing(): Thing? = if (selection > -1) grouped[selection].first() else null

    private fun adjustHeight() {
        height = headerPad + max(1, grouped.size) * spacing + padding * 2
        maxSelection = grouped.size - 1
    }

    override fun drawModalText() {
        if (maxSelection < 0) {
            drawOptionText("Your backpack is empty.", 0)
            selection = -1
            return
        }
        var n = 0
        grouped.forEach {
            var text = ""
            text = if (it.size > 1) {
                it.size.toString() + " " + it.first().name().plural()
            } else {
                it.first().listName()
            }
            drawOptionText(text, n, true)
            n++
        }
    }

    override fun drawBackground() {
        super.drawBackground()
        if (!isAnimating()) {
            if (selection > -1) drawOptionShade()
        }
    }

    override fun drawEntities() {
        if (!isAnimating()) {
            var n = 0
            grouped.forEach {
                drawOptionIcon(it.first(), n)
                n++
            }
        }
    }

    override fun doSelect() {
        if (selection < 0) return
        val parent = this
        val ourSelection = selection
        Screen.addModal(ContextMenu(width - 10, optionY(ourSelection) - 4).apply {
            this.parentModal = parent
            val these = grouped[ourSelection]
            val thing = these[0]
            if (these.size > 1) {
                addOption("drop one " + thing.name()) {
                    App.player.queue(Drop(thing, groundAtPlayer()))
                }
                addOption("drop all " + thing.name().plural()) {
                    these.forEach { App.player.queue(Drop(it, groundAtPlayer())) }
                }
            } else {
                addOption("drop " + thing.listName()) {
                    App.player.queue(Drop(thing, groundAtPlayer()))
                }
            }
            thing.uses().forEach {
                if (it.canDo(App.player)) {
                    addOption(it.command) {
                        App.player.queue(Use(thing, it.duration, it.toDo))
                    }
                }
            }
        })
    }

    override fun childSucceeded() { dismiss() }

    override fun keyDown(keycode: Int) {
        when (keycode) {
            Input.Keys.TAB -> dismiss()
            Input.Keys.NUMPAD_4 -> dismiss()
            Input.Keys.NUMPAD_6 -> doSelect()
            else -> super.keyDown(keycode)
        }
    }
}

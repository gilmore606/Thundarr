package ui.modals

import actors.actions.Drop
import com.badlogic.gdx.Input
import render.GameScreen
import things.Thing
import things.ThingHolder
import util.aOrAn
import util.groundAtPlayer
import util.plural
import java.lang.Integer.max

class InventoryModal(
    private val thingHolder: ThingHolder
    ) : SelectionModal(300, 700, "- bACkPACk -", default = 0), ContextMenu.ParentModal {

    private val byKind = ArrayList<Pair<Thing.Kind, ArrayList<Thing>>>().apply {
        thingHolder.byKind().forEach { pair -> add(Pair(pair.key, ArrayList<Thing>().apply {
            pair.value.forEach { add(it) }
        })) }
    }

    init {
        adjustHeight()
        selectionBoxHeight = 18
        spacing = 28
    }

    private fun adjustHeight() {
        height = headerPad + max(1, thingHolder.byKind().size) * 20 + padding * 2
        maxSelection = thingHolder.byKind().size - 1
    }

    override fun drawModalText() {
        if (maxSelection < 0) {
            drawOptionText("Your backpack is empty.", 0)
            return
        }
        var n = 0
        byKind.forEach {
            var text = ""
            text = if (it.second.size > 1) {
                it.second.size.toString() + " " + it.second.first().name().plural()
            } else {
                it.second.first().name().aOrAn()
            }
            drawOptionText(text, n, true)
            n++
        }
    }

    override fun drawBackground() {
        super.drawBackground()
        if (selection > -1) drawOptionShade()
    }

    override fun drawThings() {
        var n = 0
        byKind.forEach {
            drawOptionIcon(it.second.first().glyph(), n)
            n++
        }
    }

    override fun doSelect() {
        val parent = this
        GameScreen.addModal(ContextMenu(width - 10, optionY(selection) - 4).apply {
            this.parentModal = parent
            val these = byKind[selection].second
            if (these.size > 1) {
                addOption("drop one " + these[0].name()) {
                    App.player.queue(Drop(these[0], groundAtPlayer()))
                }
                addOption("drop all " + these[0].name().plural()) {
                    these.forEach { App.player.queue(Drop(it, groundAtPlayer())) }
                }
            } else {
                addOption("drop " + these[0].name()) {
                    App.player.queue(Drop(these[0], groundAtPlayer()))
                }
            }
        })
    }

    override fun childSucceeded() { dismiss() }

    override fun keyDown(keycode: Int) {
        when (keycode) {
            Input.Keys.TAB -> dismiss()
            Input.Keys.NUMPAD_6 -> doSelect()
            else -> super.keyDown(keycode)
        }
    }
}

package ui.modals

import actors.actions.Drop
import actors.actions.UseThing
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
        height = headerPad + max(1, thingHolder.byKind().size) * spacing + padding * 2
        maxSelection = thingHolder.byKind().size - 1
    }

    override fun drawModalText() {
        if (maxSelection < 0) {
            drawOptionText("Your backpack is empty.", 0)
            selection = -1
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
        if (!isAnimating()) {
            if (selection > -1) drawOptionShade()
        }
    }

    override fun drawThings() {
        if (!isAnimating()) {
            var n = 0
            byKind.forEach {
                drawOptionIcon(it.second.first().glyph(), n)
                n++
            }
        }
    }

    override fun doSelect() {
        val parent = this
        GameScreen.addModal(ContextMenu(width - 10, optionY(selection) - 4).apply {
            this.parentModal = parent
            val these = byKind[selection].second
            val thing = these[0]
            if (these.size > 1) {
                addOption("drop one " + thing.name()) {
                    App.player.queue(Drop(thing, groundAtPlayer()))
                }
                addOption("drop all " + thing.name().plural()) {
                    these.forEach { App.player.queue(Drop(it, groundAtPlayer())) }
                }
            } else {
                addOption("drop " + thing.name()) {
                    App.player.queue(Drop(thing, groundAtPlayer()))
                }
            }
            thing.uses().forEach {
                if (it.canDo(App.player)) {
                    addOption(it.command) {
                        App.player.queue(UseThing(thing, it.duration, it.toDo))
                    }
                }
            }
        })
    }

    override fun childSucceeded() { dismiss() }

    override fun keyDown(keycode: Int) {
        when (keycode) {
            Input.Keys.TAB -> dismiss()
            else -> super.keyDown(keycode)
        }
    }
}

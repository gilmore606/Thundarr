package ui.modals

import actors.actions.Equip
import actors.actions.Unequip
import com.badlogic.gdx.Input
import render.Screen
import things.Gear
import things.ThingHolder
import util.log
import java.lang.Integer.max
import java.lang.Integer.min

class GearModal(
    private val thingHolder: ThingHolder
) : SelectionModal(300, 350, "- gEAr -", default = 0), ContextMenu.ParentModal {

    class SlotMenu(
        val name: String,
        val slot: Gear.Slot,
        val current: Gear?,
        val things: List<Gear>
    ) { }

    var slots = ArrayList<SlotMenu>()
    var childDeployed = false

    init {
        zoomWhenOpen = 1.6f
        selectionBoxHeight = 18
        spacing = 32
        headerPad = 80
        fillSlots()

        sidecar = CompareSidecar(this)
        (sidecar as CompareSidecar).showGear1(slots[selection]?.current)
    }

    override fun drawModalText() {
        slots.forEachIndexed { n, slot ->
            drawOptionText((slot.current?.name() ?: ""), n, 90,
                if (childDeployed) Screen.fontColorDull else null)
            val label = slot.name + ":"
            drawString(label, padding + 90 - measure(label), headerPad + spacing * n - 2,
                Screen.fontColorDull, Screen.smallFont)
        }
    }

    override fun drawBackground() {
        if (!isAnimating()) {
            super.drawBackground()
            drawOptionShade(90)
        }
    }

    override fun doSelect() {
        if (selection < 0) return
        super.doSelect()
        val ourSelection = selection
        var startPos = slots[ourSelection].things.indexOf(slots[ourSelection].current)
        if (startPos < 0) startPos = slots[ourSelection].things.size
        childDeployed = true
        Screen.addModal(ContextMenu(
            padding + xMargin + 120, optionY(selection) - 16 - startPos * 26) { hoverOption ->
                sidecar?.also {
                    val things = slots[ourSelection].things
                    (it as CompareSidecar).showGear1(slots[ourSelection].current)
                    if (hoverOption >= 0 && hoverOption <= things.lastIndex && things[hoverOption] != slots[ourSelection].current) {
                        (it as CompareSidecar).showGear2(things[hoverOption])
                    } else {
                        (it as CompareSidecar).showGear2(null)
                    }
                }
            }
        .apply {
            zoomWhenOpen = this@GearModal.zoomWhenOpen
            parentModal = this@GearModal
            val things = slots[ourSelection].things
            if (things.isNotEmpty()) {
                slots[ourSelection].things.forEach { thing ->
                    addOption(thing.listName()) {
                        App.player.queue(Equip(thing))
                        clearCompare()
                    }
                }
                addOption("(none)") {
                    slots[ourSelection].current?.also { App.player.queue(Unequip(it)) }
                    clearCompare()
                }
            } else {
                addOption("(none)") { }
            }
            changeSelection(startPos)
        })
    }

    private fun clearCompare() {
        (sidecar as CompareSidecar).showGear1(null)
    }

    override fun changeSelection(newSelection: Int) {
        super.changeSelection(newSelection)
        if (newSelection > -1) {
            (sidecar as CompareSidecar).showGear1(slots[newSelection].current)
        }
    }

    override fun childSucceeded() {
        childDeployed = false
        changeSelection(selection)
    }

    override fun childCancelled() {
        childDeployed = false
        changeSelection(selection)
    }

    override fun advanceTime(turns: Float) {
        super.advanceTime(turns)
        fillSlots()
        changeSelection(selection)
    }

    private fun fillSlots() {
        slots.clear()
        for (gearslot in Gear.slots) {
            slots.add(SlotMenu(
                gearslot.title, gearslot,
                thingHolder.contents().firstOrNull { it is Gear && it.slot == gearslot && it.equipped }?.let { it as Gear},
                thingHolder.contents().filter { (it is Gear && it.slot == gearslot) }.map { it as Gear }
            ))
        }
        maxSelection = slots.size - 1
    }

    override fun onKeyDown(keycode: Int) {
        when (keycode) {
            Input.Keys.BACKSLASH -> dismiss()
            Input.Keys.TAB -> dismiss()
            Input.Keys.NUMPAD_4 -> dismiss()
            Input.Keys.NUMPAD_6 -> doSelect()
            else -> super.onKeyDown(keycode)
        }
    }
}

package ui.modals

import actors.actions.Equip
import actors.actions.Unequip
import render.Screen
import things.Gear
import things.ThingHolder
import util.log

class GearModal(
    private val thingHolder: ThingHolder
) : SelectionModal(300, 350, "- gEAr -", default = 0) {

    class SlotMenu(
        val name: String,
        val slot: Gear.Slot,
        val current: Gear?,
        val things: List<Gear>
    ) { }

    var slots = ArrayList<SlotMenu>()

    init {
        selectionBoxHeight = 18
        spacing = 40
        headerPad = 80
        fillSlots()

        sidecar = CompareSidecar(this)
    }

    override fun drawModalText() {
        slots.forEachIndexed { n, slot ->
            drawOptionText((slot.current?.name() ?: "(none)"), n, 90)
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
        val ourSelection = selection
        Screen.addModal(ContextMenu(
            padding + xMargin + 120, optionY(selection) - 16) { hoverOption ->
                sidecar?.also {
                    val things = slots[this@GearModal.selection].things
                    if (hoverOption >= 0 && hoverOption <= things.lastIndex && things[hoverOption] != slots[this@GearModal.selection].current) {
                        (it as CompareSidecar).showGear2(things[hoverOption])
                    } else {
                        (it as CompareSidecar).showGear2(null)
                    }
                }
            }
        .apply {
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
        })
    }

    private fun clearCompare() {
        (sidecar as CompareSidecar).showGear1(null)
    }

    override fun mouseToOption(screenX: Int, screenY: Int): Int? {
        val hoverOption = super.mouseToOption(screenX, screenY)
        hoverOption?.also { (sidecar as CompareSidecar).showGear1(slots[it].current) }
        return hoverOption
    }

    override fun advanceTime(turns: Float) {
        super.advanceTime(turns)
        fillSlots()
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
}

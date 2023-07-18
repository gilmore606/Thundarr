package ui.modals

import actors.actions.Equip
import actors.actions.Unequip
import render.Screen
import things.Gear
import things.ThingHolder
import ui.input.Keydef

class GearModal(
    private val thingHolder: ThingHolder
) : SelectionModal(320, 580, "- gEAr -", default = 0), ContextMenu.ParentModal {

    companion object {
        const val statSpacing = 24
        const val statX = 140
    }

    class SlotMenu(
        val name: String,
        val slot: Gear.Slot,
        val current: Gear?,
        val things: List<Gear>
    ) { }

    var slots = ArrayList<SlotMenu>()
    var childDeployed = false

    var toHit = 0f
    var insulation = 0f
    var cooling = 0f
    var weather = 0f

    init {
        zoomWhenOpen = 1.8f
        selectionBoxHeight = 18
        spacing = 32
        headerPad = 90
        regenerate()

        sidecar = CompareSidecar(this)
        (sidecar as CompareSidecar).showList(slots[selection].slot)
    }

    override fun drawModalText() {
        slots.forEachIndexed { n, slot ->
            drawOptionText((slot.current?.name() ?: ""), n, 90,
                if (childDeployed) Screen.fontColorDull else null)
            val label = slot.name + ":"
            drawString(label, padding + 90 - measure(label), headerPad + spacing * n - 2,
                Screen.fontColorDull, Screen.smallFont)
        }

        val statY = headerPad + spacing * slots.size + 30
        drawRightText("toHit:", statX, statY, Screen.fontColorDull, Screen.smallFont)
        drawRightText("insulation:", statX, statY + statSpacing + 12, Screen.fontColorDull, Screen.smallFont)
        drawRightText("cooling:", statX, statY + statSpacing * 2 + 12, Screen.fontColorDull, Screen.smallFont)
        drawRightText("weather:", statX, statY + statSpacing * 3 + 12, Screen.fontColorDull, Screen.smallFont)

        drawString(formatValue(toHit, showPlus = true), statX + 10, statY, Screen.fontColorBold, Screen.font)
        drawString(formatValue(insulation), statX + 10, statY + statSpacing + 12, Screen.fontColorBold, Screen.font)
        drawString(formatValue(cooling), statX + 10, statY + statSpacing * 2 + 12, Screen.fontColorBold, Screen.font)
        drawString(formatValue(weather), statX + 10, statY + statSpacing * 3 + 12, Screen.fontColorBold, Screen.font)
    }

    private fun formatValue(value: Float, showPlus: Boolean = false): String {
        if (value == 0f) return "-"
        return (if (showPlus && value > 0f) "+" else "") + String.format("%.1f", value)
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
                        App.player.queue(Equip(thing.getKey()))
                        clearCompare()
                    }
                }
                addOption("(none)") {
                    slots[ourSelection].current?.also { App.player.queue(Unequip(it.getKey())) }
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
        (sidecar as CompareSidecar).showList(
            if (newSelection > -1) slots[newSelection].slot else null
        )
    }

    override fun childSucceeded() {
        childDeployed = false
        regenerate()
        changeSelection(selection)
    }

    override fun childCancelled() {
        childDeployed = false
        regenerate()
        changeSelection(selection)
    }

    override fun advanceTime(turns: Float) {
        super.advanceTime(turns)
        regenerate()
        changeSelection(selection)
    }

    private fun regenerate() {
        slots.clear()
        for (gearslot in Gear.slots) {
            slots.add(SlotMenu(
                gearslot.title, gearslot,
                thingHolder.contents().firstOrNull { it is Gear && it.slot == gearslot && it.equipped }?.let { it as Gear},
                thingHolder.contents().filter { (it is Gear && it.slot == gearslot) }.map { it as Gear }
            ))
        }
        maxSelection = slots.size - 1

        toHit = App.player.meleeWeapon().accuracy() - App.player.armorEncumbrance()
        insulation = App.player.coldProtection()
        cooling = App.player.heatProtection()
        weather = App.player.weatherProtection()
    }

    override fun onKeyDown(key: Keydef) {
        when (key) {
            Keydef.OPEN_GEAR, Keydef.CANCEL -> dismiss()
            Keydef.OPEN_INV -> replaceWith(ThingsModal(App.player))
            Keydef.OPEN_SKILLS -> replaceWith(SkillsModal(App.player))
            Keydef.OPEN_JOURNAL -> replaceWith(JournalModal())
            Keydef.OPEN_MAP -> replaceWith(MapModal())
            Keydef.MOVE_W -> dismiss()
            Keydef.MOVE_E -> doSelect()
            else -> super.onKeyDown(key)
        }
    }
}

package ui.modals

import actors.actions.Drop
import actors.actions.Get
import actors.actions.Use
import com.badlogic.gdx.Input
import render.Screen
import things.Container
import things.Thing
import things.ThingHolder
import util.groundAtPlayer
import util.log
import util.plural
import java.lang.Integer.max
import java.lang.Integer.min

class InventoryModal(
    private val thingHolder: ThingHolder,
    private val withContainer: Container? = null,
    private val parentModal: Modal? = null,
    sidecarTitle: String? = null
    ) : SelectionModal(400, 700, default = 0,
        title = sidecarTitle?.let { "- $sidecarTitle -" } ?: "- bACkPACk -",
        position = if (parentModal == null) Position.LEFT else Position.SIDECAR
    ), ContextMenu.ParentModal {

    var grouped = ArrayList<ArrayList<Thing>>()
    var weights = ArrayList<String>()
    var totalText: String = ""
    var maxText: String = ""

    init {
        parentModal?.also { this.isSidecar = true ; changeSelection(0) }
        updateGrouped()
        adjustHeight()
        selectionBoxHeight = 18
        spacing = 28
        padding = 18
        headerPad += 10
        withContainer?.also {
            sidecar = InventoryModal(withContainer, parentModal = this, sidecarTitle = withContainer.name())
            moveToSidecar()
        } ?: run {
            changeSelection(0)
        }
    }

    fun shownThing(): Thing? = if (isInSidecar && sidecar is InventoryModal) (sidecar as InventoryModal).shownThing()
        else if (selection > -1) grouped[selection].first() else null

    private fun adjustHeight() {
        height = headerPad + max(1, grouped.size + 1) * spacing + 60 + padding * 2
    }

    override fun myXmargin() = parentModal?.let { (it.width + xMargin + 20) } ?: xMargin

    override fun drawModalText() {
        if (maxSelection < 0) {
            drawOptionText(if (isSidecar) "It's empty." else "Your backpack is empty.", 0)
            changeSelection(-1)
            return
        }
        var n = 0
        grouped.forEachIndexed { i, group ->
            var text = ""
            val first = group.first()
            text = if (group.size > 1) {
                group.size.toString() + " " + first.name().plural()
            } else {
                first.name()
            }
            drawOptionText(text, n, 30, addTag = first.listTag(), addCol = weights[i], colX = 270)
            n++
        }
        if (!isSidecar) {
            drawString("Capacity ", padding, height - 30, Screen.fontColorDull, Screen.smallFont)
            drawString(maxText, padding + 75, height - 31, Screen.fontColor, Screen.font)
        }
        drawString("Total ", padding + 252, height - 30, Screen.fontColorDull, Screen.smallFont)
        drawString(totalText, padding + 300, height - 31, Screen.fontColor, Screen.font)
    }

    override fun drawBackground() {
        super.drawBackground()
        if (!isAnimating()) {
            if (selection > -1) drawOptionShade()
        }
    }

    override fun drawEntities() {
        super.drawEntities()
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
        super.doSelect()
        val parent = this
        val ourSelection = selection
        Screen.addModal(ContextMenu(
            width + (parentModal?.width ?: 0) - 2,
            optionY(ourSelection) - 4
        ).apply {
            this.parentModal = parent
            addInventoryOptions(this, grouped[ourSelection][0], grouped[ourSelection],
                parent.withContainer, parent.parentModal)
        })
    }

    override fun childSucceeded() {
        if (parentModal == null && withContainer == null) dismiss()
    }

    override fun onMouseMovedTo(screenX: Int, screenY: Int) {
        super.onMouseMovedTo(screenX, screenY)
        if (selection > 0) {
            parentModal?.moveToSidecar()
        }
    }

    override fun onKeyDown(keycode: Int) {
        when (keycode) {
            Input.Keys.TAB -> dismiss()
            Input.Keys.NUMPAD_4 -> {
                parentModal?.also {
                    returnToParent()
                } ?: run { dismiss() }
            }
            Input.Keys.NUMPAD_6 -> {
                if (sidecar is InventoryModal && (sidecar as InventoryModal).grouped.isNotEmpty()) {
                    moveToSidecar()
                } else doSelect()
            }
            else -> super.onKeyDown(keycode)
        }
    }

    private fun returnToParent() {
        parentModal?.also {
            if (it is InventoryModal && it.grouped.isNotEmpty()) {
                it.returnFromSidecar()
                (it as SelectionModal).changeSelection(max(0, min(selection, it.maxSelection)))
                changeSelection(-1)
            }
        }
    }

    override fun advanceTime(turns: Float) {
        super.advanceTime(turns)
        updateGrouped()
    }

    private fun updateGrouped() {
        var weightTotal = 0f
        grouped = ArrayList<ArrayList<Thing>>().apply {
            thingHolder.contents().forEach { thing ->
                weightTotal += thing.weight()
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
        weights.clear()
        grouped.forEach { group ->
            weights.add(String.format("%.1f", (group[0].weight() * group.size)) + "lb")
        }
        totalText = String.format("%.1f", weightTotal) + "lb"
        maxText = String.format("%.1f", App.player.carryingCapacity()) + "lb"

        maxSelection = grouped.size - 1
        changeSelection(min(maxSelection, selection))
        adjustHeight()
        if (maxSelection < 0) {
            changeSelection(-1)
            sidecar?.also { moveToSidecar() } ?: run {
                parentModal?.also { returnToParent() }
            }
        }
    }
}

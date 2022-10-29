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
    ) : SelectionModal(300, 700, default = 0,
        title = sidecarTitle?.let { "- $sidecarTitle -" } ?: "- bACkPACk -",
        position = if (parentModal == null) Position.LEFT else Position.SIDECAR
    ), ContextMenu.ParentModal {

    var grouped = ArrayList<ArrayList<Thing>>()

    init {
        parentModal?.also { this.isSidecar = true ; selection = 0 }
        updateGrouped()
        adjustHeight()
        selectionBoxHeight = 18
        spacing = 28
        withContainer?.also {
            sidecar = InventoryModal(withContainer, parentModal = this, sidecarTitle = withContainer.name())
            moveToSidecar()
        } ?: run {
            selection = 0
        }
    }

    fun shownThing(): Thing? = if (isInSidecar && sidecar is InventoryModal) (sidecar as InventoryModal).shownThing()
        else if (selection > -1) grouped[selection].first() else null

    private fun adjustHeight() {
        height = headerPad + max(1, grouped.size) * spacing + padding * 2
    }

    override fun myXmargin() = parentModal?.let { (it.width + xMargin + 20) } ?: xMargin

    override fun drawModalText() {
        if (maxSelection < 0) {
            drawOptionText(if (isSidecar) "It's empty." else "Your backpack is empty.", 0)
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
        val parent = this
        val ourSelection = selection
        Screen.addModal(ContextMenu(
            width + (sidecar?.width ?: 0) + (parentModal?.width ?: 0) - 2,
            optionY(ourSelection) - 4
        ).apply {
            this.parentModal = parent
            val these = grouped[ourSelection]
            val thing = these[0]
            if (these.size > 1) {
                parent.withContainer?.also { container ->
                    addOption("put one " + thing.name() + " in " + container.name()) {
                        App.player.queue(Drop(thing, container))
                    }
                    addOption("put all " + thing.name().plural() + " in " + container.name()) {
                        these.forEach { App.player.queue(Drop(it, container)) }
                    }
                } ?: run {
                    parent.parentModal?.also { parent ->
                        addOption("take one " + thing.name()) {
                            App.player.queue(Get(thing))
                        }
                        addOption("take all " + thing.name().plural()) {
                            these.forEach { App.player.queue(Get(it)) }
                        }
                    } ?: run {
                        addOption("drop one " + thing.name()) {
                            App.player.queue(Drop(thing, groundAtPlayer()))
                        }
                        addOption("drop all " + thing.name().plural()) {
                            these.forEach { App.player.queue(Drop(it, groundAtPlayer())) }
                        }
                    }
                }
            } else {
                parent.withContainer?.also { container ->
                    addOption("put " + thing.name() + " in " + container.name()) {
                        App.player.queue(Drop(thing, container))
                    }
                } ?: run {
                    parent.parentModal?.also {
                        addOption("take " + thing.name()) {
                            App.player.queue(Get(thing))
                        }
                    } ?: run {
                        addOption("drop " + thing.listName()) {
                            App.player.queue(Drop(thing, groundAtPlayer()))
                        }
                    }
                }
            }

            addOption("examine " + thing.name()) {
                Screen.addModal(ExamineModal(thing, Position.CENTER_LOW))
            }

            if (parent.withContainer == null && parent.parentModal == null) {
                thing.uses().forEach {
                    if (it.canDo(App.player)) {
                        addOption(it.command) {
                            App.player.queue(Use(thing, it.duration, it.toDo))
                        }
                    }
                }
            }
        })
    }

    override fun childSucceeded() {
        if (parentModal == null && withContainer == null) dismiss()
    }

    override fun onMouseMovedTo(screenX: Int, screenY: Int) {
        super.onMouseMovedTo(screenX, screenY)
        if (selection > 0) {
            parentModal?.isInSidecar = true
        }
    }

    override fun onKeyDown(keycode: Int) {
        when (keycode) {
            Input.Keys.TAB -> dismiss()
            Input.Keys.NUMPAD_4 -> {
                parentModal?.also {
                    returnToParent()
                    selection = -1
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
                (it as SelectionModal).selection = max(0, min(selection, it.maxSelection))
                selection = -1
            }
        }
    }

    override fun advanceTime(turns: Float) {
        super.advanceTime(turns)
        updateGrouped()
    }

    private fun updateGrouped() {
        grouped = ArrayList<ArrayList<Thing>>().apply {
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
        maxSelection = grouped.size - 1
        selection = min(maxSelection, selection)
        adjustHeight()
        if (maxSelection < 0) {
            selection = -1
            sidecar?.also { moveToSidecar() } ?: run {
                parentModal?.also { returnToParent() }
            }
        }
    }
}

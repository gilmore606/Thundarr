package ui.modals

import actors.actions.Drop
import actors.actions.Get
import actors.actions.Make
import actors.actions.Use
import com.badlogic.gdx.Input
import render.Screen
import things.Container
import things.Thing
import things.ThingHolder
import things.Workbench
import ui.input.Keydef
import util.difficultyDesc
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
    var numRecipes: Int = 0
    var firstRecipeSelection: Int = -1
    var withBench = withContainer is Workbench
    var isBench = (parentModal is InventoryModal && parentModal.withBench)
    fun getBench() = if (isBench) ((parentModal as InventoryModal).withContainer as Workbench) else null

    init {
        zoomWhenOpen = 1.2f
        parentModal?.also { this.isSidecar = true ; changeSelection(0) }
        updateGrouped()
        adjustHeight()
        selectionBoxHeight = 18
        spacing = 27
        padding = 18
        headerPad = 80
        withContainer?.also {
            sidecar = InventoryModal(withContainer, parentModal = this, sidecarTitle = withContainer.name())
            moveToSidecar()
        } ?: run {
            changeSelection(0)
        }
    }

    fun shownThing(): Thing? = if (isInSidecar && sidecar is InventoryModal) (sidecar as InventoryModal).shownThing()
        else if (grouped.isEmpty()) null else if (selection > -1 && selection < firstRecipeSelection) grouped[selection].first() else null

    private fun adjustHeight() {
        height = headerPad + max(1, grouped.size + 1) * (spacing + 2) + padding * 2
        if (isBench) height += getBench()!!.possibleRecipes().size * spacing
        onResize(Screen.width, Screen.height)
    }

    override fun myXmargin() = parentModal?.let { (it.width + xMargin + 20) } ?: xMargin

    override fun drawModalText() {
        if (maxSelection < 0) {
            parentModal?.also {
                drawOptionText((parentModal as InventoryModal).withContainer?.isEmptyMsg() ?: "It's empty.", 0)
            }
            changeSelection(-1)
            return
        }
        grouped.forEachIndexed { i, group ->
            var text = ""
            val first = group.first()
            text = if (group.size > 1) {
                group.size.toString() + " " + first.name().plural()
            } else {
                first.name()
            }
            if (isBench) {
                drawOptionText(text, i, 30)
            } else {
                drawOptionText(text, i, 30, addTag = first.listTag(), addCol = weights[i], colX = 270)
            }
        }
        if (!isSidecar) {
            drawString("Capacity ", padding, height - 30, Screen.fontColorDull, Screen.smallFont)
            drawString(maxText, padding + 75, height - 31, Screen.fontColor, Screen.font)
        }
        if (!isBench) {
            drawString("Total ", padding + 252, height - 30, Screen.fontColorDull, Screen.smallFont)
            drawString(totalText, padding + 300, height - 31, Screen.fontColor, Screen.font)
        } else {
            val bench = (parentModal as InventoryModal).withContainer as Workbench
            val recipes = bench.possibleRecipes()
            if (recipes.isEmpty()) {
                drawString("You can't see anything to make out of this.", padding, height - 30, Screen.fontColor, Screen.smallFont)
            } else {
                var yc = height - padding - numRecipes * spacing
                recipes.forEachIndexed { n, recipe ->
                    val selected = (n == selection - firstRecipeSelection)
                    val cmd = bench.useVerb().capitalize() + " " + recipe.product().iname()
                    val difstr = " (" + recipe.difficulty().difficultyDesc(recipe.skill().get(App.player)) + ")"
                    drawString(cmd, padding, yc + 8, if (selected) Screen.fontColorBold else Screen.fontColorDull, Screen.font)
                    drawString(difstr, padding + 270, yc + 9, if (selected) Screen.fontColor else Screen.fontColorDull, Screen.smallFont)
                    yc += spacing
                }
            }
        }
    }

    override fun drawBackground() {
        super.drawBackground()
        if (!isAnimating()) {
            if (selection > -1 && selection < firstRecipeSelection) {
                drawOptionShade()
            } else if (selection >= firstRecipeSelection) {
                drawOptionShade(forceY = height - (spacing * numRecipes) + (selection - firstRecipeSelection) * spacing - 6)
            }
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
        if (selection >= firstRecipeSelection) {
            App.player.queue(Make(getBench()!!, getBench()!!.possibleRecipes()[selection - firstRecipeSelection]))
        } else {
            Screen.addModal(ContextMenu(
                width + (parentModal?.width ?: 0) - 2,
                optionY(ourSelection) - 4
            ).apply {
                zoomWhenOpen = this@InventoryModal.zoomWhenOpen
                this.parentModal = parent
                addInventoryOptions(
                    this, grouped[ourSelection][0], grouped[ourSelection],
                    parent.withContainer, parent.parentModal
                )
            })
        }
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

    override fun mouseToOption(screenX: Int, screenY: Int): Int? {
        val localX = screenX - x
        val localY = screenY - y + (spacing / 2)
        if (localX in 1 until width) {
            val hoverOption = (localY - headerPad - 5) / spacing
            if (hoverOption in 0 until firstRecipeSelection) {
                return hoverOption
            }
            height - padding - numRecipes * spacing
            val recipeOption = (localY - (height - numRecipes * spacing)) / spacing
            if (recipeOption in 0 until numRecipes) {
                return recipeOption + firstRecipeSelection
            }
        }
        return null
    }

    override fun onKeyDown(key: Keydef) {
        when (key) {
            Keydef.OPEN_INV -> dismiss()
            Keydef.MOVE_W -> {
                parentModal?.also {
                    returnToParent()
                } ?: run { dismiss() }
            }
            Keydef.MOVE_E -> {
                if (sidecar is InventoryModal && (sidecar as InventoryModal).grouped.isNotEmpty()) {
                    moveToSidecar()
                } else doSelect()
            }
            else -> super.onKeyDown(key)
        }
    }

    private fun returnToParent() {
        parentModal?.also {
            if (it is InventoryModal && it.grouped.isNotEmpty()) {
                it.returnFromSidecar()
                (it as SelectionModal).changeSelection(max(0, min(selection, it.maxSelection)))
                changeSelection(-1)
            }
        } ?: run { dismiss() }
    }

    override fun onDismiss() {
        if (withBench) {
            (withContainer as Workbench).emptyOnClose()
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

        numRecipes = (getBench()?.possibleRecipes()?.size ?: 0)
        firstRecipeSelection = grouped.size
        maxSelection = grouped.size - 1 + numRecipes
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

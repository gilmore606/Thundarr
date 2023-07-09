package ui.modals

import actors.actions.Drop
import actors.actions.Get
import actors.actions.Use
import actors.statuses.Status
import render.Screen
import render.tilesets.Glyph
import things.Container
import things.Thing
import things.ThingHolder
import ui.input.Keydef
import ui.input.Mouse
import ui.panels.Toolbar
import util.groundAtPlayer
import util.log
import util.plural
import java.lang.Math.max
import java.lang.Math.min

class ThingsModal(
    private val thingHolder: ThingHolder,
    private val withContainer: Container? = null,
    private val withVendor: ThingHolder? = null,
    private val parentModal: ThingsModal? = null,
    private val sidecarTitle: String? = null,
) : SelectionModal(400, 500, default = 0, title = sidecarTitle ?: "bACkPACk",
    position = if (parentModal == null) Position.LEFT else Position.SIDECAR
), ContextMenu.ParentModal {

    companion object {
        private const val minHeight = 400
        private const val tabSize = 24
        private const val footerPad = 30
    }

    var cursorLocalX = 0
    var cursorLocalY = 0
    var scrollOffset = 0
    var maxItemsShown = 0

    var scrollStart = -1
    var scrollOffsetStart = -1

    enum class Tab(
        val x: Int,
        val title: String,
        val icon: Glyph,
        val category: Thing.Category?
    ) {
        ALL(80, "all", Glyph.INVENTORY_ALL, null),
        GEAR(130, "gear", Glyph.INVENTORY_GEAR, Thing.Category.GEAR),
        CONSUMABLE(180, "supplies", Glyph.INVENTORY_CONSUMABLES, Thing.Category.CONSUMABLE),
        TOOL(230, "tools", Glyph.INVENTORY_TOOLS, Thing.Category.TOOL),
        MISC(280, "misc", Glyph.INVENTORY_MISC, Thing.Category.MISC),
    }

    enum class ParamType(
        val calculate: (List<Thing>)->String
    ) {
        NONE({ things -> "" }),
        WEIGHT({ things -> String.format("%.1f", things.sumOf { (it.weight() * 10).toInt() } * 0.1f) + "lb" }),
        SELLPRICE({ things -> "$" + things.sumOf { 5L }.toString() }),
        BUYPRICE({ things -> "$" + things.sumOf { 5L }.toString() }),
    }

    enum class Context { BACKPACK, TO_CONTAINER, FROM_CONTAINER, TO_VENDOR, FROM_VENDOR }

    enum class Mode(
        val isSidecar: Boolean,
        val hasSidecar: Boolean,
        val listParam: ParamType,
        val showTotal: Boolean,
        val context: Context,
        val emptyMsg: String,
    ) {
        MAIN_BACKPACK(false, false, ParamType.WEIGHT, true, Context.BACKPACK, "You have %t."),
        MAIN_CONTAINER(false, true, ParamType.WEIGHT, true, Context.TO_CONTAINER, "You have %t."),
        SIDE_CONTAINER(true, true, ParamType.WEIGHT, false, Context.FROM_CONTAINER, "It holds %t."),
        MAIN_VENDOR(false, true, ParamType.SELLPRICE, true, Context.TO_VENDOR, "You have %t to sell."),
        SIDE_VENDOR(true, true, ParamType.BUYPRICE, true, Context.FROM_VENDOR, "There's %t for sale."),
    }

    class Group(
        var name: String,
        val singleName: String,
        var paramText: String,
        val tag: Thing.Tag,
        val things: MutableList<Thing>,
    )

    val mode = when {
        (withVendor != null && parentModal != null) -> Mode.SIDE_VENDOR
        (withVendor != null) -> Mode.MAIN_VENDOR
        (parentModal != null) -> Mode.SIDE_CONTAINER
        (withContainer != null) -> Mode.MAIN_CONTAINER
        else -> Mode.MAIN_BACKPACK
    }

    var grouped: List<Group> = ArrayList()

    var tab = Tab.ALL
    var hoveredTab: Tab? = null
    var tabY = 0

    var weightTotalText = ""
    var weightMaxText = ""

    var downArrowLit = false
    var upArrowLit = false
    var scrollBarLit = false

    init {
        zoomWhenOpen = 1.2f
        selectionBoxHeight = 18
        spacing = 27
        padding = 18
        headerPad = 110
        tabY = headerPad - 48
        maxItemsShown = ((Screen.height - headerPad - padding - footerPad) / spacing) - 3

        updateGrouped()
        changeSelection(if (grouped.isEmpty()) -1 else 0)

        if (mode.isSidecar) {
            this.isSidecar = true
        } else if (mode.hasSidecar) {
            withContainer?.also { container ->
                sidecar = ThingsModal(container, parentModal = this, sidecarTitle = container.name())
                moveToSidecar()
            }
        }
    }

    override fun myXmargin() = parentModal?.let { (it.width + xMargin + 20) } ?: xMargin

    override fun drawModalText() {
        super.drawModalText()
        if (maxSelection < 0) {
            drawOptionText(mode.emptyMsg.replace("%t",
                if (tab == Tab.ALL) "nothing" else "no " + tab.title),
                index = 0, preSpace = 30, colorOverride = Screen.fontColorDull)
        }

        for (i in scrollOffset..scrollOffset+maxSelection) {
            if (i <= grouped.lastIndex) {
                val group = grouped[i]
                drawOptionText(group.name, i - scrollOffset, 30,
                    addTag = group.things.first().listTag(), addCol = group.paramText, colX = 270)
            }
        }

        if (mode.listParam == ParamType.WEIGHT && mode.showTotal) {
            drawString("capacity ", padding, height - 30, Screen.fontColorDull, Screen.smallFont)
            drawString(weightMaxText, padding + 75, height - 30, Screen.fontColor, Screen.smallFont)
            drawString("total ", padding + 241, height - 30, Screen.fontColorDull, Screen.smallFont)
            drawString(weightTotalText, padding + 297, height - 30, Screen.fontColor, Screen.smallFont)
        }
    }

    override fun drawEntities() {
        super.drawEntities()
        if (!isAnimating()) {
            if (mode.context == Context.FROM_VENDOR || mode.context == Context.FROM_CONTAINER) {
                val x0 = x + padding
                val y0 = y + padding
                val batch = if (mode.context == Context.FROM_VENDOR) myActorBatch() else myThingBatch()
                val glyph = if (mode.context == Context.FROM_VENDOR) null else (thingHolder as Thing).glyph()
                glyph?.also { glyph ->
                    batch?.addPixelQuad(x0, y0, x0 + 48, y0 + 48, batch.getTextureIndex(glyph))
                }
            }

            for (i in scrollOffset..scrollOffset+maxSelection) {
                if (i <= grouped.lastIndex) {
                    val group = grouped[i]
                    drawOptionIcon(group.things.first(), i - scrollOffset)
                }
            }
        }
    }

    override fun drawBackground() {
        super.drawBackground()
        if (!isAnimating()) {
            Tab.values().forEach { tab ->
                if (this.hoveredTab == tab) {
                    drawSelectionBox(tab.x - 2, tabY + 8, tabSize - 4, tabSize - 4)
                }
                val alpha = if (hoveredTab != null) {
                    if (this.tab == tab) 1f else 0.5f
                } else {
                    if (this.tab == tab && tab != Tab.ALL) 0.5f else 0.1f
                }
                drawQuad(tab.x, tabY, tabSize, tabSize, tab.icon, alpha = alpha)
            }

            if (scrollStart > -1) {
                drawQuad(padding, headerPad - 10, width - padding * 2 - 10, height - headerPad - padding - 28,
                    Glyph.BOX_SHADOW, alpha = 0.2f)
            }

            if (scrollOffset > 0) {
                drawQuad(width - 33, headerPad - 46, 24, 24, Glyph.ARROW_UP)
                upArrowLit = true
            } else upArrowLit = false
            if (maxSelection + scrollOffset < grouped.lastIndex) {
                drawQuad(width - 33, height - 41, 24, 24, Glyph.ARROW_DOWN)
                downArrowLit = true
            } else downArrowLit = false
            if (upArrowLit || downArrowLit) {
                scrollBarLit = true
                val barx = width - 28
                val bary = headerPad - 10
                val barw = 14
                val barFullHeight = maxItemsShown * spacing + 2
                val thumby = bary + (scrollOffset * (barFullHeight / grouped.size))
                val thumbh = (((maxItemsShown).toFloat() / grouped.size.toFloat()) * barFullHeight.toFloat()).toInt() + 54
                drawQuad(barx, bary, barw, barFullHeight, Glyph.BOX_SHADOW, alpha = 0.4f)
                drawQuad(barx, thumby, barw, thumbh, Glyph.BOX_SHADOW, alpha = 0.8f)
            } else scrollBarLit = false

            drawOptionShade(rightSpace = 24)
        }
    }

    override fun selectPrevious() {
        if (selection == 0) {
            if (scrollOffset > 0) {
                scrollOffset--
                updateGrouped()
                return
            }
            changeSelection(-1)
            hoveredTab = tab
        } else {
            hoveredTab = null
            if (selection < 0 && maxItemsShown < grouped.size) {
                scrollOffset = grouped.size - maxItemsShown
                changeSelection(maxSelection)
                return
            }
            super.selectPrevious()
        }
    }

    override fun selectNext() {
        if (hoveredTab != null) {
            if (maxSelection >= 0) {
                changeSelection(0)
                hoveredTab = null
            }
        } else {
            if (selection == maxSelection && (maxItemsShown + scrollOffset) < grouped.size) {
                scrollOffset++
                updateGrouped()
                return
            } else if (selection == maxSelection) {
                scrollOffset = 0
                changeSelection(0)
                return
            }
            super.selectNext()
        }
    }

    override fun doSelect() {
        super.doSelect()
        if (selection < 0) {
            hoveredTab?.also { hovered ->
                tab = hovered
                updateGrouped()
            }
        } else {
            openContextMenu(grouped[selection + scrollOffset].things)
        }
    }

    private fun openContextMenu(things: List<Thing>) {
        val thing = things[0]
        Screen.addModal(ContextMenu(
            width / 2 + (parentModal?.width ?: 0), optionY(selection) - 4
        ).apply {
            zoomWhenOpen = this@ThingsModal.zoomWhenOpen
            parentModal = this@ThingsModal
            darkenUnderSidecar = true
            when (mode.context) {
                Context.BACKPACK -> {
                    thing.uses().forEach { (tag, it) ->
                        if (it.canDo(App.player, thing.xy().x, thing.xy().y, false)) {
                            addOption(it.command) { App.player.queue(Use(tag, thing.getKey(), it.duration)) }
                        }
                    }
                    if (things.size > 1) {
                        addOption("drop one ${thing.name()}")
                            { App.player.queue(Drop(thing.getKey(), groundAtPlayer().getHolderKey())) }
                        addOption("drop all ${thing.name().plural()}")
                            { things.forEach { App.player.queue(Drop(it.getKey(), groundAtPlayer().getHolderKey())) } }
                    } else {
                        addOption("drop ${thing.listName()}")
                            { App.player.queue(Drop(thing.getKey(), groundAtPlayer().getHolderKey())) }
                    }
                    if (thing.tag != App.player.thrownTag) {
                        addOption("ready ${thing.name().plural()} for throwing")
                            { App.player.readyForThrowing(thing.tag) }
                    }
                    thing.toolbarName()?.also { toolbarName ->
                        addOption("add to toolbar ($toolbarName)")
                            { Toolbar.beginAdd(thing) }
                    }
                    if (App.player.autoPickUpTypes.contains(thing.tag)) {
                        addOption("stop auto-pickup of ${thing.tag.pluralName}")
                            { App.player.removeAutoPickUpType(thing.tag) }
                    } else {
                        addOption("auto-pickup ${thing.tag.pluralName}")
                            { App.player.addAutoPickUpType(thing.tag) }
                    }
                }
                Context.TO_CONTAINER -> {
                    withContainer?.also { container ->
                        if (container.canAccept(thing)) {
                            if (things.size > 1) {
                                addOption("put one " + thing.name() + " " + container.preposition() + " " + container.name())
                                    { App.player.queue(Drop(thing.getKey(), container.getHolderKey())) }
                                addOption("put all " + thing.name().plural() + " " + container.preposition() + " " + container.name())
                                    { things.forEach { App.player.queue(Drop(it.getKey(), container.getHolderKey())) } }
                            } else {
                                addOption("put " + thing.name() + " " + container.preposition() + " " + container.name())
                                    { App.player.queue(Drop(thing.getKey(), container.getHolderKey())) }
                            }
                        }
                    }
                }
                Context.TO_VENDOR -> {

                }
                Context.FROM_CONTAINER -> {
                    if (!App.player.hasStatus(Status.Tag.BURDENED)) {
                        if (things.size > 1) {
                            addOption("take one ${thing.name()}")
                                { App.player.queue(Get(thing.getKey())) }
                            addOption("take all ${thing.name().plural()}")
                                { things.forEach { App.player.queue(Get(it.getKey())) } }
                        } else {
                            addOption("take ${thing.name()}")
                                { App.player.queue(Get(thing.getKey())) }
                        }
                    }
                }
                Context.FROM_VENDOR -> {

                }
            }
            addOption("examine " + thing.name()) {
                Screen.addModal(ExamineModal(thing, Position.CENTER_LOW).apply { zoomWhenOpen = this@ThingsModal.zoomWhenOpen })
            }
        })
    }

    override fun childSucceeded() {
        if (!mode.isSidecar && !mode.hasSidecar) {
            dismiss()
        }
    }

    override fun advanceTime(turns: Float) {
        super.advanceTime(turns)
        updateGrouped()
    }

    override fun onKeyDown(key: Keydef) {
        when (key) {
            Keydef.OPEN_INV, Keydef.CANCEL -> dismiss()
            Keydef.OPEN_GEAR -> replaceWith(GearModal(App.player))
            Keydef.OPEN_JOURNAL -> replaceWith(JournalModal())
            Keydef.OPEN_MAP -> replaceWith(MapModal())
            Keydef.OPEN_SKILLS -> replaceWith(SkillsModal(App.player))
            Keydef.MOVE_W -> {
                hoveredTab?.also {
                    val tabs = Tab.values()
                    val i = tabs.indexOf(it)
                    hoveredTab = tabs[if (i < 1) tabs.lastIndex else i-1]
                    doSelect()
                } ?: run { if (mode.isSidecar) {
                    returnToParent()
                } else dismiss() }
            }
            Keydef.MOVE_E -> {
                hoveredTab?.also {
                    val tabs = Tab.values()
                    val i = tabs.indexOf(it)
                    hoveredTab = tabs[if (i >= tabs.lastIndex) 0 else i+1]
                    doSelect()
                } ?: run { if (mode.hasSidecar) {
                    moveToSidecar()
                } else doSelect() }
            }
            else -> super.onKeyDown(key)
        }
    }

    override fun onMouseScrolled(amount: Float) {
        scrollOffset = max(0, min((scrollOffset + amount).toInt(), grouped.size - maxItemsShown))
    }

    override fun onMouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        cursorLocalX = screenX - x
        cursorLocalY = screenY - y
        if (scrollBarLit && (cursorLocalX in (width - 35)..(width - 4))) {
            if (cursorLocalY in (headerPad - 50)..(headerPad - 20) && upArrowLit) {
                scrollOffset--
                updateGrouped()
            } else if (cursorLocalY in (height - 44)..(height - 14) && downArrowLit) {
                scrollOffset++
                updateGrouped()
            } else if (cursorLocalY in (headerPad - 19)..(height - 45)) {
                scrollStart = cursorLocalY
                scrollOffsetStart = scrollOffset
            }
            return true
        }
        return super.onMouseClicked(screenX, screenY, button)
    }

    override fun mouseToOption(screenX: Int, screenY: Int): Int? {
        cursorLocalX = screenX - x
        cursorLocalY = screenY - y + (spacing / 2)
        hoveredTab = null
        if (scrollStart > -1) {
            val offset = (cursorLocalY - scrollStart) / spacing + scrollOffsetStart
            if (offset != scrollOffset) {
                scrollOffset = max(0, min(offset, grouped.size - maxItemsShown))
                updateGrouped()
            }
            return null
        }
        if (cursorLocalX in 1 until width - 36) {
            if (cursorLocalY > headerPad) {
                val hoverOption = (cursorLocalY - headerPad - 5) / spacing
                if (hoverOption in 0 .. maxSelection) {
                    return hoverOption
                }
            } else if (cursorLocalY > tabY - 10) {
                Tab.values().forEach { thisTab ->
                    if (cursorLocalX in thisTab.x - 5 until thisTab.x + tabSize + 5) {
                        hoveredTab = thisTab
                    }
                }
            }
        }
        return null
    }

    override fun onMouseUp(screenX: Int, screenY: Int, button: Mouse.Button) {
        scrollStart = -1
        scrollOffsetStart = -1
        super.onMouseUp(screenX, screenY, button)
    }

    override fun moveToSidecar() {
        sidecar?.also { sidecar ->
            if (sidecar is ThingsModal && sidecar.maxSelection > -1) {
                super.moveToSidecar()
            }
        }
    }

    private fun returnToParent() {
        parentModal?.also {
            if (it.grouped.isNotEmpty()) {
                it.returnFromSidecar()
                changeSelection(-1)
            } else dismiss()
        }
    }

    override fun returnFromSidecar() {
        sidecar?.also { sidecar ->
            changeSelection(max(0, min(maxSelection, (sidecar as ThingsModal).selection)))
        }
        super.returnFromSidecar()
    }


    private fun updateGrouped() {
        var weightTotal = 0f
        val things = tab.category?.let { thingHolder.contents().filter { it.category() == tab.category }} ?: thingHolder.contents()
        grouped = ArrayList<Group>().apply {
            things.forEach { thing ->
                weightTotal += thing.weight()
                var placed = false
                forEach {
                    if (it.tag == thing.tag && it.singleName == thing.listName() && thing.canListGrouped()) {
                        it.things.add(thing)
                        it.paramText = mode.listParam.calculate(it.things)
                        it.name = it.things.size.toString() + " " + it.things[0].name().plural()
                        placed = true
                    }
                }
                if (!placed) {
                    add(Group(thing.name(), thing.listName(), mode.listParam.calculate(listOf(thing)), thing.tag, mutableListOf(thing)))
                }
            }
        }.sortedBy { it.singleName }

        maxSelection = min(maxItemsShown - 1, grouped.size - 1)
        if (grouped.size <= maxItemsShown) scrollOffset = 0
        changeSelection(min(maxSelection, selection))

        weightTotalText = String.format("%.1f", weightTotal) + "lb"
        weightMaxText = String.format("%.1f", App.player.carryingCapacity()) + "lb"

        adjustHeight()
    }

    fun adjustHeight(fromPartner: Boolean = false) {
        val otherMax1 = sidecar?.let { sidecar ->
            if (sidecar is ThingsModal) sidecar.grouped.size else 0
        } ?: 0
        val otherMax2 = parentModal?.grouped?.size ?: 0
        val otherMax = max(otherMax1, otherMax2)
        val maxItems = min(maxItemsShown, max(grouped.size, otherMax))

        height = max(minHeight, headerPad + spacing * maxItems + padding + footerPad)
        super.onResize(Screen.width, Screen.height)

        if (!fromPartner) {
            sidecar?.also { sidecar ->
                if (sidecar is ThingsModal) sidecar.adjustHeight(fromPartner = true)
            }
            parentModal?.adjustHeight(fromPartner = true)
        }
    }

    override fun onResize(width: Int, height: Int) {
        adjustHeight(true)
    }

    fun shownThing(): Thing? = if (isInSidecar && sidecar is ThingsModal) (sidecar as ThingsModal).shownThing() else
        if (selection > -1) grouped[selection].things[0] else null
}

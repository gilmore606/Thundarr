package ui.panels

import actors.Player
import render.Screen
import ui.modals.InventoryModal
import util.XY
import util.wrapText
import world.Entity
import world.terrains.Terrain

object LookPanel : ShadedPanel() {

    private const val padding = 12

    private var lastTime = 0.0
    private val lastPos = XY(showPos().x, showPos().y)
    private var lastInventory: Entity? = null
    var entity: Entity? = showEntity()
    private var wrapped = ArrayList<String>()

    init {
        this.width = RIGHT_PANEL_WIDTH
        this.height = 180
    }

    override fun onResize(width: Int, height: Int) {
        super.onResize(width, height)
        x = width - (this.width) - xMargin
        y = yMargin + if (Screen.panels.contains(StatusPanel)) (StatusPanel.height + padding * 2) else 0
    }

    override fun drawText() {
        entity?.also { entity ->
            drawString(entity.name(), padding + 28, padding)
            drawWrappedText(wrapped, padding, padding + 30)
        }

    }

    private fun drawWrappedText(text: List<String>, x0: Int, y0: Int) {
        text.forEachIndexed { n, line ->
            drawString(line, x0, y0 + n * 20, Screen.fontColorDull, Screen.smallFont)
        }
    }

    override fun drawBackground() {
        entity?.also {
            super.drawBackground()
        }

        val newTime = App.time
        val newPos = showPos()
        var newInventory = lastInventory
        val inventory = isInventory()
        if (inventory != null) {
            newInventory = inventory.shownThing()
        }

        if (newPos != lastPos || newTime > lastTime || newInventory != lastInventory) {
            lastTime = newTime
            lastInventory = newInventory
            entity = showEntity()
            entity?.also { wrapped = wrapText(it.description(), width, padding) } ?: wrapped.clear()
        }
    }

    override fun drawEntities() {
        entity?.also { entity ->
            val glyph = entity.glyph()
            val x0 = x + padding - 10
            val y0 = y + padding - 12 - (if (glyph.tall) 32 else 0)
            entity.uiBatch().addPixelQuad(x0, y0, x0 + 32, y0 + (if (glyph.tall) 64 else 32),
                entity.uiBatch().getTextureIndex(entity.glyph(), entity.level(), entity.xy().x, entity.xy().y),
                hue = entity.hue(), isTall = glyph.tall)
        }
    }

    private fun showPos() = Screen.cursorPosition ?: App.player.xy

    private fun isInventory(): InventoryModal? = Screen.panels.firstOrNull { it is InventoryModal } as InventoryModal?

    private fun showEntity(): Entity? {
        val inventory = isInventory()
        if (inventory != null) {
            return inventory.shownThing()
        }
        val pos = showPos()
        var e: Entity? = null
        val actor = App.level.actorAt(pos.x, pos.y)
        if (actor != null && actor !is Player) e = actor else {
            val things = App.level.thingsAt(pos.x, pos.y)
            if (things.isNotEmpty()) {
                e = things[0]
            }
        }
        e?.also { return it }
        return Terrain.get(App.level.getTerrain(App.player.xy.x, App.player.xy.y))
    }
}

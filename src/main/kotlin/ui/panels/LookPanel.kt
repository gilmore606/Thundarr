package ui.panels

import actors.Player
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import render.GameScreen
import things.Thing
import ui.modals.InventoryModal
import util.XY
import world.Entity

object LookPanel : ShadedPanel() {

    private val padding = 12

    private var lastTime = 0.0
    private val lastPos = XY(showPos().x, showPos().y)
    private var lastInventory = -1
    private var entity: Entity? = showEntity()
    private var wrapped = ArrayList<String>()

    init {
        this.width = 200
        this.height = 180
    }

    override fun onResize(width: Int, height: Int) {
        super.onResize(width, height)
        x = width - (this.width) - xMargin
        y = yMargin + if (GameScreen.panels.contains(StatusPanel)) (StatusPanel.height + padding) else 0
    }

    override fun drawText() {
        entity?.also { entity ->
            drawString(entity.name(), padding + 28, padding)
            drawWrappedText(wrapped, padding, padding + 30)
        }

    }

    private fun drawWrappedText(text: List<String>, x0: Int, y0: Int) {
        text.forEachIndexed { n, line ->
            drawString(line, x0, y0 + n * 20, GameScreen.fontColorDull, GameScreen.smallFont)
        }
    }

    private fun wrapText(text: String) {
        wrapped.clear()
        var remaining = text
        var nextLine = ""
        var linePixelsLeft = (width - padding * 2)
        val spaceWidth = GlyphLayout(GameScreen.smallFont, " ").width.toInt()
        while (remaining.isNotEmpty() || remaining == " ") {
            // get next word
            val space = remaining.indexOf(' ')
            var word = ""
            if (space >= 0) {
                word = remaining.substring(0, space)
                remaining = remaining.substring(space + 1, remaining.length)
            } else {
                word = remaining
                remaining = ""
            }
            if (word != " ") {
                val wordWidth = GlyphLayout(GameScreen.smallFont, word).width.toInt()
                if (nextLine == "" || wordWidth <= linePixelsLeft) {
                    nextLine += word + " "
                    linePixelsLeft -= wordWidth + spaceWidth
                } else {
                    wrapped.add(nextLine)
                    nextLine = word + " "
                    linePixelsLeft = (width - padding * 2) - wordWidth - spaceWidth
                }
            }
        }
        if (nextLine != "") wrapped.add(nextLine)
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
            newInventory = inventory.selection
        }

        if (newPos != lastPos || newTime > lastTime || newInventory != lastInventory) {
            lastTime = newTime
            lastInventory = newInventory
            entity = showEntity()
            entity?.also { wrapText(it.description()) } ?: wrapped.clear()
        }
    }

    override fun drawEntities() {
        entity?.also { entity ->
            val x0 = x + padding - 10
            val y0 = y + padding - 12
            entity.uiBatch().addPixelQuad(x0, y0, x0 + 32, y0 + 32,
                entity.uiBatch().getTextureIndex(entity.glyph(), entity.level(), entity.xy()?.x ?: 0, entity.xy()?.y ?: 0))
        }
    }

    private fun showPos() = GameScreen.cursorPosition ?: App.player.xy

    private fun isInventory(): InventoryModal? = GameScreen.panels.firstOrNull { it is InventoryModal } as InventoryModal?

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

        return e
    }
}

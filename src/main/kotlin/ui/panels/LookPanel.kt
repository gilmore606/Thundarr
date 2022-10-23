package ui.panels

import com.badlogic.gdx.graphics.g2d.GlyphLayout
import render.GameScreen
import things.Thing
import util.XY
import world.Entity

object LookPanel : ShadedPanel() {

    private val padding = 12

    private var lastTime = 0.0
    private val lastPos = XY(showPos().x, showPos().y)
    private var entity: Entity? = showEntity()
    private var wrapped = ArrayList<String>()

    init {
        this.width = 200
        this.height = 180
    }

    override fun onResize(width: Int, height: Int) {
        x = width - (this.width + padding * 2)
        y = padding + if (GameScreen.panels.contains(StatusPanel)) (StatusPanel.height + padding) else 0
    }

    override fun drawText() {
        entity?.also { entity ->
            drawString(entity.name(), padding + 28, padding)
            drawWrappedText(wrapped, padding, padding + 30)
        }

    }

    private fun drawWrappedText(text: List<String>, x0: Int, y0: Int) {
        text.forEachIndexed { n, line ->
            drawString(line, x0, y0 + n * 20)
        }
    }

    private fun wrapText(text: String) {
        wrapped.clear()
        var remaining = text
        var nextLine = ""
        var linePixelsLeft = (width - padding * 2)
        val spaceWidth = GlyphLayout(GameScreen.font, " ").width.toInt()
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
                val wordWidth = GlyphLayout(GameScreen.font, word).width.toInt()
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
        super.drawBackground()

        val newTime = App.time
        val newPos = showPos()
        if (newPos != lastPos || newTime > lastTime) {
            lastTime = newTime
            entity = showEntity()
            entity?.also { wrapText(it.description()) } ?: wrapped.clear()
        }
    }

    override fun drawThings() {
        entity?.also { entity ->
            if (entity is Thing) {
                val x0 = x + padding - 10
                val y0 = y + padding - 12
                thingBatch.addPixelQuad(x0, y0, x0 + 32, y0 + 32, thingBatch.getTextureIndex(entity.glyph()))
            }
        }
    }

    private fun showPos() = GameScreen.cursorPosition ?: App.player.xy

    private fun showEntity(): Entity? {
        val pos = showPos()
        var e: Entity? = null
        val things = App.level.thingsAt(pos.x, pos.y)
        if (things.isNotEmpty()) {
            e = things[0]
        }

        return e
    }
}

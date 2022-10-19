package ui.panels

import render.GameScreen
import java.lang.Float.max


object ConsolePanel : Panel() {

    private val maxLines = 7
    private val lineSpacing = 21
    private val padding = 12
    private val lines: MutableList<String> = mutableListOf<String>().apply {
        repeat(maxLines) { add("") }
    }
    private var scroll = 0f
    private var scrollSpeed = 90f

    fun say(text: String) {
        if (text == lines.last()) return
        lines.add(text)
        scroll += lineSpacing.toFloat()
        if (lines.size > maxLines) {
            lines.removeFirst()
        }
    }

    override fun onResize(width: Int, height: Int) {
        val paddingScaled = padding + (width - 800) / 100
        x = paddingScaled
        y = height - (maxLines * lineSpacing) - paddingScaled
        this.width = width - (paddingScaled * 2)
        this.height = (maxLines * lineSpacing)
    }

    override fun onRender(delta: Float) {
        scroll = max(0f, scroll - (scrollSpeed * delta))
    }

    override fun drawText() {
        var offset = scroll.toInt()
        lines.forEachIndexed { n, line ->
            drawString(line, 0, offset,
                if (n == lines.lastIndex) GameScreen.fontColor else GameScreen.fontColorDull)
            offset += lineSpacing
        }
    }
}

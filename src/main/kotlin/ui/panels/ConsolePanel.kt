package ui.panels

import render.GameScreen


object ConsolePanel : Panel() {

    private val maxLines = 7
    private val lineSpacing = 5
    private val padding = 12
    private val lines: MutableList<String> = mutableListOf<String>().apply {
        repeat(maxLines) { add("") }
    }

    fun say(text: String) {
        if (text == lines.last()) return
        lines.add(text)
        if (lines.size > maxLines) {
            lines.removeFirst()
        }
    }

    override fun onResize(width: Int, height: Int) {
        val paddingScaled = padding + (width - 800) / 100
        x = paddingScaled
        y = height - (maxLines * (lineSpacing + GameScreen.fontSize)) - paddingScaled
        this.width = width - (paddingScaled * 2)
        this.height = (maxLines * (lineSpacing + GameScreen.fontSize))
    }

    override fun drawText() {
        var offset = 0
        lines.forEachIndexed { n, line ->
            drawString(line, 0, offset,
                if (n == lines.lastIndex) GameScreen.fontColor else GameScreen.fontColorDull)
            offset += lineSpacing + GameScreen.fontSize
        }
    }

    override fun drawBackground() { }
}

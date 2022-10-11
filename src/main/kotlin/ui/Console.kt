package ui

import render.GameScreen


object Console : Panel() {

    private val maxLines = 6
    private val lineSpacing = 3
    private val padding = 12
    private val lines: MutableList<String> = mutableListOf<String>().apply {
        repeat(maxLines) { add("") }
    }

    fun say(text: String) {
        lines.add(text)
        if (lines.size > maxLines) {
            lines.removeFirst()
        }
    }

    override fun onResize(width: Int, height: Int) {
        x = padding
        y = height - (maxLines * (lineSpacing + GameScreen.fontSize)) - padding
        this.width = width - (padding * 2)
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

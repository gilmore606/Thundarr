package ui


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
        y = height - (maxLines * (lineSpacing + fontSize)) - padding
        this.width = width - (padding * 2)
        this.height = (maxLines * (lineSpacing + fontSize))
    }

    override fun drawText() {
        var offset = 0
        lines.forEachIndexed { n, line ->
            drawString(line, 0, offset, n == lines.lastIndex)
            offset += lineSpacing + fontSize
        }
    }

    override fun drawBackground() { }
}

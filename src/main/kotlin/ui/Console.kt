package ui


object Console : Panel() {

    val lines: MutableList<String> = mutableListOf()
    private val maxLines = 6
    private val lineSpacing = 20
    private var c = 0

    fun say(text: String) {
        lines.add("$c : $text")
        c++
        if (lines.size > maxLines) {
            lines.removeFirst()
        }
    }

    override fun onResize(width: Int, height: Int) {
        super.onResize(width, height)
        x = 15
        y = height - (maxLines * lineSpacing) - 15
    }

    override fun drawText() {
        var offset = 0
        lines.forEach {
            drawString(it, 0, offset)
            offset += lineSpacing
        }
    }
}

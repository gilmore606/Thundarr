package ui.panels

import com.badlogic.gdx.graphics.Color
import org.lwjgl.Sys
import render.GameScreen
import util.LightColor
import util.log
import world.Level
import java.lang.Float.max
import java.lang.Float.min


object ConsolePanel : Panel() {

    private val maxLines = 7
    private val lineSpacing = 21
    private val padding = 12
    private val lines: MutableList<String> = mutableListOf<String>().apply {
        repeat(maxLines) { add("") }
    }
    private var lastLineMs = System.currentTimeMillis()
    private var scroll = 0f
    private var scrollSpeed = 90f
    private val colorDull = Color(0.7f, 0.7f, 0.4f, 0.7f)
    private val color = Color(0.9f, 0.9f, 0.7f, 0.9f)

    private const val burstOnSay = 0.5f
    private const val burstDecay = 0.2f
    private const val burstMax = 2f
    private const val dimDelayMs = 2000L
    private const val dimLevel = 0.5f
    private var burst = 1f
    private var burstFloor = 1f

    enum class Reach { VISUAL, AUDIBLE, LEVEL }

    fun say(text: String) {
        burst = min(burstMax, burst + burstOnSay)
        burstFloor = 1f
        lastLineMs = System.currentTimeMillis()
        if (text == lines.last()) return
        log.info("  \"$text\"")
        lines.add(text)
        scroll += lineSpacing.toFloat()
        if (lines.size > maxLines) {
            lines.removeFirst()
        }
    }

    fun announce(level: Level?, x: Int, y: Int, reach: Reach, text: String) {
        if (level == App.level) {
            say(text)
        }
    }

    override fun onResize(width: Int, height: Int) {
        super.onResize(width, height)
        this.height = (maxLines * lineSpacing) + padding * 2
        x = xMargin
        y = height - this.height - yMargin
        this.width = width - (xMargin * 2)
    }

    override fun onRender(delta: Float) {
        scroll = max(0f, scroll - (scrollSpeed * delta))

        if (burstFloor == 1f && System.currentTimeMillis() - dimDelayMs > lastLineMs) {
            burstFloor = dimLevel
        }
        burst = max(burstFloor, burst - burstDecay * delta)
        color.apply {
            r = min(1f, GameScreen.fontColor.r * burst)
            g = min(1f, GameScreen.fontColor.g * burst)
            b = min(1f, GameScreen.fontColor.b * burst)
        }
        colorDull.apply {
            r = min(1f, GameScreen.fontColorDull.r * burst)
            g = min(1f, GameScreen.fontColorDull.g * burst)
            b = min(1f, GameScreen.fontColorDull.b * burst)
        }
    }

    override fun drawText() {
        var offset = scroll.toInt() + padding
        lines.forEachIndexed { n, line ->
            drawString(line, padding, offset,
                if (n == lines.lastIndex) color else colorDull)
            offset += lineSpacing
        }
    }
}

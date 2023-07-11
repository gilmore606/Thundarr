package ui.panels

import actors.Player
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import kotlinx.coroutines.launch
import ktx.async.KtxAsync
import render.Screen
import render.tilesets.Glyph
import things.Thing
import ui.input.Keydef
import util.XY
import util.log
import world.Entity
import world.gen.Metamap
import world.level.Level
import java.lang.Float.max
import java.lang.Float.min


object Console : Panel() {

    private val maxLines = 40
    private val maxLinesShown = 7
    private val lineSpacing = 21
    private val padding = 12
    val lines: MutableList<String> = mutableListOf<String>()

    private var lastLineMs = System.currentTimeMillis()
    private var scroll = 0f
    private var scrollSpeed = 80f
    private val colorDull = Color(0.7f, 0.7f, 0.4f, 0.7f)
    private val color = Color(0.9f, 0.9f, 0.7f, 0.9f)
    private val floatColor = Color(0.9f, 0.9f, 0.7f, 0f)
    private val floatHeightAbovePlayer = 70
    private var floatText = ""
    private var floatAge = 0f
    private var floatWidth = 0
    private var floatFadeTime = 2f

    private const val burstOnSay = 0.5f
    private const val burstDecay = 0.2f
    private const val burstMax = 1.3f
    private const val dimDelayMs = 1500L
    private const val dimLevel = 0.5f
    private var burst = 1f
    private var burstFloor = 1f
    var mouseInside = false
    private var modalUp = false

    var inputActive = false
    var inputCommand = ""

    private val pronounSubs = mutableMapOf<String,(Entity?,Entity?,Entity?)->String>().apply {
        set("n") { s,d,i -> s?.name() ?: "???" } // subject
        set("d") { s,d,i -> d?.name() ?: "???" } // direct object
        set("t") { s,d,i -> d?.name() ?: "???" } // direct object
        set("i") { s,d,i -> i?.name() ?: "???" } // indirect object
        set("in") { s,d,i -> s?.iname() ?: "???" } // a creature
        set("id") { s,d,i -> d?.iname() ?: "???" }
        set("it") { s,d,i -> d?.iname() ?: "???" }
        set("ii") { s,d,i -> i?.iname() ?: "???" }
        set("dn") { s,d,i -> s?.dname() ?: "???" } // the creature
        set("dd") { s,d,i -> d?.dname() ?: "???" }
        set("dt") { s,d,i -> d?.dname() ?: "???" }
        set("di") { s,d,i -> i?.dname() ?: "???" }
        set("s") { s,d,i -> s?.gender()?.ps ?: "???" } // he
        set("o") { s,d,i -> s?.gender()?.po ?: "???" } // him
        set("p") { s,d,i -> s?.gender()?.pp ?: "???" } // his
        set("r") { s,d,i -> s?.gender()?.pr ?: "???" } // himself
        set("ds") { s,d,i -> d?.gender()?.ps ?: "???" }
        set("do") { s,d,i -> d?.gender()?.po ?: "???" }
        set("dp") { s,d,i -> d?.gender()?.pp ?: "???" }
        set("dr") { s,d,i -> d?.gender()?.pr ?: "???" }
        set("is") { s,d,i -> i?.gender()?.ps ?: "???" }
        set("io") { s,d,i -> i?.gender()?.po ?: "???" }
        set("ip") { s,d,i -> i?.gender()?.pp ?: "???" }
        set("ir") { s,d,i -> i?.gender()?.pr ?: "???" }
        set("N") { s,d,i -> s?.name()?.capitalize() ?: "???" } // subject
        set("D") { s,d,i -> d?.name()?.capitalize() ?: "???" } // direct object
        set("T") { s,d,i -> d?.name()?.capitalize() ?: "???" } // direct object
        set("I") { s,d,i -> i?.name()?.capitalize() ?: "???" } // indirect object
        set("In") { s,d,i -> s?.iname()?.capitalize() ?: "???" } // a creature
        set("Id") { s,d,i -> d?.iname()?.capitalize() ?: "???" }
        set("It") { s,d,i -> d?.iname()?.capitalize() ?: "???" }
        set("Ii") { s,d,i -> i?.iname()?.capitalize() ?: "???" }
        set("Dn") { s,d,i -> s?.dname()?.capitalize() ?: "???" } // the creature
        set("Dd") { s,d,i -> d?.dname()?.capitalize() ?: "???" }
        set("Dt") { s,d,i -> d?.dname()?.capitalize() ?: "???" }
        set("Di") { s,d,i -> i?.dname()?.capitalize() ?: "???" }
        set("S") { s,d,i -> s?.gender()?.ps?.capitalize() ?: "???" } // he
        set("O") { s,d,i -> s?.gender()?.po?.capitalize() ?: "???" } // him
        set("P") { s,d,i -> s?.gender()?.pp?.capitalize() ?: "???" } // his
        set("R") { s,d,i -> s?.gender()?.pr?.capitalize() ?: "???" } // himself
        set("Ds") { s,d,i -> d?.gender()?.ps?.capitalize() ?: "???" }
        set("Do") { s,d,i -> d?.gender()?.po?.capitalize() ?: "???" }
        set("Dp") { s,d,i -> d?.gender()?.pp?.capitalize() ?: "???" }
        set("Dr") { s,d,i -> d?.gender()?.pr?.capitalize() ?: "???" }
        set("Is") { s,d,i -> i?.gender()?.ps?.capitalize() ?: "???" }
        set("Io") { s,d,i -> i?.gender()?.po?.capitalize() ?: "???" }
        set("Ip") { s,d,i -> i?.gender()?.pp?.capitalize() ?: "???" }
        set("Ir") { s,d,i -> i?.gender()?.pr?.capitalize() ?: "???" }
    }

    enum class Reach { VISUAL, AUDIBLE, LEVEL, WORLD }

    init {
        clear()
    }

    fun clear() {
        lines.clear()
        repeat (maxLines) { lines.add("") }
    }

    fun say(text: String) {
        if (text == "") return
        if (App.attractMode) return
        burst = min(if (App.attractMode) burstMax * 0.5f else burstMax, burst + if (App.attractMode) burstOnSay * 0.5f else burstOnSay)
        burstFloor = 1f
        lastLineMs = System.currentTimeMillis()
        if (text == lines.last()) {
            resetFloat(text)
            return
        }
        log.info("  \"$text\"")
        addLine(text)
    }

    private fun addLine(line: String) {
        lines.add(line)
        scroll += lineSpacing.toFloat()
        if (lines.size > maxLines) {
            lines.removeFirst()
        }
        resetFloat(line)
    }

    fun sayFromThread(text: String) {
        if (App.attractMode) return
        KtxAsync.launch {
            say(text)
        }
    }

    fun announce(level: Level?, x: Int, y: Int, reach: Reach, text: String) {
        if (level == App.level || reach == Reach.WORLD) {
            if (reach != Reach.VISUAL || level?.visibilityAt(x, y) == 1f) {
                say(text)
            }
        }
    }

    fun sayAct(playerMsg: String, otherMsg: String, subject: Entity, direct: Entity? = null, indirect: Entity? = null,
               reach: Reach = Reach.VISUAL, source: XY? = null) {
        var out = ""
        var code = ""
        var inCode = false

        fun process() {
            pronounSubs[code]?.also { h -> out += h(subject, direct, indirect) } ?: run { out += "???" }
            code = ""
            inCode = false
        }

        (if (subject is Player) playerMsg else otherMsg).forEach { c ->
            if (c == '%') {
                if (inCode) process() else inCode = true
            } else if (c !in "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" && inCode) {
                process()
                out += c
            } else if (inCode) {
                code += c
            } else {
                out += c
            }
        }
        if (code.isNotEmpty()) process()

        val x = source?.x ?: subject.xy().x
        val y = source?.y ?: subject.xy().y
        if (subject is Player) say(out) else {
            announce(subject.level(), x, y, reach, out)
        }
    }

    fun restoreLines(newLines: List<String>) {
        lines.clear()
        lines.addAll(newLines)
    }

    override fun onResize(width: Int, height: Int) {
        super.onResize(width, height)
        this.height = (maxLines * lineSpacing) + padding * 2
        x = xMargin
        y = height - this.height - yMargin - padding
        this.width = width - (xMargin * 2)
    }

    override fun onRender(delta: Float) {
        scroll = max(0f, scroll - (scrollSpeed * delta * scroll * 0.05f))

        if (burstFloor == 1f && Screen.timeMs - dimDelayMs > lastLineMs) {
            burstFloor = dimLevel
        }
        modalUp = Screen.topModal != null
        if (modalUp) burst = burstMax
        if (!(mouseInside || inputActive || modalUp)) burst = max(burstFloor, burst - burstDecay * delta)

        color.apply {
            r = min(1f, Screen.fontColor.r * burst)
            g = min(1f, Screen.fontColor.g * burst)
            b = min(1f, Screen.fontColor.b * burst)
            a = min(1f, Screen.fontColor.a * max(1f, burst))
        }
        colorDull.apply {
            r = min(1f, Screen.fontColorDull.r * burst)
            g = min(1f, Screen.fontColorDull.g * burst)
            b = min(1f, Screen.fontColorDull.b * burst)
            a = min(1f, Screen.fontColorDull.a * burst)
        }
        floatAge += delta
    }

    override fun mouseMovedTo(screenX: Int, screenY: Int) {
        super.mouseMovedTo(screenX, screenY)
        if (screenY > this.y + (maxLines - maxLinesShown) * lineSpacing && screenX < this.width - RIGHT_PANEL_WIDTH) {
            mouseInside = true
            this.burst = 1.2f
        } else mouseInside = false
    }

    override fun drawText() {
        var offset = scroll.toInt() + padding
        lines.forEachIndexed { n, line ->
            if ((mouseInside || inputActive) || (n >= lines.size - maxLinesShown)) {
                drawString(
                    line, padding, offset,
                    if (n == lines.lastIndex) color else colorDull
                )
            }
            offset += lineSpacing
        }
        if (!App.attractMode && floatAge <= floatFadeTime) {
            floatColor.a = min(1.0f, 1.2f - (floatAge / floatFadeTime * 1.2f))
            drawStringAbsolute(floatText,
                Screen.width / 2 - floatWidth / 2,
                Screen.height / 2 - floatHeightAbovePlayer + if (Metamap.isWorking) 150 else 0,
                floatColor, Screen.smallFont)
        }
    }

    override fun drawBackground() {
        if ((mouseInside || inputActive) && !App.attractMode) {
            Screen.uiBatch.addPixelQuad(xMargin, yMargin + EnvPanel.height + yMargin,
                Screen.width - xMargin * 2 - RadarPanel.width, Screen.height - yMargin,
                Screen.uiBatch.getTextureIndex(Glyph.CONSOLE_SHADE), alpha = 0.6f)
        }
    }

    private fun resetFloat(newText: String) {
        floatText = newText
        floatAge = 0f
        floatWidth = measure(floatText, Screen.smallFont)
        floatFadeTime = 1f + (floatText.length * 0.04f)
    }

    fun openDebug() {
        log.info("opening debug console")
        inputActive = true
        lines.add("> _")
        inputCommand = ""
        burst = 1.2f
    }

    fun keycodeDown(keycode: Int) {
        val line = lines.last()
        when (keycode) {
            Input.Keys.ENTER -> {
                inputActive = false
                lines.removeLast()
                addLine("> $inputCommand")
                debugCommand(inputCommand)
                inputCommand = ""
            }
            Input.Keys.BACKSPACE -> {
                if (inputCommand.isNotEmpty()) inputCommand = inputCommand.dropLast(1)
                updateCommand()
            }
            Input.Keys.SPACE -> {
                inputCommand += " "
                updateCommand()
            }
            Input.Keys.GRAVE -> {
                inputActive = false
                lines.removeLast()
                inputCommand = ""
            }
            else -> {
                inputCommand += Input.Keys.toString(keycode).toLowerCase()
                updateCommand()
            }
        }
    }

    private fun updateCommand() {
        lines.removeLast()
        lines.add("> ${inputCommand}_")
    }

    private fun debugCommand(commandString: String) {
        val words = commandString.split(" ")
        val command = words[0]
        when (command) {
            "help", "?" -> {
                say("Commands:")
                say("  HP <amount> - Change hp to amount (or full if no amount)")
                say("  GET <item> - Spawn item on ground")
                say("  GET <number> <item> - Spawn number of item on ground")
                say("  SPAWN <npc> - Spawn NPC in front of you")
                say("  TIME <hours> - Advance time n hours")
                say("  WEATHER <value> - Change weather to 0.0 - 1.0 (or 0 if no value)")
                say("  XP <amount> - Earn xp")
            }
            "hp" -> { debugHp(words) }
            "weather" -> { debugWeather(words) }
            "get" -> { debugGet(words) }
            "xp" -> { debugXP(words) }
            else -> say("I don't understand that.")
        }
    }

    private fun debugXP(words: List<String>) {
        val xp = if (words.size > 1) words[1].toInt() else 500
        App.player.gainXP(xp)
        say("earned $xp free XP.")
    }

    private fun debugHp(words: List<String>) {
        val newhp = if (words.size > 1) words[1].toInt().toFloat() else App.player.hpMax
        App.player.hp = newhp
        say("hp set to $newhp / ${App.player.hpMax}.")
    }

    private fun debugWeather(words: List<String>) {
        val newWeather = if (words.size > 1) words[1].toFloat() else 0f
        App.weather.forceWeather(newWeather)
        say("Weather forced to $newWeather.")
    }

    private fun debugGet(words: List<String>) {
        if (words.size <= 1) {
            say("Usage: GET <item name>")
            return
        }
        var amount = words[1].toIntOrNull() ?: 0
        var itemName = words.drop(2).joinToString(" ")
        if (amount < 1) {
            amount = 1
            itemName = words.drop(1).joinToString(" ")
        }
        Thing.Tag.values().firstOrNull { it.singularName == itemName }?.also { tag ->
            spawnThings(amount, tag)
        } ?: Thing.Tag.values().firstOrNull { it.singularName.contains(itemName, ignoreCase = true) }?.also { tag ->
            spawnThings(amount, tag)
        } ?: run {
            say("I don't know about anything called '${words[1]}'.")
        }
    }

    private fun spawnThings(amount: Int, tag: Thing.Tag) {
        repeat (amount) {
            val thing = tag.spawn.invoke()
            thing.moveTo(App.player.level!!, App.player.xy.x, App.player.xy.y)
            say("Spawned ${amount} ${tag}.")
        }
    }
}

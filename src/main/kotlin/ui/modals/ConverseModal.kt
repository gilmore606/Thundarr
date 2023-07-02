package ui.modals

import actors.NPC
import audio.Speaker
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ktx.async.KtxAsync
import render.Screen
import render.batches.QuadBatch
import render.tilesets.Glyph
import ui.input.Keydef
import ui.input.Mouse
import util.Stack
import util.log
import util.wrapText
import java.lang.Math.max

class ConverseModal(
    private val talker: NPC,
    position: Position = Position.LEFT,
): Modal(
    650, 400, talker.iname(),
    position = position,
) {
    val portrait = talker.portraitGlyph()
    val portraitBatch = QuadBatch(Screen.portraitTileSet, maxQuads = 100)

    var bubbleProgress: Float? = null

    override fun drawEverything() {
        super.drawEverything()
        portraitBatch.clear()
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        Gdx.gl.glEnable(GL20.GL_BLEND)
        if (!isAnimating()) drawPortrait()
        portraitBatch.draw()
    }

    interface Source {
        fun getConversationTopic(topic: String): Scene?
        fun optionsForText(text: String): List<Option> = listOf()
    }

    class Option(
        val topic: String,
        val question: String,
        val onPick: (()->Unit)? = null,
    )

    class Scene(
        val topic: String,
        val text: String,
        val options: List<Option> = listOf(),
        val clearStack: Boolean = false,
    )


    private var scene: Scene? = null
    private val topicStack = Stack<String>()

    private var wrappedText = mutableListOf<String>()

    var selection = -1
    private var maxSelection = 0

    private val textSpacing = 24
    val padding = 22
    private var optionStartY = 0
    private val optionSpacing = 32
    private val headerPad = 50
    private val portraitSize = 96
    private val portraitXpad = if (portrait == null) 0 else 120

    init {
        zoomWhenOpen = 1.8f

        changeTopic(Option("hello", ""))
    }

    override fun onRender(delta: Float) {
        bubbleProgress?.also {
            bubbleProgress = it + delta * 1.3f
            if (it + delta > 1f) bubbleProgress = null
        }
    }

    private fun adjustHeight() {
        height = padding * 2 +
                max(portraitSize + 16, wrappedText.size * textSpacing) +
                (scene?.options?.size ?: 1) * optionSpacing +
                84
        onResize(Screen.width, Screen.height)
    }

    private fun changeTopic(newTopic: Option) {
        var toTopic = newTopic
        if (newTopic.topic == "back") {
            if (topicStack.isEmpty()) {
                toTopic = Option("hello", "")
            } else {
                val previousKeyword = topicStack.pop()
                toTopic = Option(previousKeyword!!, "")
            }
        } else {
            scene?.topic?.also { topicStack.push(it) }
        }

        toTopic.onPick?.invoke()

        var nextResponse = ""
        val nextOptions = mutableListOf<Option>()
        val sources = talker.conversationSources()
        var clearStack = false
        sources.forEach { source ->
            source.getConversationTopic(toTopic.topic)?.also { addScene ->
                nextResponse += (if (nextResponse.isBlank()) "" else "\n") + addScene.text
                nextOptions.addAll(addScene.options)
                clearStack = clearStack || addScene.clearStack
            }
        }
        sources.forEach { source ->
            source.optionsForText(nextResponse).forEach { addOption ->
                nextOptions.add(addOption)
            }
        }
        if (topicStack.isNotEmpty()) {
            nextOptions.add(Option("back", if (nextOptions.isEmpty())
                (if (clearStack) "Something else..." else "Anyway...") else "Never mind."))
        }
        if (clearStack) {
            topicStack.clear()
        }
        nextOptions.add(Option("bye", "Goodbye.") { endConversation() })

        wrappedText = wrapText("\"" + nextResponse + "\"", width - portraitXpad, padding, Screen.font)
        scene = Scene(newTopic.topic, nextResponse, nextOptions)
        adjustHeight()
        maxSelection = nextOptions.size - 1
        selection = 0
        bubbleProgress = 0f
        Speaker.world(talker.talkSound(App.player), source = talker.xy)
    }

    fun openTrade() {
        dismiss()
        Screen.addModal(TradeModal(talker))
    }

    fun endConversation() {
        dismiss()
    }

    override fun onKeyDown(key: Keydef) {
        super.onKeyDown(key)
        when (key) {
            Keydef.MOVE_N -> selectPrevious()
            Keydef.MOVE_S -> selectNext()
            Keydef.INTERACT -> doSelect()
            Keydef.SHORTCUT1 -> onShortcutSelect(0)
            Keydef.SHORTCUT2 -> onShortcutSelect(1)
            Keydef.SHORTCUT3 -> onShortcutSelect(2)
            Keydef.SHORTCUT4 -> onShortcutSelect(3)
            Keydef.SHORTCUT5 -> onShortcutSelect(4)
            Keydef.SHORTCUT6 -> onShortcutSelect(5)
            Keydef.SHORTCUT7 -> onShortcutSelect(6)
            Keydef.SHORTCUT8 -> onShortcutSelect(7)
            Keydef.SHORTCUT9 -> onShortcutSelect(8)
        }
    }

    private fun onShortcutSelect(newSelection: Int) {
        if (newSelection <= maxSelection) {
            KtxAsync.launch {
                changeSelection(newSelection)
                delay(100L)
                doSelect()
            }
        }
    }

    private fun selectNext() {
        changeSelection(if (selection >= maxSelection) if (maxSelection < 0) -1 else 0 else selection + 1)
    }

    private fun selectPrevious() {
        changeSelection(if (selection < 1) maxSelection else selection - 1)
    }

    private fun changeSelection(newSelection: Int) {
        if (newSelection != selection) {
            Speaker.ui(Speaker.SFX.UIMOVE, screenX = x)
        }
        selection = newSelection
    }

    override fun onMouseMovedTo(screenX: Int, screenY: Int) {
        changeSelection(mouseToOption(screenX, screenY) ?: -1)
    }

    override fun onMouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        if (super.onMouseClicked(screenX, screenY, button)) return true
        mouseToOption(screenX, screenY)?.also { changeSelection(it) ; doSelect(); return true }
        doSelect()
        return false
    }

    private fun mouseToOption(screenX: Int, screenY: Int): Int? {
        val localX = screenX - x
        val localY = screenY - y
        if (localX in 1 until width) {
            val hoverOption = (localY - optionStartY + 4) / optionSpacing
            if (hoverOption in 0..maxSelection) {
                return hoverOption
            }
        }
        return null
    }

    private fun doSelect() {
        Speaker.ui(Speaker.SFX.UISELECT, screenX = x)
        scene?.also { changeTopic(it.options[selection] )}
    }

    override fun drawText() {
        super.drawText()
        if (isAnimating()) return

        drawWrappedText(wrappedText, padding + portraitXpad, padding + headerPad, textSpacing, Screen.font)

        scene?.also { scene ->
            optionStartY = height - padding - (scene.options.size * optionSpacing) + 4
            var n = 0
            scene.options.forEach { option ->
                drawString(
                    option.question, padding, optionStartY + n * optionSpacing,
                    if (n == selection) Screen.fontColorBold else Screen.fontColorDull
                )
                n++
            }
        }
    }

    override fun drawBackground() {
        super.drawBackground()
        if (!isAnimating()) {
            val boxPadding = -2
            val boxY = optionStartY + optionSpacing * selection
            if (selection >= 0) {
                drawSelectionBox(
                    padding - boxPadding, boxY - boxPadding,
                    width - (padding * 2) + boxPadding * 2 - 4, optionSpacing + boxPadding * 2 - 4
                )
            }
        }
    }

    private fun drawPortrait() {
        val shadePad = 8
        portrait?.also { portrait ->
            portraitBatch.addPixelQuad(x + padding - shadePad, y + padding + headerPad - shadePad,
                x + padding + portraitSize + shadePad, y + padding + headerPad + portraitSize + shadePad, portraitBatch.getTextureIndex(
                    Glyph.PORTRAIT_SHADE))
            portraitBatch.addPixelQuad(x + padding, y + padding + headerPad,
                x + padding + portraitSize, y + padding + headerPad + portraitSize,
                portraitBatch.getTextureIndex(portrait), mirror = talker.mirrorGlyph)
            bubbleProgress?.also { progress ->
                val bubbleOffset = 16 - (progress * 20f).toInt()
                portraitBatch.addPixelQuad(x + padding + 40, y + padding + headerPad + bubbleOffset + 32,
                    x + padding + portraitSize + 8, y + padding + headerPad + portraitSize + bubbleOffset,
                    portraitBatch.getTextureIndex(Glyph.PORTRAIT_SPEECH_BUBBLE), alpha = 1f - progress)
            }
        }
    }
}

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

class ConverseModal(
    private val talker: NPC,
    position: Position = Position.LEFT,
): Modal(
    650, 400, talker.iname(),
    position = position,
) {
    val portrait = talker.portraitGlyph()
    val portraitBatch = QuadBatch(Screen.portraitTileSet, maxQuads = 100)

    override fun drawEverything() {
        super.drawEverything()
        portraitBatch.clear()
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        Gdx.gl.glEnable(GL20.GL_BLEND)
        drawPortrait()
        portraitBatch.draw()
    }

    interface Source {
        fun getConversationTopic(topic: String): Scene?
    }

    class Option(
        val topic: String,
        val question: String,
        val onPick: (()->Unit)? = null,
    ) {
        var wrappedQuestion = mutableListOf<String>()
    }

    class Scene(
        val topic: String,
        val text: String,
        val options: List<Option>
    )


    var scene: Scene? = null
    val topicStack = Stack<String>()

    var wrappedText = mutableListOf<String>()

    var selection = -1
    var maxSelection = 0

    val padding = 22
    var optionStartY = 0
    val optionSpacing = 32
    val headerPad = 50
    val portraitSize = 96
    val portraitXpad = if (portrait == null) 0 else 120

    init {
        zoomWhenOpen = 1.8f

        changeTopic(Option("hello", ""))
    }

    fun changeTopic(newTopic: Option) {
        var toTopic = newTopic
        if (newTopic.topic == "back") {
            val previousKeyword = topicStack.pop() ?: throw RuntimeException("went back in convo but nothing on stack!")
            toTopic = Option(previousKeyword, "")
        } else {
            scene?.topic?.also { topicStack.push(it) }
        }

        toTopic.onPick?.invoke()

        var nextResponse = ""
        val nextOptions = mutableListOf<Option>()
        talker.conversationSources().forEach { source ->
            source.getConversationTopic(toTopic.topic)?.also { addScene ->
                nextResponse += (if (nextResponse.isBlank()) "" else " ") + addScene.text
                nextOptions.addAll(addScene.options)
            }
        }
        if (topicStack.isNotEmpty()) {
            nextOptions.add(Option("back", if (nextOptions.isEmpty()) "Anyway..." else "Never mind."))
        } else if (talker.willTrade()) {
            nextOptions.add(Option("trade", talker.tradeMsg()) { openTrade() } )
        }
        nextOptions.add(Option("bye", "Goodbye.") { endConversation() })

        wrappedText = wrapText(nextResponse, width - portraitXpad, padding, Screen.font)
        scene = Scene(newTopic.topic, nextResponse, nextOptions)
        maxSelection = nextOptions.size - 1
        selection = 0

        log.info("text: ${scene?.text}")
        log.info("options: ${scene?.options}")
        log.info("portrait: ${talker.portraitGlyph()}")
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

        drawWrappedText(wrappedText, padding + portraitXpad, padding + headerPad, 24, Screen.font)

        scene?.also { scene ->
            optionStartY = height - padding - (scene.options.size * optionSpacing)
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
                x + padding + portraitSize, y + padding + headerPad + portraitSize, portraitBatch.getTextureIndex(portrait))
        }
    }
}

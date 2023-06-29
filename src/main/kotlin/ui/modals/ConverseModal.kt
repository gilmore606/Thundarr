package ui.modals

import actors.NPC
import audio.Speaker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ktx.async.KtxAsync
import render.Screen
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

    val padding = 22

    var scene: Scene? = null
    val topicStack = Stack<String>()

    var wrappedText = mutableListOf<String>()

    var selection = -1
    var maxSelection = 0
    var optionStartY = 0
    val optionSpacing = 32

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
        }
        nextOptions.add(Option("bye", "Goodbye.") { endConversation() })

        wrappedText = wrapText(nextResponse, width, padding, Screen.font)
        scene = Scene(newTopic.topic, nextResponse, nextOptions)
        maxSelection = nextOptions.size - 1
        selection = 0

        log.info("text: ${scene?.text}")
        log.info("options: ${scene?.options}")
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
            val hoverOption = (localY - optionStartY - 1) / optionSpacing
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

        drawWrappedText(wrappedText, padding, padding + 50, 24, Screen.font)

        scene?.also { scene ->
            optionStartY = height - padding - (scene.options.size * optionSpacing)
            var n = 0
            scene.options.forEach { option ->
                drawString(
                    option.question, padding, optionStartY + n * optionSpacing,
                    if (n == selection) Screen.fontColorBold else Screen.fontColor
                )
                n++
            }
        }
    }

    override fun drawBackground() {
        super.drawBackground()
        val boxPadding = -2
        val boxY = optionStartY + optionSpacing * selection
        if (!isAnimating() && selection >= 0) {
            drawSelectionBox(padding - boxPadding, boxY - boxPadding,
                width - (padding*2) + boxPadding*2 - 4, optionSpacing + boxPadding*2 - 4)
        }
    }
}

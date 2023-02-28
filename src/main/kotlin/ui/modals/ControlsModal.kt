package ui.modals

import com.badlogic.gdx.Input
import render.Screen
import ui.input.Keyboard
import ui.input.Keydef
import ui.input.KeydefSection
import com.badlogic.gdx.Input.Keys.*

class ControlsModal : Modal(530, 450, "- cONtRoLS -") {

    override fun newThingBatch() = null
    override fun newActorBatch() = null

    var selectionX = -1
    var selectionY = -1

    val header = 80
    val padding = 20
    val columnWidth = 245
    val lineSpacing = 24

    private val keyNames = mapOf(
        -1 to "???",
        A to "A",
        B to "B",
        C to "C",
        D to "D",
        E to "E",
        F to "F",
        G to "G",
        H to "H",
        I to "I",
        J to "J",
        K to "K",
        L to "L",
        M to "M",
        N to "N",
        O to "O",
        P to "P",
        Q to "Q",
        R to "R",
        S to "S",
        T to "T",
        U to "U",
        V to "V",
        W to "W",
        X to "X",
        Y to "Y",
        Z to "Z",
        NUM_0 to "0",
        NUM_1 to "1",
        NUM_2 to "2",
        NUM_3 to "3",
        NUM_4 to "4",
        NUM_5 to "5",
        NUM_6 to "6",
        NUM_7 to "7",
        NUM_8 to "8",
        NUM_9 to "9",
        APOSTROPHE to "'",
        BACKSLASH to "\\",
        COMMA to ",",
        DEL to "DEL",
        BACKSPACE to "<-",
        DPAD_DOWN to "down",
        DPAD_LEFT to "left",
        DPAD_RIGHT to "right",
        DPAD_UP to "up",
        ENTER to "enter",
        EQUALS to "=",
        GRAVE to "`",
        LEFT_BRACKET to "[",
        MINUS to "-",
        PERIOD to ".",
        RIGHT_BRACKET to "]",
        SEMICOLON to ";",
        SLASH to "/",
        SPACE to "space",
        TAB to "tab",
        ESCAPE to "esc",
        END to "end",
        HOME to "home",
        INSERT to "ins",
        PAGE_UP to "pgUp",
        PAGE_DOWN to "pgDown",
        NUMPAD_DIVIDE to "num /",
        NUMPAD_MULTIPLY to "num *",
        NUMPAD_SUBTRACT to "num -",
        NUMPAD_ADD to "num +",
        NUMPAD_DOT to "num .",
        NUMPAD_ENTER to "num enter",
    )

    class KeyItem(
        val header: String? = null,
        val key: Keydef? = null,
        var code: Int = -1
    )

    val items = Array (2) { ArrayList<KeyItem>() }.apply {
        addItems(this[0], KeydefSection.MOVE)
        addItems(this[0], KeydefSection.INTERACT)
        addItems(this[1], KeydefSection.AWARE)
        addItems(this[1], KeydefSection.UI)
    }

    private fun addItems(toList: ArrayList<KeyItem>, section: KeydefSection) {
        toList.add(KeyItem(header = section.title))
        for (keyDef in Keydef.values()) {
            if (keyDef.uiSection == section) toList.add(KeyItem(
                key = keyDef,
                code = Keyboard.codeForBind(keyDef)
            ))
        }
    }

    override fun drawModalText() {
        super.drawModalText()
        for (ix in 0..1) {
            var iy = 0
            for (item in items[ix]) {
                val x = padding + ix * columnWidth
                val y = header + iy * lineSpacing
                item.header?.also { header ->
                    drawString(header, x, y + 4, Screen.fontColorDull, Screen.smallFont)
                } ?: item.key?.also { key ->
                    val name = keyNames[item.code] ?: "???"
                    drawString(key.description, x, y, Screen.fontColor, Screen.font)
                    drawString(name, x + columnWidth - 55, y, Screen.fontColorBold, Screen.font)
                }
                iy++
            }
        }
    }

}

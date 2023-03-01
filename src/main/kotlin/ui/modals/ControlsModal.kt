package ui.modals

import render.Screen
import ui.input.Keyboard
import ui.input.Keydef
import ui.input.KeydefSection
import com.badlogic.gdx.Input.Keys.*
import ui.input.Mouse
import ui.panels.Console
import util.log

class ControlsModal : Modal(550, 485, "- cONtRoLS -") {

    override fun newThingBatch() = null
    override fun newActorBatch() = null

    var selectionX = -1
    var selectionY = -1

    val header = 80
    val padding = 20
    val columnWidth = 255
    val lineSpacing = 24

    var childModal: ControlsRebindModal? = null

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
        F1 to "F1",
        F2 to "F2",
        F3 to "F3",
        F4 to "F4",
        F5 to "F5",
        F6 to "F6",
        F7 to "F7",
        F8 to "F8",
        F9 to "F9",
        F10 to "F10",
        F11 to "F11",
        F12 to "F12",
    )

    class KeyItem(
        val header: String? = null,
        val key: Keydef? = null,
        var code: Int = -1
    ) {
        fun isKey() = header == null
        fun isHeader() = header != null
    }

    val items = Array (2) { ArrayList<KeyItem>() }.apply {
        addItems(this[0], KeydefSection.MOVE)
        addItems(this[0], KeydefSection.INTERACT)
        addItems(this[1], KeydefSection.AWARE)
        addItems(this[1], KeydefSection.UI)
    }

    fun codeToName(code: Int): String? = keyNames[code]

    private fun addItems(toList: ArrayList<KeyItem>, section: KeydefSection) {
        toList.add(KeyItem(header = section.title))
        for (keyDef in Keydef.values()) {
            if (keyDef.uiSection == section) toList.add(KeyItem(
                key = keyDef,
                code = Keyboard.codeForBind(keyDef)
            ))
        }
    }

    private fun selectedItem(): KeyItem? =
        if (selectionX >= 0 && selectionY >= 0 && selectionY < items[selectionX].size)
            items[selectionX][selectionY] else null

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
                    drawString(name, x + columnWidth - 62, y, Screen.fontColorBold, Screen.font)
                }
                iy++
            }
        }
        drawString("Restore defaults", padding, height - 40, Screen.fontColorDull, Screen.font)
    }

    override fun drawBackground() {
        super.drawBackground()
        if (!isAnimating()) {
            if (selectionX >= 0 && selectionY >= 0 && selectionY < items[selectionX].size) {
                selectedItem()?.also { selected ->
                    if (selected.isKey())
                        drawSelectionBox(
                            padding + selectionX * columnWidth, header + selectionY * lineSpacing + 4,
                            columnWidth - 10, 18
                        )
                }
            } else if (selectionX >=0 && selectionY == items[selectionX].size) {
                drawSelectionBox(
                    padding, height - 37, columnWidth - 10, 18
                )
            }
        }
    }

    override fun onKeyDown(key: Keydef) {
        when (key) {
            Keydef.MOVE_N -> selectPrevious()
            Keydef.MOVE_S -> selectNext()
            Keydef.MOVE_W, Keydef.MOVE_E -> selectNextColumn()
            Keydef.INTERACT -> select()
            Keydef.CANCEL -> dismiss()
        }
    }

    private fun selectPrevious() {
        selectionY--
        if (selectionX < 0) selectionX = 0
        if (selectionY < 0) selectionY = items[selectionX].size
        if (selectedItem()?.isHeader() == true) selectPrevious()
    }
    private fun selectNext() {
        selectionY++
        if (selectionX < 0) selectionX = 0
        if (selectionY > items[selectionX].size) selectionY = 0
        if (selectedItem()?.isHeader() == true) selectNext()
    }
    private fun selectNextColumn() {
        if (selectionY < 0) selectionY = 0
        selectionX = if (selectionX <= 0) 1 else 0
        if (selectedItem()?.isHeader() == true) selectNext()
    }

    override fun onMouseMovedTo(screenX: Int, screenY: Int) {
        val lx = screenX - x - padding
        val ly = screenY - y - header
        if (lx > 0 && ly > 0 && lx < (columnWidth * 2)) {
            val column = if (lx > columnWidth) 1 else 0
            val row = ly / lineSpacing
            if (row < items[column].size) {
                if (items[column][row].isKey()) {
                    selectionX = column
                    selectionY = row
                    return
                }
            }
        }
        if (lx in 1 until columnWidth && ly in (height - header - 48) until height - header - 8) {
            selectionX = 0
            selectionY = items[0].size
            return
        }
        selectionX = -1
        selectionY = -1
    }

    override fun onMouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        select()
        return true
    }

    private fun select() {
        if (selectionX > -1 && selectionY == items[selectionX].size) {
            selectDefaults()
        } else selectedItem()?.also { keyItem ->
            if (keyItem.isKey()) {
                childModal = ControlsRebindModal(
                    keyItem.key!!,
                    keyNames[keyItem.code] ?: "???",
                    this
                ).also { Screen.addModal(it) }
            }
        }
    }

    private fun selectDefaults() {
        Screen.addModal(ConfirmModal(listOf("Restore all bindings to the default keys?")) { yes ->
            if (yes) {
                Keyboard.loadDefaultBinds()
                for (column in items) {
                    for (item in column) {
                        if (item.isKey()) {
                            item.code = Keyboard.codeForBind(item.key!!)
                        }
                    }
                }
                Console.say("Restored all key bindings to default keys.")
                selectionX = 0
                selectionY = 1
            }
        })
    }

    fun abortChild() {
        childModal?.remoteClose()
        childModal = null
    }

    fun unbind(code: Int, def: Keydef) {
        items.forEach { column ->
            column.forEach { item ->
                if (item.code == code) {
                    item.code = -1
                    Console.say("Unbound key " + (keyNames[code] ?: "???") + " from " + def.description + ".")
                }
            }
        }
        Keyboard.unbind(code)
    }

    fun bind(code: Int, def: Keydef) {
        Keyboard.bind(code, def)
        items.forEach { column ->
            column.forEach { item ->
                if (item.key == def) item.code = code
            }
        }
        Console.say("Bound key " + keyNames[code] + " to " + def.description + ".")
        childModal?.remoteClose()
        childModal = null
    }

    override fun onDismiss() {
        App.savePrefs()
    }
}

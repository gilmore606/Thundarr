package ui.input

import App
import actors.Herder
import actors.Ox
import com.badlogic.gdx.Input
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ktx.app.KtxInputAdapter
import ktx.async.KtxAsync
import render.Screen
import things.Lightbulb
import things.PalmTree
import ui.panels.DebugPanel
import util.*

object Keyboard : KtxInputAdapter {

    var lastKey = -1
    var lastKeyTime = System.currentTimeMillis()

    private const val REPEAT_DELAY_MS = 300L
    private const val REPEAT_MS = 90L

    private var SHIFT = false
    private var CTRL = false
    private var ALT = false

    var CURSOR_MODE = false

    init {
        KtxAsync.launch {
            while (true) {
                delay(REPEAT_MS)
                if ((System.currentTimeMillis() > lastKeyTime + REPEAT_DELAY_MS) &&
                    (Screen.topModal == null) && lastKey >= 0) {
                    pressKey(lastKey)
                }
            }
        }
    }

    override fun keyDown(keycode: Int): Boolean {
        if (keycode == -1) return true
        Screen.releaseScrollLatch()
        Screen.topModal?.also { modal ->
            modal.keyDown(keycode)
        } ?: run {
            if (keycode == Input.Keys.ALT_LEFT || keycode == Input.Keys.ALT_RIGHT) {
                ALT = true
            } else if (keycode == Input.Keys.CONTROL_LEFT || keycode == Input.Keys.CONTROL_RIGHT) {
                CTRL = true
            } else if (keycode == Input.Keys.SHIFT_LEFT || keycode == Input.Keys.SHIFT_RIGHT) {
                SHIFT = true
            } else {
                lastKey = keycode
                lastKeyTime = System.currentTimeMillis()
                pressKey(keycode)
            }
        }
        return true
    }

    override fun keyUp(keycode: Int): Boolean {
        Screen.topModal?.also { modal ->
            modal.keyUp(keycode)
        } ?: run {
            if (keycode == Input.Keys.ALT_LEFT || keycode == Input.Keys.ALT_RIGHT) {
                ALT = false
            } else if (keycode == Input.Keys.CONTROL_LEFT || keycode == Input.Keys.CONTROL_RIGHT) {
                CTRL = false
            } else if (keycode == Input.Keys.SHIFT_LEFT || keycode == Input.Keys.SHIFT_RIGHT) {
                SHIFT = false
            }
        }
        lastKey = -1
        return true
    }

    private fun toggleCursorMode() {
        CURSOR_MODE = !CURSOR_MODE
        if (CURSOR_MODE) {
            Screen.moveCursor(NO_DIRECTION)
        } else {
            Screen.clearCursor()
        }
    }

    private fun pressKey(keycode: Int) {
        when (keycode) {
            Input.Keys.NUMPAD_8, Input.Keys.W -> { if (CURSOR_MODE) Screen.moveCursor(NORTH) else App.player.tryMove(NORTH) }
            Input.Keys.NUMPAD_7, Input.Keys.Q -> { if (CURSOR_MODE) Screen.moveCursor(NORTHWEST) else App.player.tryMove(NORTHWEST) }
            Input.Keys.NUMPAD_4, Input.Keys.A -> { if (CURSOR_MODE) Screen.moveCursor(WEST) else App.player.tryMove(WEST) }
            Input.Keys.NUMPAD_1, Input.Keys.Z -> { if (CURSOR_MODE) Screen.moveCursor(SOUTHWEST) else App.player.tryMove(SOUTHWEST) }
            Input.Keys.NUMPAD_2, Input.Keys.X -> { if (CURSOR_MODE) Screen.moveCursor(SOUTH) else App.player.tryMove(SOUTH) }
            Input.Keys.NUMPAD_3, Input.Keys.C -> { if (CURSOR_MODE) Screen.moveCursor(SOUTHEAST) else App.player.tryMove(SOUTHEAST) }
            Input.Keys.NUMPAD_6, Input.Keys.D -> { if (CURSOR_MODE) Screen.moveCursor(EAST) else App.player.tryMove(EAST) }
            Input.Keys.NUMPAD_9, Input.Keys.E -> { if (CURSOR_MODE) Screen.moveCursor(NORTHEAST) else App.player.tryMove(NORTHEAST) }
            Input.Keys.NUMPAD_5, Input.Keys.S -> { Screen.rightClickCursorTile() }

            Input.Keys.NUMPAD_DIVIDE -> { toggleCursorMode() }
            Input.Keys.PAGE_UP -> { CURSOR_MODE = true ; Screen.cursorNextActor(-1) }
            Input.Keys.PAGE_DOWN -> { CURSOR_MODE = true ; Screen.cursorNextActor(1) }

            Input.Keys.EQUALS -> { Screen.mouseScrolled(-1.43f) }
            Input.Keys.MINUS -> { Screen.mouseScrolled(1.43f) }

            Input.Keys.TAB -> { App.openInventory() }
            Input.Keys.ESCAPE -> { App.openSystemMenu() }
            Input.Keys.M -> { App.openMap() }
            Input.Keys.ENTER -> { lastKey = -1 ; if (ALT) Screen.toggleFullscreen() }

            Input.Keys.BACKSPACE -> { App.player.toggleAggro() }

            Input.Keys.F1 -> {
                App.DEBUG_VISIBLE = !App.DEBUG_VISIBLE
            }
            Input.Keys.F2 -> { Lightbulb().moveTo(groundAtPlayer()) }
            Input.Keys.F4 -> { PalmTree().moveTo(groundAtPlayer()) }
            Input.Keys.F5 -> {
                App.DEBUG_PANEL = !App.DEBUG_PANEL
                if (App.DEBUG_PANEL) {
                    Screen.addPanel(DebugPanel)
                } else {
                    Screen.removePanel(DebugPanel)
                }
            }
            Input.Keys.F6 -> {
                Herder().moveTo(App.level, App.player.xy.x + 1, App.player.xy.y)
            }
            Input.Keys.F12 -> { Screen.showSeenAreas = !Screen.showSeenAreas }

            else -> { lastKey = -1 }
        }
    }
}

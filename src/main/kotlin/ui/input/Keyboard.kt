package ui.input

import App
import actors.Herder
import actors.Wolfman
import actors.actions.Wait
import com.badlogic.gdx.Input
import com.badlogic.gdx.Input.Keys.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ktx.app.KtxInputAdapter
import ktx.async.KtxAsync
import render.Screen
import things.Lightbulb
import things.PalmTree
import ui.panels.DebugPanel
import ui.panels.Toolbar
import util.*

object Keyboard : KtxInputAdapter {

    var debugFloat = 0f
    val debugFloatStep = 0.05f

    var lastKey = -1
    var lastKeyTime = System.currentTimeMillis()

    private const val REPEAT_DELAY_MS = 300L
    const val REPEAT_MS = 90L

    var SHIFT = false
    var CTRL = false
    var ALT = false

    var modKeyDown = 0

    var CURSOR_MODE = false

    init {
        KtxAsync.launch {
            while (true) {
                delay(REPEAT_MS)
                if ((System.currentTimeMillis() > lastKeyTime + REPEAT_DELAY_MS) && lastKey >= 0) {
                    pressKey(lastKey)
                }
            }
        }
    }

    override fun keyDown(keycode: Int): Boolean {
        if (keycode == -1) return true
        Screen.releaseScrollLatch()
        if (keycode == ALT_LEFT || keycode == ALT_RIGHT) {
            ALT = true
            modKeyDown = keycode
        } else if (keycode == CONTROL_LEFT || keycode == CONTROL_RIGHT) {
            CTRL = true
            modKeyDown = keycode
            Screen.updateCursorLine()
        } else if (keycode == SHIFT_LEFT || keycode == SHIFT_RIGHT) {
            SHIFT = true
            modKeyDown = keycode
        } else {
            lastKey = keycode
            modKeyDown = 0
            lastKeyTime = System.currentTimeMillis()
            pressKey(keycode)
        }
        return true
    }

    override fun keyUp(keycode: Int): Boolean {
        var isMod = false
        if (keycode == ALT_LEFT || keycode == ALT_RIGHT) {
            ALT = false
            isMod = true
        } else if (keycode == CONTROL_LEFT || keycode == CONTROL_RIGHT) {
            CTRL = false
            isMod = true
            Screen.clearCursorLine()
        } else if (keycode == SHIFT_LEFT || keycode == SHIFT_RIGHT) {
            SHIFT = false
            isMod = true
        }
        lastKey = -1
        if (isMod && modKeyDown > 0) {
            pressKey(modKeyDown)
            modKeyDown = 0
        }
        Screen.topModal?.also { modal ->
            modal.keyUp(keycode)
        }
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

    private fun processDir(dir: XY) {
        if (CTRL) {
            Screen.moveCursor(dir)
            Screen.rightClickCursorTile()
        } else if (CURSOR_MODE) {
            Screen.moveCursor(dir)
        } else {
            App.player.tryMove(dir)
        }
    }

    private fun pressKey(keycode: Int) {
        when (keycode) {
            ENTER -> { lastKey = -1 ; if (ALT) Screen.toggleFullscreen() }
        }

        Screen.topModal?.also { modal ->
            modal.keyDown(keycode)
        } ?: run {
            when (keycode) {
                NUMPAD_8, W -> { processDir(NORTH) }
                NUMPAD_7, Q -> { processDir(NORTHWEST) }
                NUMPAD_4, A -> { processDir(WEST) }
                NUMPAD_1, Z -> { processDir(SOUTHWEST) }
                NUMPAD_2, X -> { processDir(SOUTH) }
                NUMPAD_3, C -> { processDir(SOUTHEAST) }
                NUMPAD_6, D -> { processDir(EAST) }
                NUMPAD_9, E -> { processDir(NORTHEAST) }
                NUMPAD_5, S -> { Screen.rightClickCursorTile() }

                SPACE -> { App.player.queue(Wait(1f)) }
                PERIOD -> { App.player.toggleSleep() }

                NUMPAD_DIVIDE -> { toggleCursorMode() }
                PAGE_UP -> { CURSOR_MODE = true ; Screen.cursorNextActor(-1) }
                PAGE_DOWN -> { CURSOR_MODE = true ; Screen.cursorNextActor(1) }

                EQUALS -> { Screen.mouseScrolled(-1.43f) }
                MINUS -> { Screen.mouseScrolled(1.43f) }

                Input.Keys.TAB -> { App.openInventory() }
                Input.Keys.BACKSLASH -> { App.openGear() }
                Input.Keys.BACKSPACE -> { App.openSkills() }
                Input.Keys.ESCAPE -> { App.openSystemMenu() }
                Input.Keys.M -> { App.openMap() }

                Input.Keys.SLASH -> { App.player.toggleAggro() }

                Input.Keys.NUM_1 -> { Toolbar.onKey(1) }
                Input.Keys.NUM_2 -> { Toolbar.onKey(2) }
                Input.Keys.NUM_3 -> { Toolbar.onKey(3) }
                Input.Keys.NUM_4 -> { Toolbar.onKey(4) }
                Input.Keys.NUM_5 -> { Toolbar.onKey(5) }
                Input.Keys.NUM_6 -> { Toolbar.onKey(6) }
                Input.Keys.NUM_7 -> { Toolbar.onKey(7) }
                Input.Keys.NUM_8 -> { Toolbar.onKey(8) }

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
                Input.Keys.F7 -> {
                    Wolfman().moveTo(App.level, App.player.xy.x + 1, App.player.xy.y)
                }
                Input.Keys.F12 -> { Screen.showSeenAreas = !Screen.showSeenAreas }

                Input.Keys.F9 -> { debugFloat += debugFloatStep }
                Input.Keys.F10 -> { debugFloat -= debugFloatStep }

                else -> { lastKey = -1 }
            }
        }
    }
}

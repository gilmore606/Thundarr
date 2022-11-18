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
import things.*
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

    val moveKeys = mutableMapOf<Int, XY>().apply {
        this[W] = NORTH
        this[E] = NORTHEAST
        this[D] = EAST
        this[C] = SOUTHEAST
        this[X] = SOUTH
        this[Z] = SOUTHWEST
        this[A] = WEST
        this[Q] = NORTHWEST
        this[S] = NO_DIRECTION

        this[NUMPAD_8] = NORTH
        this[NUMPAD_9] = NORTHEAST
        this[NUMPAD_6] = EAST
        this[NUMPAD_3] = SOUTHEAST
        this[NUMPAD_2] = SOUTH
        this[NUMPAD_1] = SOUTHWEST
        this[NUMPAD_4] = WEST
        this[NUMPAD_7] = NORTHWEST
        this[NUMPAD_5] = NO_DIRECTION
        this[NUMPAD_ENTER] = NO_DIRECTION

        this[DPAD_UP] = NORTH
        this[DPAD_RIGHT] = EAST
        this[DPAD_DOWN] = SOUTH
        this[DPAD_LEFT] = WEST
    }

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
            if (moveKeys.contains(keycode)) {

                val dir = moveKeys[keycode]
                if (dir == NO_DIRECTION) Screen.rightClickCursorTile()
                else dir?.also { processDir(it) }

            } else when (keycode) {

                SPACE -> { App.player.queue(Wait(1f)) }
                PERIOD -> { App.player.toggleSleep() }

                NUMPAD_DIVIDE -> { toggleCursorMode() }
                PAGE_UP -> { CURSOR_MODE = true ; Screen.cursorNextTarget(-1) }
                PAGE_DOWN -> { CURSOR_MODE = true ; Screen.cursorNextTarget(1) }

                EQUALS -> { Screen.mouseScrolled(-1.43f) }
                MINUS -> { Screen.mouseScrolled(1.43f) }

                TAB -> { App.openInventory() }
                BACKSLASH -> { App.openGear() }
                BACKSPACE -> { App.openSkills() }
                ESCAPE -> { App.openSystemMenu() }
                M -> { App.openMap() }

                SLASH -> { App.player.toggleAggro() }

                GRAVE -> { Toolbar.onKey(-1)}
                NUM_1 -> { Toolbar.onKey(1) }
                NUM_2 -> { Toolbar.onKey(2) }
                NUM_3 -> { Toolbar.onKey(3) }
                NUM_4 -> { Toolbar.onKey(4) }
                NUM_5 -> { Toolbar.onKey(5) }
                NUM_6 -> { Toolbar.onKey(6) }
                NUM_7 -> { Toolbar.onKey(7) }
                NUM_8 -> { Toolbar.onKey(8) }

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
                    Table().moveTo(App.level, App.player.xy.x + 1, App.player.xy.y)
                }
                Input.Keys.F8 -> {
                    ModernDoor().moveTo(App.level, App.player.xy.x + 1, App.player.xy.y)
                }
                F11 -> {
                    CeilingLight().withColor(Dice.float(0f,1f), Dice.float(0f,1f), Dice.float(0f,1f))
                        .moveTo(App.level, App.player.xy.x, App.player.xy.y)
                }
                Input.Keys.F12 -> { Screen.showSeenAreas = !Screen.showSeenAreas }

                Input.Keys.F9 -> { Screen.tiltAmount += debugFloatStep }
                Input.Keys.F10 -> { Screen.tiltAmount -= debugFloatStep }

                else -> { lastKey = -1 }
            }
        }
    }
}

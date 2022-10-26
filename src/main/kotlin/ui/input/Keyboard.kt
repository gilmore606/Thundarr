package ui.input

import App
import actors.Ox
import actors.actions.Move
import com.badlogic.gdx.Input
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ktx.app.KtxInputAdapter
import ktx.async.KtxAsync
import render.GameScreen
import things.Lightbulb
import things.PalmTree
import ui.modals.SystemMenu
import ui.modals.InventoryModal
import ui.modals.MapModal
import ui.panels.DebugPanel
import util.*

object Keyboard : KtxInputAdapter {

    var lastKey = -1
    var lastKeyTime = System.currentTimeMillis()

    private const val REPEAT_DELAY_MS = 300L
    private const val REPEAT_MS = 65L

    private var SHIFT = false
    private var CTRL = false
    private var ALT = false

    var CURSOR_MODE = false

    init {
        KtxAsync.launch {
            while (true) {
                delay(REPEAT_MS)
                if ((System.currentTimeMillis() > lastKeyTime + REPEAT_DELAY_MS) &&
                    (GameScreen.topModal == null) && lastKey >= 0) {
                    pressKey(lastKey)
                }
            }
        }
    }

    override fun keyDown(keycode: Int): Boolean {
        if (keycode == -1) return true
        GameScreen.scrollLatch = false
        GameScreen.topModal?.also { modal ->
            modal.keyDown(keycode)
        } ?: run {
            when  {
                keycode == Input.Keys.SHIFT_LEFT -> { SHIFT = true }
                keycode == Input.Keys.CONTROL_LEFT  -> { CTRL = true }
                keycode == Input.Keys.ALT_LEFT -> { ALT = true }
                else -> {
                    lastKey = keycode
                    lastKeyTime = System.currentTimeMillis()
                    pressKey(keycode)
                }
            }
        }
        return true
    }

    override fun keyUp(keycode: Int): Boolean {
        GameScreen.topModal?.also { modal ->
            modal.keyUp(keycode)
        } ?: run {
            when (keycode) {
                Input.Keys.SHIFT_LEFT or Input.Keys.SHIFT_RIGHT -> { SHIFT = false }
                Input.Keys.CONTROL_LEFT or Input.Keys.CONTROL_RIGHT -> { CTRL = false }
                Input.Keys.ALT_LEFT or Input.Keys.ALT_RIGHT -> { ALT = false }
            }
        }
        lastKey = -1
        return true
    }

    private fun toggleCursorMode() {
        CURSOR_MODE = !CURSOR_MODE
        if (CURSOR_MODE) {
            GameScreen.moveCursor(NO_DIRECTION)
        } else {
            GameScreen.clearCursor()
        }
    }

    private fun pressKey(keycode: Int) {
        when (keycode) {
            Input.Keys.NUMPAD_8, Input.Keys.W -> { if (CURSOR_MODE) GameScreen.moveCursor(NORTH) else App.player.tryMove(NORTH) }
            Input.Keys.NUMPAD_7, Input.Keys.Q -> { if (CURSOR_MODE) GameScreen.moveCursor(NORTHWEST) else App.player.tryMove(NORTHWEST) }
            Input.Keys.NUMPAD_4, Input.Keys.A -> { if (CURSOR_MODE) GameScreen.moveCursor(WEST) else App.player.tryMove(WEST) }
            Input.Keys.NUMPAD_1, Input.Keys.Z -> { if (CURSOR_MODE) GameScreen.moveCursor(SOUTHWEST) else App.player.tryMove(SOUTHWEST) }
            Input.Keys.NUMPAD_2, Input.Keys.X -> { if (CURSOR_MODE) GameScreen.moveCursor(SOUTH) else App.player.tryMove(SOUTH) }
            Input.Keys.NUMPAD_3, Input.Keys.C -> { if (CURSOR_MODE) GameScreen.moveCursor(SOUTHEAST) else App.player.tryMove(SOUTHEAST) }
            Input.Keys.NUMPAD_6, Input.Keys.D -> { if (CURSOR_MODE) GameScreen.moveCursor(EAST) else App.player.tryMove(EAST) }
            Input.Keys.NUMPAD_9, Input.Keys.E -> { if (CURSOR_MODE) GameScreen.moveCursor(NORTHEAST) else App.player.tryMove(NORTHEAST) }
            Input.Keys.NUMPAD_5, Input.Keys.S -> { GameScreen.rightClickCursorTile() }

            Input.Keys.NUMPAD_DIVIDE -> { toggleCursorMode() }
            Input.Keys.PAGE_UP -> { CURSOR_MODE = true ; GameScreen.cursorNextActor(-1) }
            Input.Keys.PAGE_DOWN -> { CURSOR_MODE = true ; GameScreen.cursorNextActor(1) }

            Input.Keys.EQUALS -> { GameScreen.mouseScrolled(-1.43f) }
            Input.Keys.MINUS -> { GameScreen.mouseScrolled(1.43f) }

            Input.Keys.TAB -> { App.openInventory() }
            Input.Keys.ESCAPE -> { App.openSystemMenu() }
            Input.Keys.M -> { App.openMap() }

            Input.Keys.F1 -> {
                App.DEBUG_VISIBLE = !App.DEBUG_VISIBLE
            }
            Input.Keys.F2 -> { Lightbulb().moveTo(groundAtPlayer()) }
            Input.Keys.F4 -> { PalmTree().moveTo(groundAtPlayer()) }
            Input.Keys.F5 -> {
                App.DEBUG_PANEL = !App.DEBUG_PANEL
                if (App.DEBUG_PANEL) {
                    GameScreen.addPanel(DebugPanel)
                } else {
                    GameScreen.removePanel(DebugPanel)
                }
            }
            Input.Keys.F6 -> {
                Ox().moveTo(App.level, App.player.xy.x + 1, App.player.xy.y)
            }

            else -> { lastKey = -1 }
        }
    }
}

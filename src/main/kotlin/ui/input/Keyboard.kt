package ui.input

import App
import actors.actions.Move
import com.badlogic.gdx.Input
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ktx.app.KtxInputAdapter
import ktx.async.KtxAsync
import render.GameScreen
import render.tilesets.Glyph
import things.Thing
import ui.modals.EscMenu
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
            Input.Keys.NUMPAD_8 -> { if (CURSOR_MODE) GameScreen.moveCursor(NORTH) else App.player.queue(Move(NORTH)) }
            Input.Keys.NUMPAD_7 -> { if (CURSOR_MODE) GameScreen.moveCursor(NORTHWEST) else App.player.queue(Move(NORTHWEST)) }
            Input.Keys.NUMPAD_4 -> { if (CURSOR_MODE) GameScreen.moveCursor(WEST) else App.player.queue(Move(WEST)) }
            Input.Keys.NUMPAD_1 -> { if (CURSOR_MODE) GameScreen.moveCursor(SOUTHWEST) else App.player.queue(Move(SOUTHWEST)) }
            Input.Keys.NUMPAD_2 -> { if (CURSOR_MODE) GameScreen.moveCursor(SOUTH) else App.player.queue(Move(SOUTH)) }
            Input.Keys.NUMPAD_3 -> { if (CURSOR_MODE) GameScreen.moveCursor(SOUTHEAST) else App.player.queue(Move(SOUTHEAST)) }
            Input.Keys.NUMPAD_6 -> { if (CURSOR_MODE) GameScreen.moveCursor(EAST) else App.player.queue(Move(EAST)) }
            Input.Keys.NUMPAD_9 -> { if (CURSOR_MODE) GameScreen.moveCursor(NORTHEAST) else App.player.queue(Move(NORTHEAST)) }
            Input.Keys.NUMPAD_5 -> { GameScreen.rightClickCursorTile() }
            Input.Keys.NUMPAD_DIVIDE -> { toggleCursorMode() }

            Input.Keys.EQUALS -> { GameScreen.mouseScrolled(-1.43f) }
            Input.Keys.MINUS -> { GameScreen.mouseScrolled(1.43f) }

            Input.Keys.TAB -> { GameScreen.addModal(InventoryModal()) }
            Input.Keys.ESCAPE -> { GameScreen.addModal(EscMenu()) }
            Input.Keys.M -> { GameScreen.addModal(MapModal()) }

            Input.Keys.F1 -> {
                App.DEBUG_VISIBLE = !App.DEBUG_VISIBLE
            }
            Input.Keys.F2 -> { App.level.addThingAt(App.player.xy.x, App.player.xy.y, Thing(
                    Glyph.LIGHTBULB, false, false
                ).apply { light = LightColor(1f, 1f, 0.4f) }
            )}
            Input.Keys.F3 -> {
                KtxAsync.launch {
                    val things: MutableList<Thing> = mutableListOf<Thing>().apply { addAll(App.level.thingsAt(App.player.xy.x, App.player.xy.y)) }
                    things.forEach {
                        App.level.removeThingAt(App.player.xy.x, App.player.xy.y, it)
                    }
                }
            }
            Input.Keys.F4 -> {
                App.level.addThingAt(App.player.xy.x, App.player.xy.y, Thing(
                    Glyph.TREE, true, false
                ))
            }
            Input.Keys.F5 -> {
                App.DEBUG_PANEL = !App.DEBUG_PANEL
                if (App.DEBUG_PANEL) {
                    GameScreen.addPanel(DebugPanel)
                } else {
                    GameScreen.removePanel(DebugPanel)
                }
            }
            else -> { lastKey = -1 }
        }
    }
}

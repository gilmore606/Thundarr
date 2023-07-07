package ui.input

import App
import actors.Herder
import actors.Jerif
import actors.MagicPortal
import actors.actions.Wait
import com.badlogic.gdx.Gdx
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
import ui.input.Keydef.*
import ui.panels.Console

object Keyboard : KtxInputAdapter {

    var binds = mutableMapOf<Int, Keydef>()

    var debugFloat = 0f
    val debugFloatStep = 0.05f

    var lastKey = -1
    var lastKeyTime = System.currentTimeMillis()

    private const val REPEAT_DELAY_MS = 300L
    const val REPEAT_MS = 90L

    var CTRL = false
    var ALT = false
    var lastShiftUp = System.currentTimeMillis()

    var modKeyDown = 0

    var CURSOR_MODE = false

    val moveKeys = mutableMapOf<Keydef, XY>().apply {
        this[MOVE_N] = NORTH
        this[MOVE_NE] = NORTHEAST
        this[MOVE_E] = EAST
        this[MOVE_SE] = SOUTHEAST
        this[MOVE_S] = SOUTH
        this[MOVE_SW] = SOUTHWEST
        this[MOVE_W] = WEST
        this[MOVE_NW] = NORTHWEST
        this[INTERACT] = NO_DIRECTION
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

        loadDefaultBinds()
    }

    fun loadDefaultBinds() {
        log.info("Installing default keybinds")
        binds.clear()
        Keydef.values().forEach { keydef ->
            keydef.defaultKey?.also { keycode ->
                binds[keycode] = keydef
            }
        }
    }

    fun loadBinds(newBinds: Map<Int, Keydef>) {
        binds.clear()
        newBinds.forEach { entry ->
            log.info("Loading keybind ${entry.key} for ${entry.value}")
            binds[entry.key] = entry.value
        }
    }

    fun codeForBind(forBind: Keydef): Int {
        for (bind in binds) {
            if (bind.value == forBind) return bind.key
        }
        return -1
    }

    fun unbind(code: Int) {
        binds.remove(code)
    }

    fun bind(code: Int, keyDef: Keydef) {
        binds.remove(code)
        binds[code] = keyDef
    }

    override fun keyDown(keycode: Int): Boolean {
        if (keycode == -1) return true
        Screen.releaseScrollLatch()
        Screen.topModal?.also { modal ->
            if (modal.receiveRawKeys()) {
                modal.onRawKeyDown(keycode)
                return true
            }
        }
        if (keycode == ALT_LEFT || keycode == ALT_RIGHT) {
            ALT = true
            modKeyDown = keycode
        } else if (keycode == CONTROL_LEFT || keycode == CONTROL_RIGHT) {
            CTRL = true
            modKeyDown = keycode
            Screen.updateCursorLine()
        } else if (keycode != SHIFT_RIGHT && keycode != SHIFT_LEFT) {
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
            lastShiftUp = System.currentTimeMillis()
        }
        lastKey = -1
        if (isMod && modKeyDown > 0) {
            pressKey(modKeyDown)
            modKeyDown = 0
        }
        Screen.topModal?.also { modal ->
            val key = keycodeToKeydef(keycode)
            key?.also { modal.keyUp(it) }
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

    private fun isShiftHeld() = Gdx.input.isKeyPressed(SHIFT_LEFT) || Gdx.input.isKeyPressed(SHIFT_RIGHT) || Gdx.input.isKeyPressed(
        META_SHIFT_ON) || (System.currentTimeMillis() - lastShiftUp < 50)

    private fun processDir(dir: XY) {
        if (CTRL) {
            Screen.moveCursor(dir)
            Screen.rightClickCursorTile()
        } else if (isShiftHeld()) {
            App.player.tryAutoMove(dir)
        } else if (CURSOR_MODE) {
            Screen.moveCursor(dir)
        } else {
            App.player.tryMove(dir)
        }
    }

    private fun pressKey(keycode: Int) {
        if (Console.inputActive) {
            Console.keycodeDown(keycode)
            return
        }

        when (keycode) {
            ENTER -> { lastKey = -1 ; if (ALT) Screen.toggleFullscreen() }
        }

        val key = keycodeToKeydef(keycode)

        Screen.topModal?.also { modal ->
            key?.also { modal.keyDown(it) }
        } ?: run {
            if (App.player.isActing()) {
                App.player.cancelAction()
            }
            if (moveKeys.contains(key)) {
                val dir = moveKeys[key]
                if (dir == NO_DIRECTION) Screen.rightClickCursorTile()
                else dir?.also { processDir(it) }
            } else when (key) {

                WAIT -> { App.player.queue(Wait(1f)) }
                SLEEP -> { App.player.toggleSleep() }
                AGGRO -> { App.player.toggleAggro() }

                CURSOR_TOGGLE -> { toggleCursorMode() }
                CURSOR_PREV -> { CURSOR_MODE = true ; Screen.cursorNextTarget(-1) }
                CURSOR_NEXT -> { CURSOR_MODE = true ; Screen.cursorNextTarget(1) }

                ZOOM_IN -> { Screen.mouseScrolled(-1.43f) }
                ZOOM_OUT -> { Screen.mouseScrolled(1.43f) }

                SEEN_TOGGLE -> { Screen.showSeenAreas = !Screen.showSeenAreas }
                RADAR_TOGGLE -> { Screen.showRadar = !Screen.showRadar }

                OPEN_INV -> { App.openInventory() }
                OPEN_GEAR -> { App.openGear() }
                OPEN_SKILLS -> { App.openSkills() }
                OPEN_MAP -> { App.openMap() }
                OPEN_JOURNAL -> { App.openJournal() }

                CANCEL -> { App.openSystemMenu() }
                TOOLBAR_SHOW -> { Toolbar.onKey(-1)}
                SHORTCUT1 -> { Toolbar.onKey(1) }
                SHORTCUT2 -> { Toolbar.onKey(2) }
                SHORTCUT3 -> { Toolbar.onKey(3) }
                SHORTCUT4 -> { Toolbar.onKey(4) }
                SHORTCUT5 -> { Toolbar.onKey(5) }
                SHORTCUT6 -> { Toolbar.onKey(6) }
                SHORTCUT7 -> { Toolbar.onKey(7) }
                SHORTCUT8 -> { Toolbar.onKey(8) }
                SHORTCUT9 -> { Toolbar.onKey(9) }

                DEBUG_CONSOLE -> { Console.openDebug() }

                else -> when (keycode) {
                    I -> { App.player.debugMove(NORTH) }
                    J -> { App.player.debugMove(WEST) }
                    K -> { App.player.debugMove(SOUTH) }
                    L -> { App.player.debugMove(EAST) }

                    F8 -> { MagicPortal().spawnAt(App.player.level!!, App.player.xy.x + 1, App.player.xy.y) }
                    F9 -> { App.DEBUG_VISIBLE = !App.DEBUG_VISIBLE }
                    F10 -> { Sunsword().moveTo(groundAtPlayer()) }
                    F11 -> {
                        App.DEBUG_PANEL = !App.DEBUG_PANEL
                        if (App.DEBUG_PANEL) {
                            Screen.addPanel(DebugPanel)
                        } else {
                            Screen.removePanel(DebugPanel)
                        }
                    }

                    F12 -> { App.openPerlinDebug() }

                    else -> { lastKey = -1 }
                }
            }
        }
    }

    private fun keycodeToKeydef(keycode: Int): Keydef? = binds[keycode] ?: when (keycode) {
        NUMPAD_8 -> MOVE_N
        NUMPAD_9 -> MOVE_NE
        NUMPAD_6 -> MOVE_E
        NUMPAD_3 -> MOVE_SE
        NUMPAD_2 -> MOVE_S
        NUMPAD_1 -> MOVE_SW
        NUMPAD_4 -> MOVE_W
        NUMPAD_7 -> MOVE_NW
        NUMPAD_5 -> INTERACT
        NUMPAD_ENTER -> INTERACT
        NUMPAD_DIVIDE -> CURSOR_TOGGLE
        ENTER -> INTERACT
        else -> null
    }
}

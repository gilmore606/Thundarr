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
import util.*

object Keyboard : KtxInputAdapter {

    var lastKey = -1
    var lastKeyTime = System.currentTimeMillis()

    private const val REPEAT_DELAY_MS = 400L
    private const val REPEAT_MS = 65L

    init {
        KtxAsync.launch {
            while (true) {
                delay(REPEAT_MS)
                if ((System.currentTimeMillis() > lastKeyTime + REPEAT_DELAY_MS) &&
                    (GameScreen.topModal == null)) {
                    pressKey(lastKey)
                }
            }
        }
    }

    override fun keyDown(keycode: Int): Boolean {
        GameScreen.topModal?.also { modal ->
            modal.keyDown(keycode)
        } ?: run {
            lastKey = keycode
            lastKeyTime = System.currentTimeMillis()
            pressKey(keycode)
        }
        return true
    }

    override fun keyUp(keycode: Int): Boolean {
        GameScreen.topModal?.also { modal ->
            modal.keyUp(keycode)
        }
        lastKey = -1
        return true
    }

    private fun pressKey(keycode: Int) {
        when (keycode) {
            Input.Keys.NUMPAD_8 -> { App.player.queue(Move(NORTH)) }
            Input.Keys.NUMPAD_7 -> { App.player.queue(Move(NORTHWEST)) }
            Input.Keys.NUMPAD_4 -> { App.player.queue(Move(WEST)) }
            Input.Keys.NUMPAD_1 -> { App.player.queue(Move(SOUTHWEST)) }
            Input.Keys.NUMPAD_2 -> { App.player.queue(Move(SOUTH)) }
            Input.Keys.NUMPAD_3 -> { App.player.queue(Move(SOUTHEAST)) }
            Input.Keys.NUMPAD_6 -> { App.player.queue(Move(EAST)) }
            Input.Keys.NUMPAD_9 -> { App.player.queue(Move(NORTHEAST)) }

            Input.Keys.ESCAPE -> { GameScreen.addModal(EscMenu()) }

            Input.Keys.F1 -> { App.DEBUG_VISIBLE = !App.DEBUG_VISIBLE }
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
        }
    }
}

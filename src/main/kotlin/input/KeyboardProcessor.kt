package input

import App
import actors.actions.Move
import com.badlogic.gdx.Input
import ktx.app.KtxInputAdapter
import util.*

object KeyboardProcessor : KtxInputAdapter {

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.NUMPAD_8 -> { App.player.queue(Move(NORTH)) }
            Input.Keys.NUMPAD_7 -> { App.player.queue(Move(NORTHWEST)) }
            Input.Keys.NUMPAD_4 -> { App.player.queue(Move(WEST)) }
            Input.Keys.NUMPAD_1 -> { App.player.queue(Move(SOUTHWEST)) }
            Input.Keys.NUMPAD_2 -> { App.player.queue(Move(SOUTH)) }
            Input.Keys.NUMPAD_3 -> { App.player.queue(Move(SOUTHEAST)) }
            Input.Keys.NUMPAD_6 -> { App.player.queue(Move(EAST)) }
            Input.Keys.NUMPAD_9 -> { App.player.queue(Move(NORTHEAST)) }

            else -> return super.keyDown(keycode)
        }
        return true
    }

}

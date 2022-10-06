package input

import com.badlogic.gdx.Input
import ktx.app.KtxInputAdapter
import render.GameScreen
import util.*

object KeyboardProcessor : KtxInputAdapter {

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.NUMPAD_8 -> { GameScreen.movePlayer(NORTH) }
            Input.Keys.NUMPAD_7 -> { GameScreen.movePlayer(NORTHWEST) }
            Input.Keys.NUMPAD_4 -> { GameScreen.movePlayer(WEST) }
            Input.Keys.NUMPAD_1 -> { GameScreen.movePlayer(SOUTHWEST) }
            Input.Keys.NUMPAD_2 -> { GameScreen.movePlayer(SOUTH) }
            Input.Keys.NUMPAD_3 -> { GameScreen.movePlayer(SOUTHEAST) }
            Input.Keys.NUMPAD_6 -> { GameScreen.movePlayer(EAST) }
            Input.Keys.NUMPAD_9 -> { GameScreen.movePlayer(NORTHEAST) }

            else -> return super.keyDown(keycode)
        }
        return true
    }

}

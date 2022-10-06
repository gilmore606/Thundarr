package input

import com.badlogic.gdx.Input
import ktx.app.KtxInputAdapter
import render.GameScreen
import util.EAST
import util.NORTH
import util.SOUTH
import util.WEST

object KeyboardProcessor : KtxInputAdapter {

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.W -> { GameScreen.movePlayer(NORTH) }
            Input.Keys.A -> { GameScreen.movePlayer(WEST) }
            Input.Keys.S -> { GameScreen.movePlayer(SOUTH) }
            Input.Keys.D -> { GameScreen.movePlayer(EAST) }

            else -> return super.keyDown(keycode)
        }
        return true
    }

}

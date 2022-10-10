package ui

import com.badlogic.gdx.scenes.scene2d.ui.Widget

// TODO: a pane for scrolling text updates
object Console : Widget() {

    fun say(text: String) {
        println(text)
    }
}

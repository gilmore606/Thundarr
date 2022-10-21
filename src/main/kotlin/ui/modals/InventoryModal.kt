package ui.modals

import com.badlogic.gdx.Input
import things.ThingHolder
import ui.input.Mouse
import util.aOrAn
import util.plural

class InventoryModal(
    private val thingHolder: ThingHolder
    ) : SelectionModal(400, 700, "- bACkPACk -", default = 0) {

    init {
        adjustHeight()
    }

    private fun adjustHeight() {
        height = headerPad + thingHolder.byKind().size * 20 + padding
        maxSelection = thingHolder.byKind().size - 1
    }

    override fun drawModalText() {
        var n = 0
        thingHolder.byKind().forEach {
            var text = ""
            text = if (it.value.size > 1) {
                it.value.size.toString() + " " + it.value.first().name().plural()
            } else {
                it.value.first().name().aOrAn()
            }
            drawOptionText(text, n)
            n++
        }
    }

    override fun doSelect() {

    }

    override fun keyDown(keycode: Int) {
        if (keycode == Input.Keys.TAB) {
            dismiss()
        } else {
            super.keyDown(keycode)
        }
    }
}

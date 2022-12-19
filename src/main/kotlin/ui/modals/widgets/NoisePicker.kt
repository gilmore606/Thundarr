package ui.modals.widgets

import render.Screen
import ui.input.Mouse
import ui.modals.ContextMenu
import ui.modals.WidgetModal
import ui.panels.Console
import world.gen.NoisePatches

class NoisePicker(
    valInit: String,
    modal: WidgetModal, x: Int, y: Int, width: Int, height: Int,
    val onChange: (String)->Unit
) : Widget(modal, x, y, width, height) {

    var patchName = valInit

    override fun drawText() {
        drawString(patchName, 0, 0, Screen.fontColorBold, Screen.subTitleFont)
        drawString("[ SAVE ]", 300, 0, Screen.fontColorBold, Screen.font)
    }

    override fun onMouseClicked(x: Int, y: Int, button: Mouse.Button) {
        if (x > 290) {
            save()
        } else {
            openPicker()
        }
    }

    private fun save() {
        NoisePatches.save()
        Console.say("Noise patches saved to noisepatches.json!")
    }

    private fun openPicker() {
        Screen.addModal(ContextMenu(
            modal.x + x, modal.y + y
        ).apply {
            zoomWhenOpen = 0.5f
            this.parentModal = modal as ContextMenu.ParentModal
            NoisePatches.patches.forEach { addOption(it.key) {
                patchName = it.key
                onChange(it.key)
            } }
        })
    }

}

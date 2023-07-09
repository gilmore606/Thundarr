package ui.modals

import things.recipes.Recipe
import ui.input.Mouse

class RecipeModal(
    val workbenchModal: WorkbenchModal,
    val recipe: Recipe
) : Modal(500, 500, recipe.name(), position = Position.LEFT) {

    init {
        zoomWhenOpen = 1.4f
    }

    override fun onMouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        dismiss()
        return true
    }
}

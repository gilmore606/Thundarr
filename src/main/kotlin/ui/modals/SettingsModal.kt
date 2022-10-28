package ui.modals

import com.badlogic.gdx.graphics.g2d.GlyphLayout
import render.Screen
import render.tilesets.Glyph
import ui.input.Mouse
import kotlin.math.max
import kotlin.math.min

class SettingsModal : Modal(300, 500, "- settings -") {

    val contentY = 130

    class Slider(
        val title: String,
        val y: Int,
        val valCurrent: Double,
        val valMin: Double,
        val valMax: Double,
        val onValueSet: (Double)->Unit
    ) {
        var value = (valCurrent - valMin) / (valMax - valMin)
        var dragging = false

        fun drawText(modal: SettingsModal) {
            modal.drawString(title, modal.padding, modal.contentY + y, color = if (dragging) Screen.fontColorBold else Screen.fontColor)
        }
        fun drawBackground(modal: SettingsModal) {
            modal.drawQuad(modal.padding, modal.contentY + y + 40, modal.width - modal.padding * 2, 4, Glyph.BOX_BORDER)
            val thumbx = ((modal.width - modal.padding * 2).toFloat() * value).toInt()
            modal.drawQuad(thumbx, modal.contentY + y + 26, 32, 32, Glyph.BUTTON_SYSTEM)
        }
        fun mouseClicked(modal: SettingsModal, modalX: Int, modalY: Int) {
            val thumbx = ((modal.width - modal.padding * 2).toFloat() * value).toInt()
            if (modalX >= thumbx && modalX <= thumbx + 32) {
                dragging = true
            }
        }
        fun mouseMovedTo(modal: SettingsModal, modalX: Int, modalY: Int) {
            if (dragging) {
                value = min(1.0, max(0.0, ((modalX - modal.padding).toFloat() / (modal.width - modal.padding * 2f)).toDouble()))
                val outValue = valMin + (valMax - valMin) * value
                onValueSet(outValue)
            }
        }
        fun mouseUp(modal: SettingsModal) { dragging = false }
    }

    class Multipick(
        val title: String,
        val y: Int,
        val options: List<String>
    ) {
        var selected = 0
        var hovered: Int? = null
        val widths = options.map { option -> GlyphLayout(Screen.font, option).width.toInt() + 20 }

        fun drawText(modal: SettingsModal) {
            modal.drawString(title, modal.padding, modal.contentY + y)
            var xused = 0
            options.forEachIndexed { n, option ->
                modal.drawString(option, modal.padding + xused + 10, modal.contentY + y + 30,
                    color = if (n == selected) Screen.fontColorBold else Screen.fontColorDull)
                xused += widths[n]
            }
        }
        fun drawBackground(modal: SettingsModal) {

        }
    }

    abstract class Section(
        val title: String
    ) {
        abstract fun drawText(modal: SettingsModal)
        abstract fun drawBackground(modal: SettingsModal)
        abstract fun mouseClicked(modal: SettingsModal, modalX: Int, modalY: Int)
        open fun mouseMovedTo(modal: SettingsModal, modalX: Int, modalY: Int) { }
        open fun mouseUp(modal: SettingsModal) { }
    }

    object Video : Section("video") {
        val sliderCameraSpring = Slider("Camera follow slack", 0, Screen.cameraSlack, 0.04, 0.7) { Screen.cameraSlack = it }
        val sliderWorldZoom = Slider("Overworld auto zoom-out", 85, Screen.worldZoom, 1.0, 1.4) { Screen.worldZoom = it }
        val sliderMenuShift = Slider("Camera shift on menu open", 170, Screen.cameraMenuShift, 0.01, 0.9) { Screen.cameraMenuShift = it}
        val sliders = listOf(sliderCameraSpring, sliderWorldZoom, sliderMenuShift)

        val menuPos = Multipick("Dialog window position", 270, listOf("Left", "Center", "Top"))
        override fun drawText(modal: SettingsModal) {
            sliders.forEach { it.drawText(modal) }
            menuPos.drawText(modal)
        }

        override fun drawBackground(modal: SettingsModal) {
            sliders.forEach { it.drawBackground(modal) }
            menuPos.drawBackground(modal)
        }
        override fun mouseClicked(modal: SettingsModal, modalX: Int, modalY: Int) {
            sliders.forEachIndexed { n, slider ->
                if (modalY > slider.y + modal.contentY + 10  && modalY < slider.y + 75 + modal.contentY ) {
                    slider.mouseClicked(modal, modalX, modalY)
                }
            }
        }
        override fun mouseMovedTo(modal: SettingsModal, modalX: Int, modalY: Int) {
            sliders.forEach { slider ->
                if (modalY > slider.y + modal.contentY + 10 && modalY < slider.y + 75 + modal.contentY) {
                    slider.mouseMovedTo(modal, modalX, modalY)
                }
            }
        }
        override fun mouseUp(modal: SettingsModal) {
            sliders.forEach { it.mouseUp(modal) }
        }
    }

    object Audio : Section("audio") {
        override fun drawText(modal: SettingsModal) {

        }
        override fun drawBackground(modal: SettingsModal) { }
        override fun mouseClicked(modal: SettingsModal, screenX: Int, screenY: Int) { }
    }

    object Game : Section("game") {
        override fun drawText(modal: SettingsModal) {

        }
        override fun drawBackground(modal: SettingsModal) { }
        override fun mouseClicked(modal: SettingsModal, screenX: Int, screenY: Int) { }
    }

    private val padding = 24

    private val sections = listOf(Video, Audio, Game)

    private val spacePerSectionTitle = (width - padding * 2) / sections.size

    private var selectedSection = 0
    private var hoveredSection: Int? = null

    override fun mouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        if (!(screenX in x until x+width && screenY in y until y+height)) {
            dismiss()
            return true
        }
        hoveredSection?.also { hovered ->
            selectedSection = hovered
        } ?: run {
            sections[selectedSection].mouseClicked(this, screenX - this.x, screenY - this.y)
        }
        return true
    }

    override fun mouseMovedTo(screenX: Int, screenY: Int) {
        if (!(screenX in x until x+width && screenY in y until y+height)) {
            return
        }
        val localX = screenX - x
        val localY = screenY - y
        var newSection: Int? = null
        if (localX in 1 until width) {
            if (localY in 50 until 90) {
                newSection = min(sections.lastIndex, (localX / spacePerSectionTitle))
            }
        }
        hoveredSection = newSection
        if (localY >= contentY) {
            sections[selectedSection].mouseMovedTo(this, localX, localY)
        }
    }

    override fun mouseUp(screenX: Int, screenY: Int, button: Mouse.Button) {
        sections[selectedSection].mouseUp(this)
    }

    override fun keyDown(keycode: Int) {
        super.keyDown(keycode)
        dismiss()
    }

    override fun drawModalText() {
        super.drawModalText()
        sections.forEachIndexed { n, section ->
            drawString(section.title, padding + spacePerSectionTitle * n, 70,
                font = Screen.subTitleFont,
                color = if (selectedSection == n) Screen.fontColorBold else Screen.fontColorDull)
            if (n == selectedSection) section.drawText(this)
        }
    }

    override fun drawBackground() {
        super.drawBackground()
        if (isAnimating()) return
        hoveredSection?.also { hovered ->
            if (hovered != selectedSection) {
                drawSelectionBox(padding + hovered * spacePerSectionTitle, 72, spacePerSectionTitle - padding, 20)
            }
        }
        sections[selectedSection].drawBackground(this)
    }

}
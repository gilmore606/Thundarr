package ui.modals

import actors.actors.Actor
import render.Screen
import things.Thing
import things.MeleeWeapon
import ui.input.Keydef
import ui.input.Mouse
import util.wrapText
import world.Entity

class ExamineModal(
    val entity: Entity,
    position: Position = Position.LEFT
) : Modal(400, 300 + (entity.examineStats().size + 3) * statSpacing, entity.name(), position) {

    companion object {
        const val padding = 22
        const val statSpacing = 24
        const val statX = 40
    }

    class StatLine(
        val name: String = "",
        val value: Float? = null,
        val suffix: String = "",
        val valueString: String? = null,
        val showPlus: Boolean = false,
        val compare: Float? = null,
        val isSpacer: Boolean = false,
        val lowerBetter: Boolean = false
    )

    private var statY = 0

    private val wrappedDesc = wrapText(entity.examineDescription(), width - 64, padding, Screen.font)
    private val wrappedInfo = wrapText(entity.examineInfo(), width, padding, Screen.font)
    private val examineStats = entity.examineStats()

    override fun onMouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        if (super.onMouseClicked(screenX, screenY, button)) return true
        dismiss()
        return true
    }

    override fun onKeyDown(key: Keydef) {
        super.onKeyDown(key)
        dismiss()
    }

    override fun drawText() {
        super.drawText()
        if (isAnimating()) return
        drawWrappedText(wrappedDesc, padding, padding + 60, 24, Screen.font)
        drawWrappedText(wrappedInfo, padding, padding + 80 + 24 * wrappedDesc.size, 24, Screen.font)

        if (entity is Actor) {
            val effects = entity.statuses
            if (effects.isNotEmpty()) {
                var m = ""
                effects.forEachIndexed { n, status ->
                    m += status.name()
                    if (n < effects.lastIndex) { m += ", " }
                }
                val ty = padding + 100 + 24 * (wrappedDesc.size + wrappedInfo.size)
                drawString("Affected by: ", padding, ty)
                drawString(m, padding + 120, ty, Screen.fontColorBold)
            }
        }

        statY = 240
        if (entity is Thing) {
            if (entity.isPortable()) drawStat("weight", "lb", entity.getWeight(), statX)

            examineStats.forEach { statLine ->
                if (statLine.isSpacer) {
                    statY += statSpacing / 2
                } else {
                    statLine.valueString?.also {
                        drawStat(statLine.name, it, statX)
                    } ?: statLine.value?.also {
                        drawStat(statLine.name, statLine.suffix, it, statX, showPlus = statLine.showPlus)
                    }
                }
            }

            if (App.player.autoPickUpTypes.contains(entity.tag)) {
                drawStatFact("You'll pick up any " + entity.tag.pluralName + " you see.", padding)
            }
            if (App.player.thrownTag == entity.tag) {
                drawStatFact(entity.tag.pluralName + " are your preferred thrown weapon.", padding)
            }
        }
    }

    private fun drawStat(statName: String, suffix: String, value: Float, atx0: Int, showPlus: Boolean = false) {
        drawString(
            "$statName:", atx0 + (90 - measure(statName, Screen.smallFont) - 8), padding + statY,
            font = Screen.smallFont, color = Screen.fontColorDull
        )
        val valuestr = if (value != 0f) ((if (showPlus && value > 0f) "+" else "") + String.format("%.1f", value)) else "-"
        drawString(valuestr, atx0 + 100, padding + statY, font = Screen.font, color = Screen.fontColorBold)
        drawString(suffix, atx0 + 104 + measure(valuestr), padding + statY + 1, font = Screen.smallFont, color = Screen.fontColorDull)
        statY += statSpacing
    }

    private fun drawStat(statName: String, valuestr: String, atx0: Int) {
        drawString(
            "$statName:", atx0 + (90 - measure(statName, Screen.smallFont) - 8), padding + statY,
            font = Screen.smallFont, color = Screen.fontColorDull
        )
        drawString(valuestr, atx0 + 100, padding + statY, font = Screen.font, color = Screen.fontColorBold)
        statY += statSpacing
    }

    private fun drawStatFact(fact: String, x0: Int) {
        statY += statSpacing
        drawString(fact, x0, padding + statY, font = Screen.smallFont, color = Screen.fontColor)
        statY += statSpacing
    }

    override fun drawEntities() {
        if (isAnimating()) return
        val x0 = x + width - padding - 64
        val y0 = y + padding
        val batch = if (entity is Thing) myThingBatch() else myActorBatch()
        batch?.addPixelQuad(x0, y0, x0 + 64, y0 + 64,
            batch.getTextureIndex(entity.glyph(), entity.level(), entity.xy().x, entity.xy().y),
            hue = entity.hue())
    }
}

package ui.modals

import actors.actors.Actor
import render.Screen
import things.Clothing
import things.Thing
import things.MeleeWeapon
import ui.input.Keydef
import ui.input.Mouse
import util.wrapText
import world.Entity

class ExamineModal(
    val entity: Entity,
    position: Position = Position.LEFT
) : Modal(400, 440, entity.name(), position) {

    private val padding = 22
    private val statSpacing = 24
    private var statY = 0

    private val wrappedDesc = wrapText(entity.examineDescription(), width - 64, padding, Screen.font)
    private val wrappedInfo = wrapText(entity.examineInfo(), width, padding, Screen.font)


    override fun onMouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
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
            if (entity.isPortable()) drawStat("weight:", "lb", entity.weight(), padding)
            if (entity is MeleeWeapon) {
                drawStat("speed:", "", entity.speed(), padding)
                drawStat("accuracy:", "", entity.accuracy(), padding)
                drawStat("damage:", "", entity.damage(), padding)
            }
            if (entity is Clothing) {
                drawStat("armor:", "", entity.armor(), padding)
            }
            if (App.player.autoPickUpTypes.contains(entity.tag)) {
                drawStatFact("You'll pick up any " + entity.tag.pluralName + " you see.", padding)
            }
            if (App.player.thrownTag == entity.tag) {
                drawStatFact(entity.tag.pluralName + " are your preferred thrown weapon.", padding)
            }
        }
    }

    private fun drawStat(statName: String, suffix: String, value: Float, atx0: Int) {
        drawString(
            statName, atx0 + (80 - measure(statName, Screen.smallFont) - 8), padding + statY,
            font = Screen.smallFont, color = Screen.fontColorDull
        )
        val valuestr = String.format("%.1f", value)
        drawString(valuestr, atx0 + 80, padding + statY, font = Screen.font, color = Screen.fontColorBold)
        drawString(suffix, atx0 + 80 + measure(valuestr), padding + statY, font = Screen.font, color = Screen.fontColor)
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

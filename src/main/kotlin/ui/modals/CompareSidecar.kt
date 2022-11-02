package ui.modals

import render.Screen
import things.Clothing
import things.Gear

class CompareSidecar(private val parentModal: GearModal) : Modal(500, 350) {

    var gear1: Gear? = null
    var gear2: Gear? = null
    private var gear1desc = ArrayList<String>()
    private var gear2desc = ArrayList<String>()

    private val widthPerGear = 250
    private val padding = 18
    private val statSpacing = 24
    private var statY = 0

    init {
        adjustSize()
    }

    override fun myXmargin() = parentModal.let { (it.width + xMargin + 20) }

    fun showGear1(gear: Gear?) {
        gear1 = gear
        gear2 = null
        gear1?.also {
            gear1desc = wrapText(it.examineDescription(), widthPerGear - 60, padding, Screen.smallFont)
        }
        adjustSize()
    }

    fun showGear2(gear: Gear?) {
        gear2 = gear
        gear2?.also {
            gear2desc = wrapText(it.examineDescription(), widthPerGear - 60, padding, Screen.smallFont)
        }
        adjustSize()
    }

    fun adjustSize() {
        if (gear1 == null && gear2 == null) {
            width = 0
        } else if (gear2 != null) {
            width = widthPerGear * 2
        } else {
            width = widthPerGear
        }
    }

    override fun drawModalText() {
        super.drawModalText()
        gear1?.also { drawExamText(it, 0, gear1desc) }
        gear2?.also { drawExamText(it, widthPerGear, gear2desc, gear1) }
    }

    private fun drawExamText(gear: Gear, x0: Int, desc: List<String>, compareTo: Gear? = null) {
        drawString(gear.name(), x0 + padding, padding, font = Screen.subTitleFont)
        drawWrappedText(desc, x0 + padding, padding + 35, 20, Screen.smallFont)
        beginStats()
        drawStat("weight:", "lb", gear.weight(), x0, (compareTo?.weight() ?: gear.weight()) - gear.weight())

        if (gear is Clothing) {
            val armor = gear.armor()
            val comp = armor - (if (compareTo is Clothing) compareTo.armor() else armor)
            drawStat("armor:", "", armor, x0, comp)
        }
    }

    private fun beginStats() { statY = 190 }

    private fun drawStat(statName: String, suffix: String, value: Float, x0: Int, comparison: Float) {
        drawString(statName, x0 + (120 - measure(statName, Screen.smallFont) - 8), padding + statY, font = Screen.smallFont, color = Screen.fontColorDull)
        val valuestr = value.toString()
        drawString(valuestr, x0 + 120, padding + statY, font = Screen.font, color = Screen.fontColorBold)
        drawString(suffix, x0 + 120 + measure(valuestr), padding + statY, font = Screen.font, color = Screen.fontColor)
        if (comparison != 0f) {
            val symbol = (if (comparison > 0f) "+" else "") + String.format("%.1f", comparison)
            val color = if (comparison > 0f) Screen.fontColorGreen else Screen.fontColorRed
            drawString(symbol, x0 + 190, padding + statY, font = Screen.font, color = color)
        }
        statY += statSpacing
    }

    override fun drawEntities() {
        if (isAnimating()) return
        gear1?.also { drawExamEntity(it, 0) }
        gear2?.also { drawExamEntity(it, widthPerGear) }
    }

    override fun drawBackground() {
        if (!isAnimating() && width > 0) {
            super.drawBackground()
        }
    }

    private fun drawExamEntity(gear: Gear, x0: Int) {
        val ex = x0 + x + widthPerGear - padding - 64 + 8
        val ey = y + padding
        myThingBatch().addPixelQuad(ex, ey, ex + 64, ey + 64, myThingBatch().getTextureIndex(gear.glyph()), hue = gear.hue())
    }

}

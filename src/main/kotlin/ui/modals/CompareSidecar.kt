package ui.modals

import render.Screen
import things.Clothing
import things.Gear
import things.MeleeWeapon
import util.wrapText
import java.lang.Math.max

class CompareSidecar(private val parentModal: GearModal) : Modal(500, 580) {

    companion object {
        const val listWidth = 200
        const val widthPerGear = 250
        const val padding = 18
        const val statSpacing = 24
    }

    var gear1: Gear? = null
    var gear2: Gear? = null
    var gear1stats: List<ExamineModal.StatLine>? = null
    var gear2stats: List<ExamineModal.StatLine>? = null
    var slot: Gear.Slot? = null

    private var gear1desc = ArrayList<String>()
    private var gear2desc = ArrayList<String>()
    private var slotList = ArrayList<String>()

    private var statY = 0

    init {
        adjustSize()
    }

    override fun myXmargin() = parentModal.let { (it.width + xMargin + 20) }

    fun showGear1(gear: Gear?) {
        gear1 = gear
        gear1stats = gear?.examineStats()
        gear2 = null
        gear2stats = null
        gear1?.also {
            gear1desc = wrapText(it.examineDescription(), widthPerGear - 60, padding, Screen.smallFont)
        }
        adjustSize()
    }

    fun showGear2(gear: Gear?) {
        gear2 = gear
        gear2stats = gear?.examineStats(gear1)
        gear2?.also {
            gear2desc = wrapText(it.examineDescription(), widthPerGear - 60, padding, Screen.smallFont)
        }
        adjustSize()
    }

    fun showList(newSlot: Gear.Slot?) {
        slot = newSlot
        slotList.clear()
        slot?.also { slot ->
            App.player.contents.filter { it is Gear && it.slot == slot }.forEach { gear ->
                slotList.add(gear.name())
            }
        }
        adjustSize()
    }

    private fun adjustSize() {
        if (gear1 == null && gear2 == null) {
            if (slotList.isNotEmpty()) width = listWidth else width = 0
        } else if (gear2 != null) {
            width = widthPerGear * 2
        } else {
            width = widthPerGear
        }
    }

    override fun drawModalText() {
        super.drawModalText()
        gear1?.also { drawExamText(it, 0, gear1desc, gear1stats) }
        gear2?.also { drawExamText(it, widthPerGear, gear2desc, gear2stats, gear1) }
        if (gear1 == null && gear2 == null && slotList.isNotEmpty()) {
            slot?.also { slot ->
                drawString("${slot.title} gear", padding, padding + 10, font = Screen.subTitleFont)
                slotList.forEachIndexed { n, item ->
                    drawString(item, padding + 16, padding + (n * 20) + 50, font = Screen.smallFont)
                }
            }
        }
    }

    private fun drawExamText(gear: Gear, x0: Int, desc: List<String>, stats: List<ExamineModal.StatLine>? = null, compareTo: Gear? = null) {
        drawString(gear.name(), x0 + padding, padding, font = Screen.subTitleFont)
        drawWrappedText(desc, x0 + padding, padding + 35, 20, Screen.smallFont)
        beginStats()
        drawStat("weight", "lb", gear.getWeight(), x0, compareTo?.let { gear.getWeight() - it.getWeight() }, lowerBetter = true)

        stats?.forEach { statLine ->
            if (statLine.isSpacer) {
                statY += statSpacing / 2
            } else {
                statLine.valueString?.also { string ->
                    drawStat(statLine.name, string, x0)
                } ?: statLine.value?.also { value ->
                    drawStat(statLine.name, statLine.suffix, value, x0, statLine.compare?.let { value - it },
                        showPlus = statLine.showPlus, lowerBetter = statLine.lowerBetter)
                }
            }
        }
    }

    private fun beginStats() { statY = 190 }

    private fun drawStat(statName: String, suffix: String, value: Float, x0: Int, comparison: Float? = null,
                         showPlus: Boolean = false, lowerBetter: Boolean = false) {
        val fullName = "${statName}:"
        drawString(fullName, x0 + (120 - measure(fullName, Screen.smallFont) - 8), padding + statY, font = Screen.smallFont, color = Screen.fontColorDull)
        val valuestr = if (value != 0f) ((if (showPlus && value > 0f) "+" else "") + String.format("%.1f", value)) else "-"
        drawString(valuestr, x0 + 120, padding + statY, font = Screen.font, color = Screen.fontColorBold)
        drawString(suffix, x0 + 124 + measure(valuestr), padding + statY + 1, font = Screen.smallFont, color = Screen.fontColorDull)
        comparison?.also { comparison ->
            if (comparison != 0f) {
                val txt = if (comparison.toInt().toFloat() == comparison) comparison.toInt().toString() else String.format("%.1f", comparison)
                val symbol = (if (comparison > 0f) "+" else "") + txt
                val color = if ((comparison > 0f && !lowerBetter) || (comparison < 0f && lowerBetter)) Screen.fontColorGreen else Screen.fontColorRed
                drawString(symbol, x0 + 190, padding + statY, font = Screen.font, color = color)
            }
        }
        statY += statSpacing
    }

    private fun drawStat(statName: String, valuestr: String, x0: Int) {
        val fullName = "${statName}:"
        drawString(fullName, x0 + (120 - measure(fullName, Screen.smallFont) - 8), padding + statY, font = Screen.smallFont, color = Screen.fontColorDull)
        drawString(valuestr, x0 + 120, padding + statY, font = Screen.font, color = Screen.fontColorBold)
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
        myThingBatch()?.addPixelQuad(ex, ey, ex + 64, ey + 64, myThingBatch()?.getTextureIndex(gear.glyph()) ?: 0, hue = gear.hue())
    }

}

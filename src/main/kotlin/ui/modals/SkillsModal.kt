package ui.modals

import actors.Actor
import actors.stats.*
import audio.Speaker
import com.badlogic.gdx.Input
import render.Screen
import ui.input.Keyboard
import ui.input.Keydef
import ui.input.Mouse
import util.*
import java.lang.Integer.max

class SkillsModal(val actor: Actor) : Modal(370, 500, "- ${actor.name()} -") {

    val header = 80
    val padding = 18
    val statSpacing = 32
    val skillSpacing = 24

    val stats = allStats
    val statBonuses = stats.map { it.statBonuses(actor) }
    val statBonusStrings = statBonuses.map {
        if (it != 0f) (if (it >= 0f) "+" else "") + it.toInt().toString() else ""
    }
    val skills = actor.knownSkills().sortedBy { it.name }
    val skillBonuses = skills.map { it.statBonuses(actor) }
    val skillBonusStrings = skillBonuses.map {
        if (it != 0f) (if (it >= 0f) "+" else "") + it.toInt().toString() else ""
    }

    var selection = -1
    val maxSelection = stats.size + skills.size - 1

    init {
        sidecar = SkillSidecar(this)
        adjustHeight()
    }

    private fun adjustHeight() {
        this.height = header + padding * 2 + max(5, skills.size) * skillSpacing + 10
        onResize(Screen.width, Screen.height)
    }

    override fun drawModalText() {
        super.drawModalText()
        stats.forEachIndexed { n, stat ->
            val total = stat.get(actor)
            val bonus = statBonuses[n]
            val bonusString = statBonusStrings[n]
            drawRightText(stat.name + ":", 100, header + statSpacing * n)
            drawString((total - bonus).toInt().toString(), 110, header + statSpacing * n, Screen.fontColorBold, Screen.subTitleFont)
            drawString(bonusString, 140, header + statSpacing * n,
                if (bonusString.startsWith('+')) Screen.fontColorGreen else Screen.fontColorRed, Screen.font)
        }

        skills.forEachIndexed { n, skill ->
            val total = skill.get(actor)
            val bonus = skillBonuses[n]
            val bonusString = skillBonusStrings[n]
            val base = skill.getBase(actor).toInt()
            val baseString = if (base < 1) "" else base.toString()
            drawRightText(skill.name, 280, header + skillSpacing * n, if (base < 1) Screen.fontColorDull else Screen.fontColor)
            drawString(baseString, 290, header + skillSpacing * n + 1, Screen.fontColorDull, Screen.smallFont)
            drawString((total - bonus).toInt().toString(), 310, header + skillSpacing * n, Screen.fontColorBold)
            drawString(bonusString, 332, header + skillSpacing * n,
                if (bonusString.startsWith('+')) Screen.fontColorGreen else Screen.fontColorRed, Screen.font)
        }
    }

    override fun drawBackground() {
        super.drawBackground()
        if (!isAnimating() && selection >= 0) {
            if (selection < stats.size) {
                drawSelectionBox(padding, header + statSpacing * selection + 2, 140, 18)
            } else {
                drawSelectionBox(210, header + skillSpacing * (selection - stats.size) + 6, 140, 15)
            }
        }
    }

    override fun onMouseMovedTo(screenX: Int, screenY: Int) {
        val lx = screenX - x
        val ly = screenY - y
        var newSelection = -1
        if (ly > header && ly < height && lx > padding && lx < width - padding) {
            if (lx < 180) {
                val stati = (ly - header + 12) / statSpacing
                if (stati in stats.indices) {
                    newSelection = stati
                }
            } else {
                val skilli = (ly - header + 1) / skillSpacing
                if (skilli in skills.indices) {
                    newSelection = skilli + stats.size
                }
            }
        }
        changeSelection(newSelection)
    }

    private fun changeSelection(newSelection: Int) {
        if (newSelection == selection) return
        Speaker.ui(Speaker.SFX.UIMOVE)
        selection = newSelection
        if (selection < 0) {
            showSkill(null)
            return
        }
        if (selection < stats.size) showSkill(stats[selection])
        else showSkill(skills[selection - stats.size])
    }

    private fun showSkill(skill: Stat?) {
        (sidecar as SkillSidecar).showSkill(skill)
    }

    override fun onMouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        dismiss()
        return true
    }

    override fun onKeyDown(key: Keydef) {
        when (key) {
            Keydef.MOVE_N -> selectPrevious()
            Keydef.MOVE_S -> selectNext()
            Keydef.MOVE_W, Keydef.MOVE_E -> switchSide()
            Keydef.CANCEL, Keydef.OPEN_SKILLS -> dismiss()
        }
    }

    private fun selectNext() {
        var ns = selection
        if (ns <= stats.size - 1) {
            ns++
            if (ns > stats.size - 1) ns = 0
        } else {
            ns++
            if (ns > maxSelection) ns = stats.size
        }
        changeSelection(ns)
    }

    private fun selectPrevious() {
        var ns = selection
        if (ns <= stats.size - 1) {
            ns--
            if (ns < 0) ns = stats.size - 1
        } else {
            ns--
            if (ns < stats.size) ns = maxSelection
        }
        changeSelection(ns)
    }

    private fun switchSide() {
        var ns = selection
        if (ns <= stats.size - 1) {
            ns += stats.size
        } else {
            ns = max(0, ns - skills.size)
        }
        changeSelection(ns)
    }
}

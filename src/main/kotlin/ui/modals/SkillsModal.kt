package ui.modals

import actors.Actor
import actors.stats.*
import render.Screen
import ui.input.Mouse
import util.log

class SkillsModal(val actor: Actor) : Modal(370, 400, "- ${actor.name()} -") {

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

    init {
        sidecar = SkillSidecar(this)
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

    override fun onMouseMovedTo(screenX: Int, screenY: Int) {
        val lx = screenX - x
        val ly = screenY - y
        if (ly > header && ly < height && lx > padding && lx < width - padding) {
            if (lx < 180) {
                val stati = (ly - header) / statSpacing
                if (stati in stats.indices) {
                    (sidecar as SkillSidecar).showSkill(stats[stati])
                    return
                }
            } else {
                val skilli = (ly - header) / skillSpacing
                if (skilli in skills.indices) {
                    (sidecar as SkillSidecar).showSkill(skills[skilli])
                    return
                }
            }
        }
        (sidecar as SkillSidecar).showSkill(null)
    }

    override fun onMouseClicked(screenX: Int, screenY: Int, button: Mouse.Button): Boolean {
        dismiss()
        return true
    }

    override fun onKeyDown(keycode: Int) {
        super.onKeyDown(keycode)
        dismiss()
    }
}

package ui.modals

import actors.Actor
import actors.stats.*
import render.Screen
import ui.input.Mouse

class SkillsModal(val actor: Actor) : Modal(370, 400, "- ${actor.name()} -") {

    val header = 80
    val padding = 18
    val statSpacing = 32
    val skillSpacing = 24

    val stats = allStats
    val skills = actor.knownSkills().sortedBy { it.name }

    init {
        sidecar = SkillSidecar(this)
    }

    override fun drawModalText() {
        super.drawModalText()
        stats.forEachIndexed { n, stat ->
            drawRightText(stat.name + ":", 100, header + statSpacing * n)
            drawString(stat.get(actor).toInt().toString(), 110, header + statSpacing * n, Screen.fontColorBold, Screen.subTitleFont)
        }

        skills.forEachIndexed { n, skill ->
            drawRightText(skill.name + ":", 280, header + skillSpacing * n)
            drawString(skill.getBase(actor).toInt().toString(), 290, header + skillSpacing * n + 1, Screen.fontColorDull, Screen.smallFont)
            drawString(skill.get(actor).toInt().toString(), 310, header + skillSpacing * n, Screen.fontColorBold)
        }
    }

    override fun onMouseMovedTo(screenX: Int, screenY: Int) {
        val lx = screenX - x
        val ly = screenY - y
        if (ly > header && ly < height && lx > padding && lx < width - padding) {
            if (lx < 140) {
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

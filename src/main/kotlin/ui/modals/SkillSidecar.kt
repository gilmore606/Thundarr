package ui.modals

import actors.stats.Stat
import actors.stats.skills.Skill
import actors.statuses.Status
import render.Screen
import util.wrapText

class SkillSidecar(private val parentModal: SkillsModal) : Modal(0, 400) {

    var skill: Stat? = null
    private var skillDesc = ArrayList<String>()
    private var affectorStrings = ArrayList<String>()

    private val padding = 18
    private val fullWidth = 200

    override fun myXmargin() = parentModal.let { (it.width + xMargin + 20) }

    fun showSkill(skill: Stat?) {
        this.skill = skill
        skillDesc.clear()
        affectorStrings.clear()
        skill?.also {
            skillDesc = wrapText(it.description(), width, padding, Screen.smallFont)
            parentModal.actor.statEffectors(skill).forEach {
                affectorStrings.add(it.name() + if (it is Status) " status" else "")
            }
        }
        adjustSize()
    }

    fun adjustSize() {
        width = skill?.let { fullWidth } ?: 0
    }

    override fun drawModalText() {
        super.drawModalText()
        skill?.also { skill ->
            drawString(skill.name, padding, padding, font = Screen.subTitleFont)
            drawWrappedText(skillDesc, padding, padding + 35, 20, Screen.smallFont)
            if (skill is Skill) {  // i know, i know
                drawString("Depends on:", padding, padding + 150)
                skill.dependsOn.forEachIndexed { n, dep ->
                    drawString(dep.name, padding + 12, padding + 175 + n * 20, Screen.fontColorDull, Screen.smallFont)
                }
            }
            if (affectorStrings.isNotEmpty()) {
                drawString("Affected by:", padding, padding + 245)
                affectorStrings.forEachIndexed { n, aff ->
                    drawString(aff, padding + 12, padding + 270 + n * 20, Screen.fontColorDull, Screen.smallFont)
                }
            }
            val ip = skill.getImprovement(App.player)
            if (ip > 0f) {
                drawString("Progress:", padding, padding + 325)
            }
        }
    }

    override fun drawBackground() {
        if (!isAnimating() && width > 0) {
            super.drawBackground()
            val ip = skill?.getImprovement(App.player) ?: 0f
            if (ip > 0f) {
                boxBatch.addHealthBar(x + padding, y + padding + 350, x + width - padding * 2, y + padding + 350 + 12,
                    ip.toInt(), 100, true)
            }
        }
    }

}

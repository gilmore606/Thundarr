package ui.modals

import actors.stats.Stat
import actors.stats.skills.Skill
import render.Screen

class SkillSidecar(private val parentModal: SkillsModal) : Modal(200, 400) {

    var skill: Stat? = null
    private var skillDesc = ArrayList<String>()

    private val padding = 18
    private val fullWidth = 200

    override fun myXmargin() = parentModal.let { (it.width + xMargin + 20) }

    fun showSkill(skill: Stat?) {
        this.skill = skill
        skill?.also { skillDesc = wrapText(it.description(), width, padding, Screen.smallFont) }
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
                drawString("Depends on:", padding, padding + 130)
                skill.dependsOn.forEachIndexed { n, dep ->
                    drawString(dep.name, padding + 12, padding + 160 + n * 20, Screen.fontColorDull, Screen.smallFont)
                }
            }
        }
    }

    override fun drawBackground() {
        if (!isAnimating() && width > 0) {
            super.drawBackground()
        }
    }

}

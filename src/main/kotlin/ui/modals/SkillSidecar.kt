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
    private val fullWidth = 250

    override fun myXmargin() = parentModal.let { (it.width + xMargin + 20) }

    fun showSkill(skill: Stat?) {
        this.skill = skill
        adjustSize()
        skillDesc.clear()
        affectorStrings.clear()
        skill?.also {
            skillDesc = wrapText(it.description(), width, padding, Screen.smallFont)
            parentModal.actor.statEffectors(skill).forEach {
                affectorStrings.add(it.name() + if (it is Status) " status" else "")
            }
        }
    }

    fun adjustSize() {
        width = skill?.let { fullWidth } ?: 0
    }

    override fun drawModalText() {
        super.drawModalText()
        var yc = padding + 35
        skill?.also { skill ->
            drawString(skill.name, padding, padding, font = Screen.subTitleFont)
            drawWrappedText(skillDesc, padding, yc, 20, Screen.smallFont)
            yc += skillDesc.size * 20 + 10
            if (skill is Skill) {  // i know, i know
                drawString("Depends on:", padding, yc)
                yc += 22
                skill.dependsOn.forEach { dep ->
                    drawString(dep.name, padding + 12, yc, Screen.fontColorDull, Screen.smallFont)
                    yc += 20
                }
                yc += 14
            }
            if (affectorStrings.isNotEmpty()) {
                drawString("Affected by:", padding, yc)
                yc += 22
                affectorStrings.forEach { aff ->
                    drawString(aff, padding + 12, yc, Screen.fontColorDull, Screen.smallFont)
                    yc += 20
                }
                yc += 14
            }
            skill.examineSpecialStat()?.also { specialName ->
                drawString(specialName + ":", padding, yc)
                yc += 22
                skill.examineSpecialStatValue(App.player)?.also { value ->
                    drawString(value, padding + 12, yc, Screen.fontColorDull, Screen.smallFont)
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

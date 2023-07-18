package ui.modals

import actors.actors.Actor
import actors.actors.Player
import actors.stats.*
import actors.stats.skills.Skill
import audio.Speaker
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import render.Screen
import render.batches.QuadBatch
import render.tilesets.Glyph
import ui.input.Keydef
import ui.input.Mouse
import java.lang.Integer.max

class SkillsModal(val actor: Actor) : Modal(400, 550, "- ${actor.name()} -") {

    val portraitBatch = QuadBatch(Screen.portraitTileSet, maxQuads = 100)

    val header = 180
    val padding = 24
    val statSpacing = 32
    val skillSpacing = 24
    val minHeight = 400
    val skillX = 275

    val stats = allStats
    lateinit var statBonuses: List<Float>
    lateinit var statBonusStrings: List<String>
    lateinit var skills: List<Skill>
    lateinit var skillBonuses: List<Float>
    lateinit var skillBonusStrings: List<String>

    var xpNeed: Int = 0
    var xpHave: Int = 0

    var xpString = ""

    var xpOffset = 0

    var selection = -1
    var maxSelection = 0

    init {
        regenerate()
        sidecar = SkillSidecar(this)
    }

    private fun regenerate() {
        statBonuses = stats.map { it.statBonuses(actor) }
        statBonusStrings = statBonuses.map {
            if (it != 0f) (if (it >= 0f) "+" else "") + it.toInt().toString() else ""
        }
        skills = Skill.all().sortedBy { it.name }
        skillBonuses = skills.map { it.statBonuses(actor) }
        skillBonusStrings = skillBonuses.map {
            if (it != 0f) (if (it >= 0f) "+" else "") + it.toInt().toString() else ""
        }
        xpNeed = if (actor is Player) actor.xpNeededForLevel() else 0
        xpHave = if (actor is Player) actor.xpEarnedForLevel() else 0
        xpString = if (actor is Player) {
            if (actor.levelUpsAvailable > 0) "level up!" else
                "${xpNeed - xpHave}xp to lvl ${actor.xpLevel + 1}"
        } else ""
        xpOffset = measure(xpString, Screen.smallFont)
        maxSelection = stats.size + skills.size - 1

        adjustHeight()
    }

    private fun adjustHeight() {
        this.height = max(minHeight, header + padding + max(5, skills.size) * skillSpacing)
        onResize(Screen.width, Screen.height)
    }

    override fun drawEverything() {
        super.drawEverything()
        portraitBatch.clear()
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        Gdx.gl.glEnable(GL20.GL_BLEND)
        if (!isAnimating()) drawPortrait()
        portraitBatch.draw()
    }

    private fun drawPortrait() {
        val shadePad = 8
        val portraitSize = 64
        val px0 = x + width - portraitSize - padding
        val px1 = px0 + portraitSize
        val py0 = y + padding
        val py1 = py0 + portraitSize
        portraitBatch.addPixelQuad(px0 - shadePad, py0 - shadePad, px1 + shadePad, py1 + shadePad,
            portraitBatch.getTextureIndex(Glyph.PORTRAIT_SHADE))
        portraitBatch.addPixelQuad(px0, py0, px1, py1, portraitBatch.getTextureIndex(Glyph.PORTRAIT_THUNDARR))
    }

    override fun drawModalText() {
        super.drawModalText()

        drawSubTitle("the  ${Player.levels[App.player.xpLevel-1].name}")

        drawString("lvl ${actor.xpLevel}", padding, header - 65, Screen.fontColorDull, Screen.smallFont)
        if (actor is Player) {
            if (actor.xpLevel < Player.levels.lastIndex) {
                drawRightText(xpString, width - padding, header - 65, Screen.fontColorDull, Screen.smallFont)
            }
            if (actor.skillPoints > 0) {
                drawRightText("${actor.skillPoints} point${if (actor.skillPoints > 1) "s" else ""} to spend!",
                    width - padding, header - 32, Screen.fontColorBlue, Screen.smallFont)
            }
        }

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
            drawRightText(skill.name, skillX, header + skillSpacing * n, if (base < 1) Screen.fontColorDull else Screen.fontColorBold)
            drawString(baseString, skillX+15, header + skillSpacing * n + 1, Screen.fontColorDull, Screen.smallFont)
            drawString((total - bonus).toInt().toString(), skillX+35, header + skillSpacing * n,
                if (base < 1) Screen.fontColorDull else Screen.fontColorBold)
            drawString(bonusString, skillX+55, header + skillSpacing * n,
                if (bonusString.startsWith('+')) Screen.fontColorGreen else Screen.fontColorRed, Screen.font)
        }
    }

    override fun drawBackground() {
        super.drawBackground()
        if (isAnimating()) return

        if (actor is Player && actor.xpLevel < Player.levels.lastIndex) {
            val barx0 = x + padding + 55
            val bary0 = y + header - 67
            val barx1 = x + width - padding - xpOffset - 16
            val bary1 = y + header - 51
            boxBatch.addHealthBar(barx0, bary0, barx1, bary1,
                if (actor.levelUpsAvailable > 0) xpNeed else xpHave, xpNeed,
                allBlue = true, hideFull = false, withBorder = true)
        }

        if (selection >= 0) {
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
        if (isOutside(screenX, screenY)) dismiss() else doSelect()
        return true
    }

    private fun doSelect() {
        if (selection < 0) return
        if (actor !is Player) return
        if (actor.skillPoints < 1) return
        val skilli = selection - stats.size
        if (skilli in skills.indices) {
            val base = skills[skilli].getBase(actor).toInt()
            val newbase = if (base < 0) 1 else base+1
            Screen.addModal(ContextMenu(
                x + skillX + 60, (skilli * skillSpacing) + y + header
            ).apply {
                addOption("improve " + skills[skilli].name + " to $newbase base")
                    { improveSkill(skills[skilli]) }
            })
        }
    }

    private fun improveSkill(skill: Skill) {
        (actor as Player).skillPoints--
        skill.improve(actor, fullLevel = true)
        regenerate()
    }

    override fun onKeyDown(key: Keydef) {
        when (key) {
            Keydef.MOVE_N -> selectPrevious()
            Keydef.MOVE_S -> selectNext()
            Keydef.INTERACT -> doSelect()
            Keydef.MOVE_W, Keydef.MOVE_E -> switchSide()
            Keydef.CANCEL, Keydef.OPEN_SKILLS -> dismiss()
            Keydef.OPEN_INV -> replaceWith(ThingsModal(App.player))
            Keydef.OPEN_GEAR -> replaceWith(GearModal(App.player))
            Keydef.OPEN_JOURNAL -> replaceWith(JournalModal())
            Keydef.OPEN_MAP -> replaceWith(MapModal())
            else -> { }
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

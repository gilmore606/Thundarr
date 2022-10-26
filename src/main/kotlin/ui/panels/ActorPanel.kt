package ui.panels

import actors.Actor
import render.Screen
import render.tilesets.Glyph
import world.ChunkLoader

object ActorPanel : ShadedPanel() {

    private const val padding = 12
    private const val spacing = 44

    private var lastTime = -1.0
    private var lastCheckMs = System.currentTimeMillis()
    private const val checkInterval = 250L
    private var actors =  ArrayList<Actor>()

    init {
        this.width = RIGHT_PANEL_WIDTH
        this.height = 250
    }

    override fun onResize(width: Int, height: Int) {
        super.onResize(width, height)
        x = width - (this.width) - xMargin
        y = yMargin + (if (Screen.panels.contains(StatusPanel)) (StatusPanel.height + padding * 2) else 0) +
                if (Screen.panels.contains(LookPanel)) (LookPanel.height + padding * 2) else 0

    }

    override fun drawBackground() {
        if (actors.isNotEmpty()) super.drawBackground()
        if ((lastTime < App.time || lastCheckMs + checkInterval < System.currentTimeMillis()) && !ChunkLoader.isWorking()) {
            lastTime = App.time
            lastCheckMs = System.currentTimeMillis()
            updateActors()
        }
    }

    override fun drawText() {
        actors.forEachIndexed { n, actor ->
            drawString(actor.iname(), padding + 38, padding + spacing * n, font = Screen.smallFont,
                color = if (actor == LookPanel.entity) Screen.fontColorBold else Screen.fontColorDull)
        }
    }

    override fun drawEntities() {
        actors.forEachIndexed { n, actor ->
            val x0 = x + padding - 1
            val y0 = y + padding + spacing * n + 1
            actor.uiBatch().addPixelQuad(x0, y0, x0 + 32, y0 + 32,
                actor.uiBatch().getTextureIndex(actor.glyph(), actor.level(), actor.xy().x, actor.xy().y))
            if (actor == LookPanel.entity) {
                Screen.uiBatch.addPixelQuad(x0 + 36, y0 + 16, x0 + width - padding * 2 - 34, y0 + 36,
                    Screen.uiBatch.getTextureIndex(Glyph.CURSOR))
            }
            Screen.uiBatch.addHealthBar(x0 + 38, y0 + 20,
                x0 + width - padding * 2 - 38, y0 + 32, actor.hp, actor.hpMax)
        }
    }

    private fun updateActors() {
        App.player.level?.visibleNPCs()?.also {
            actors = it
        } ?: run {
            if (actors.isNotEmpty()) actors = ArrayList()
        }
        this.height = actors.size * spacing + padding * 2
    }

    fun actorAfter(actor: Actor, dir: Int): Actor? {
        if (actors.isEmpty()) return null
        val i = actors.indexOf(actor)
        if (i < 0) return null
        return if (dir == 1) {
            if (i == actors.lastIndex) actors.first() else actors[i+1]
        } else {
            if (i == 0) actors.last() else actors[i-1]
        }
    }

    fun firstActor(): Actor? = if (actors.isNotEmpty()) actors.first() else null
}

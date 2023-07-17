package ui.panels

import App
import render.Screen
import ui.input.Keyboard
import world.gen.Metamap
import world.persist.LevelKeeper
import path.Pather
import world.terrains.Terrain

object DebugPanel : ShadedPanel() {

    private val padding = 12

    init {
        this.width = 250
        this.height = 250
    }

    override fun onResize(width: Int, height: Int) {
        super.onResize(width, height)
        x = xMargin
        y = yMargin + EnvPanel.height + yMargin
    }

    override fun drawText() {
        drawString("x: ${App.player.xy.x} y: ${App.player.xy.y}", padding, padding)
        drawString(App.level.debugText(), padding, padding + 20)

        val l = App.level.lightAt(App.player.xy.x, App.player.xy.y)
        val lr = (l.r * 100).toInt()
        val lg = (l.g * 100).toInt()
        val lb = (l.b * 100).toInt()
        drawString("light $lr $lg $lb", padding, padding + 40)
        drawString("render ${Screen.drawTime} ms", padding, padding + 60)
        drawString("action ${Screen.actTime} ms", padding, padding + 80)
        drawString("${LevelKeeper.liveLevels.size} live levels", padding, padding + 100)

        val meta = Metamap.metaAtWorld(App.player.xy.x, App.player.xy.y)
        drawString("threat ${App.player.threatLevel()}", padding, padding + 120)
        drawString("temperature ${meta.temperature}", padding, padding + 140)

        val terrain = Terrain.get(App.level.getTerrain(App.player.xy.x, App.player.xy.y))
        val tdata = App.level.getTerrainData(App.player.xy.x, App.player.xy.y)
        val datatext = terrain.debugData(tdata)
        drawString(terrain.type.toString(), padding, padding + 160)
        drawString("  $datatext", padding, padding + 180)
        Screen.cursorPosition?.also {
            App.player.level?.lightAt(it.x, it.y)?.also { light ->
                drawString("light: ${light.r} ${light.g} ${light.b} B: ${light.brightness()}", padding, padding + 200)
            }
            App.player.level?.roofedAt(it.x, it.y)?.also { roofed ->
                App.player.level?.lightsAt(it.x, it.y)?.also { lights ->
                    drawString("roofed: $roofed  lights: ${lights.size}", padding, padding + 220)
                }
            }
        }
    }

}

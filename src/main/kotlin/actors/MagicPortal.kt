package actors

import actors.states.IdleDoNothing
import kotlinx.serialization.Serializable
import render.Screen
import render.sparks.GooGore
import render.sparks.Smoke
import render.tilesets.Glyph
import ui.panels.Console
import util.LightColor

@Serializable
class MagicPortal : NPC() {
    companion object {
        private val lightColor = LightColor(0.5f, 0.15f, 0.5f)
        private val flipInterval = 0.06f
        private val lifespan = 3f
    }
    override fun name() = "portal"
    override fun glyph() = Glyph.MAGIC_PORTAL
    override fun shadowWidth() = 0f
    override fun description() = "A swirling portal of eldritch origin."
    override fun idleState() = IdleDoNothing()
    override fun corpse() = null
    override fun bloodstain() = null
    override fun gore() = GooGore()

    override fun light() = lightColor
    override fun animOffsetY() = Screen.sinBob * 0.2f

    override fun onConverse(actor: Actor): Boolean {
        Console.say("The portal sizzles and resists your touch.  There's no way back.")
        return true
    }

    var flipClock = 0f
    override fun doOnRender(delta: Float) {
        super.doOnRender(delta)
        flipClock += delta
        if (flipClock > flipInterval) {
            flipClock = 0f
            mirrorGlyph = !mirrorGlyph
        }
    }

    var lifeLeft = lifespan
    override fun advanceTime(delta: Float) {
        super.advanceTime(delta)
        lifeLeft -= delta
        if (lifeLeft < 0f) {
            banishSelf()
        }
    }

    private fun banishSelf() {
        level?.addSpark(Smoke().at(xy.x, xy.y))
        App.weather.flashLightning(lightColor)
        Console.say("The portal vanishes with a crack of energy dispersal, leaving a puff of green vapor.")
        die()
    }
}

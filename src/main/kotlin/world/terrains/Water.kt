package world.terrains

import actors.Actor
import audio.Speaker
import render.sparks.Splash
import render.tilesets.Glyph
import util.*
import world.level.Level


sealed class Water(
    type: Type,
    glyph: Glyph
) : Terrain(type, glyph, false, true, false, false, dataType = Type.GENERIC_WATER){

    override fun name() = "water"
    override fun stepSound(actor: Actor) = Speaker.SFX.STEPWATER
    override fun stepSpark(actor: Actor, dir: XY) = Splash(dir)
    override fun trailsOverwrite() = false

    open fun surfGlyph() = Glyph.SURF

    override fun renderExtraQuads(level: Level, x: Int, y: Int, vis: Float, glyph: Glyph, light: LightColor,
                                  doQuad: (x0: Double, y0: Double, x1: Double, y1: Double, tx0: Float, ty0: Float, tx1: Float, ty1: Float,
                                           vis: Float, glyph: Glyph, light: LightColor, rotate: Boolean
                                  )->Unit
    ) {
        if (vis < 1f) return
        CARDINALS.forEach { dir ->
            if (get(level.getTerrain(x+dir.x,y+dir.y)) !is Water) {
                when (dir) {
                    WEST -> doQuad(x.toDouble(), y.toDouble(), x.toDouble() + 1.0, y.toDouble() + 1.0, 0f, 0f, 1f, 1f, vis, surfGlyph(), light, false)
                    EAST -> doQuad(x.toDouble(), y.toDouble(), x.toDouble() + 1.0, y.toDouble() + 1.0, 1f, 0f, 0f, 1f, vis, surfGlyph(), light, false)
                    NORTH -> doQuad(x.toDouble(), y.toDouble(), x.toDouble() + 1.0, y.toDouble() + 1.0, 0f, 0f, 1f, 1f, vis, surfGlyph(), light, true)
                    SOUTH -> doQuad(x.toDouble(), y.toDouble(), x.toDouble() + 1.0, y.toDouble() + 1.0, 1f, 0f, 0f, 1f, vis, surfGlyph(), light, true)
                }
            }
        }
    }

}

object ShallowWater : Water(Type.TERRAIN_SHALLOW_WATER, Glyph.SHALLOW_WATER) {
    override fun name() = "shallow water"
    override fun isWalkableBy(actor: Actor) = true
}

object DeepWater : Water(Type.TERRAIN_DEEP_WATER, Glyph.DEEP_WATER) {
    override fun isWalkableBy(actor: Actor) = false
}

// Only used in generation, should never appear in the world
object ScratchWater : Water(Type.GENERIC_WATER, Glyph.BLANK) { }

object Lava : Water(Type.TERRAIN_LAVA, Glyph.LAVA) {
    override fun name() = "lava"
    override fun surfGlyph() = Glyph.LAVA_SURF
    override fun glowColor() = LightColor(1f, 0.4f, 0.1f)
    override fun isWalkableBy(actor: Actor) = false
}

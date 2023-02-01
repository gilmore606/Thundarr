package world.terrains

import actors.Actor
import audio.Speaker
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.LightColor
import world.Chunk
import world.level.Level

sealed class Floor(
    type: Type,
    glyph: Glyph,
    canGrowPlants: Boolean
) : Terrain(type, glyph, true, true, false, canGrowPlants, dataType = Type.GENERIC_FLOOR) {

    @Serializable class Data(
        var extraQuads: Set<Quad>
    ) : TerrainData(Type.GENERIC_FLOOR)

    @Serializable class Quad(
        val x0: Double,
        val y0: Double,
        val x1: Double,
        val y1: Double,
        val glyph: Glyph,
        val tx0: Float,
        val ty0: Float,
        val tx1: Float,
        val ty1: Float
    )

    open fun overlapsOn(): Set<Type> = setOf()
    open fun overlapSize() = 0.25f
    open fun overlapInset() = 0.1f

    fun makeOverlaps(chunk: Chunk, x: Int, y: Int): Data {
        val quads = mutableSetOf<Quad>()
        val size = overlapSize()
        val inset = overlapInset()
        val x0 = x.toDouble()
        val y0 = y.toDouble()

        givesOverlapAt(chunk, x, y-1)?.also { glyph ->
            quads.add(Quad(x0 + inset, y0, x0+1.0-inset, y0+size, glyph, 0f, 0f, 1f, size))
        }
        givesOverlapAt(chunk, x, y+1)?.also { glyph ->
            quads.add(Quad(x0+inset, y0+1.0-size, x0+1.0-inset, y0+1.0, glyph, 0f, 0f, 1f, size))
        }
        givesOverlapAt(chunk, x+1, y)?.also { glyph ->
            quads.add(Quad(x0+1.0-size, y0+inset, x0+1.0, y0+1.0-inset, glyph, 0f, 0f, size, 1f))
        }
        givesOverlapAt(chunk, x-1, y)?.also { glyph ->
            quads.add(Quad(x0, y0+inset, x0+size, y0+1.0-inset, glyph, 0f, 0f, size, 1f))
        }

        val shadsize = 0.22f

        var northOK = true
        var eastOK = true
        var westOK = true
        var southOK = true
        if (givesShadowAt(chunk, x, y-1)) {
            quads.add(Quad(x0, y0, x0+1.0, y0+shadsize, Glyph.OCCLUSION_SHADOWS_H, 0f, 0.5f, 1f, 0.75f))
            northOK = false
        }
        if (givesShadowAt(chunk, x, y+1)) {
            quads.add(Quad(x0, y0+1.0-shadsize, x0+1.0, y0+1.0, Glyph.OCCLUSION_SHADOWS_H, 0f, 0.75f, 1f, 0.5f))
            southOK = false
        }
        if (givesShadowAt(chunk, x-1, y)) {
            quads.add(Quad(x0, y0, x0+shadsize, y0+1.0, Glyph.OCCLUSION_SHADOWS_V, 0.5f, 0f, 0.75f, 1f))
            westOK = false
        }
        if (givesShadowAt(chunk, x+1, y)) {
            quads.add(Quad(x0+1.0-shadsize, y0, x0+1.0, y0+1.0, Glyph.OCCLUSION_SHADOWS_V, 0.74f, 0f, 0.51f, 1f))
            eastOK = false
        }
        if (givesShadowAt(chunk, x-1, y-1) && northOK && westOK) {
            quads.add(Quad(x0, y0, x0+shadsize, y0+shadsize, Glyph.OCCLUSION_SHADOWS_V, 0.75f, 0.75f, 1f, 1f))
        }
        if (givesShadowAt(chunk, x+1, y-1) && northOK && eastOK) {
            quads.add(Quad(x0+1.0-shadsize, y0, x0+1.0, y0+shadsize, Glyph.OCCLUSION_SHADOWS_V, 1f, 0.75f, 0.75f, 1f))
        }
        if (givesShadowAt(chunk, x-1, y+1) && southOK && westOK) {
            quads.add(Quad(x0, y0+1.0-shadsize, x0+shadsize, y0+1.0, Glyph.OCCLUSION_SHADOWS_V, 0.75f, 1f, 1f, 0.75f))
        }
        if (givesShadowAt(chunk, x+1, y+1) && southOK && eastOK) {
            quads.add(Quad(x0+1.0-shadsize, y0+1.0-shadsize, x0+1.0, y0+1.0, Glyph.OCCLUSION_SHADOWS_V, 1f, 1f, 0.75f, 0.75f))
        }

        return Data(quads)
    }

    private fun givesOverlapAt(chunk: Chunk, x: Int, y: Int) =
        Terrain.get(chunk.getTerrain(x, y)).let { if (it is Floor && it.overlapsOn().contains(this.type)) it.glyph() else null }

    private fun givesShadowAt(chunk: Chunk, x: Int, y: Int) =
        Terrain.get(chunk.getTerrain(x, y)).let { (!it.isWalkable() && it.isOpaque()) }

    override fun renderExtraQuads(level: Level, x: Int, y: Int, vis: Float, glyph: Glyph, light: LightColor,
                                  doQuad: (x0: Double, y0: Double, x1: Double, y1: Double, tx0: Float, ty0: Float, tx1: Float, ty1: Float,
                                           vis: Float, glyph: Glyph, light: LightColor, rotate: Boolean)->Unit
    ) {
        level.getTerrainData(x, y)?.let { it as Data }?.also {
            it.extraQuads.forEach { quad ->
                doQuad(quad.x0, quad.y0, quad.x1, quad.y1, quad.tx0, quad.ty0, quad.tx1, quad.ty1, vis, quad.glyph, light, false)
            }
        }
    }

    override fun debugData(data: TerrainData?): String {
        if (data == null) return "none"
        val mine = data as Data
        return mine.extraQuads.size.toString() + " extra quads"
    }
}

object CaveFloor : Floor(Type.TERRAIN_CAVEFLOOR, Glyph.CAVE_FLOOR, true) {
    override fun name() = "rock floor"
    override fun moveSpeed(actor: Actor) = 0.8f
    override fun stepSound(actor: Actor) = Speaker.SFX.STEPHARD
    override fun fertilityBonus() = -0.4f
}

object StoneFloor : Floor(Type.TERRAIN_STONEFLOOR, Glyph.STONE_FLOOR, false) {
    override fun name() = "stone floor"
    override fun moveSpeed(actor: Actor) = 0.7f
    override fun stepSound(actor: Actor) = Speaker.SFX.STEPHARD
}

object Dirt : Floor(Type.TERRAIN_DIRT, Glyph.DIRT, true) {
    override fun name() = "bare ground"
    override fun overlapsOn() = setOf(Type.TERRAIN_STONEFLOOR, Type.TERRAIN_PAVEMENT, Type.TERRAIN_ROCKS)
    override fun moveSpeed(actor: Actor) = 0.9f
    override fun stepSound(actor: Actor) = Speaker.SFX.STEPDIRT
    override fun fertilityBonus() = -0.2f
}

object Rocks : Floor(Type.TERRAIN_ROCKS, Glyph.ROCKS, true) {
    override fun name() = "rocky ground"
    override fun moveSpeed(actor: Actor) = 1.4f
    override fun stepSound(actor: Actor) = Speaker.SFX.STEPHARD
    override fun fertilityBonus() = -0.4f
}

object CaveRocks : Floor(Type.TERRAIN_CAVE_ROCKS, Glyph.CAVE_ROCKS, false) {
    override fun name() = "rocky ground"
    override fun moveSpeed(actor: Actor) = 1.4f
    override fun stepSound(actor: Actor) = Speaker.SFX.STEPHARD
}

object Grass : Floor(Type.TERRAIN_GRASS, Glyph.GRASS, true) {
    override fun name() = "grass"
    override fun overlapsOn() = setOf(Type.TERRAIN_STONEFLOOR, Type.TERRAIN_DIRT, Type.TERRAIN_PAVEMENT, Type.TERRAIN_ROCKS, Type.TERRAIN_HARDPAN)
    override fun stepSound(actor: Actor) = Speaker.SFX.STEPGRASS
}

object Undergrowth : Floor(Type.TERRAIN_UNDERGROWTH, Glyph.UNDERGROWTH, true) {
    override fun name() = "undergrowth"
    override fun overlapsOn() = setOf(Type.TERRAIN_GRASS, Type.TERRAIN_DIRT)
    override fun moveSpeed(actor: Actor) = 1.7f
    override fun stepSound(actor: Actor) = Speaker.SFX.STEPGRASS
    override fun fertilityBonus() = 0.4f
}

object Swamp : Floor(Type.TERRAIN_SWAMP, Glyph.SWAMP, true) {
    override fun name() = "bog"
    override fun overlapsOn() = setOf(Type.TERRAIN_GRASS, Type.TERRAIN_DIRT)
    override fun moveSpeed(actor: Actor) = 1.8f
    override fun stepSound(actor: Actor) = Speaker.SFX.STEPDIRT
    override fun fertilityBonus() = 0.2f
}

object Beach : Floor(Type.TERRAIN_BEACH, Glyph.BEACH, true) {
    override fun name() = "beach sand"
    override fun overlapsOn() = setOf(Type.TERRAIN_DIRT, Type.TERRAIN_GRASS, Type.TERRAIN_PAVEMENT, Type.TERRAIN_ROCKS, Type.TERRAIN_HARDPAN)
    override fun moveSpeed(actor: Actor) = 1.3f
    override fun stepSound(actor: Actor) = Speaker.SFX.STEPGRASS
}

object Sand : Floor(Type.TERRAIN_SAND, Glyph.BEACH, true) {
    override fun name() = "sand"
    override fun overlapsOn() = setOf(Type.TERRAIN_DIRT, Type.TERRAIN_GRASS, Type.TERRAIN_PAVEMENT, Type.TERRAIN_ROCKS, Type.TERRAIN_HARDPAN)
    override fun moveSpeed(actor: Actor) = 1.3f
    override fun stepSound(actor: Actor) = Speaker.SFX.STEPGRASS
    override fun fertilityBonus() = -0.2f
}

object Hardpan : Floor(Type.TERRAIN_HARDPAN, Glyph.HARDPAN, true) {
    override fun name() = "hardpan"
    override fun overlapsOn() = setOf(Type.TERRAIN_DIRT, Type.TERRAIN_ROCKS)
    override fun moveSpeed(actor: Actor) = 0.8f
    override fun stepSound(actor: Actor) = Speaker.SFX.STEPDIRT
}

object Pavement : Floor(Type.TERRAIN_PAVEMENT, Glyph.PAVEMENT, false) {
    override fun name() = "pavement"
    override fun moveSpeed(actor: Actor) = 0.7f
    override fun stepSound(actor: Actor) = Speaker.SFX.STEPHARD
}

sealed class Highway : Floor(Type.GENERIC_HIGHWAY, Glyph.HIGHWAY_H, false) {
    override fun name() = "pavement"
    override fun moveSpeed(actor: Actor) = 0.7f
    override fun stepSound(actor: Actor) = Speaker.SFX.STEPHARD
}

object HighwayH : Highway() {
    override fun glyph() = Glyph.HIGHWAY_H
}

object HighwayV : Highway() {
    override fun glyph() = Glyph.HIGHWAY_V
}

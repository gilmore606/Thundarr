package world.terrains

import actors.actors.Actor
import audio.Speaker
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Brick
import things.Rebar
import things.Stick
import util.Dice
import util.LightColor
import world.Chunk
import world.level.Level

sealed class Floor(
    type: Type,
    glyph: Glyph,
    canGrowPlants: Boolean,
    sneakDifficulty: Float = 0f,
) : Terrain(type, glyph, true, false, canGrowPlants,
    dataType = Type.GENERIC_FLOOR, sneakDifficulty = sneakDifficulty) {

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
        Terrain.get(chunk.getTerrain(x, y)).let { (!it.isWalkableBy(App.player) && it.isOpaque()) }

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
    override fun sleepComfort() = 0f
}

object StoneFloor : Floor(Type.TERRAIN_STONEFLOOR, Glyph.STONE_FLOOR, false, -2f) {
    override fun name() = "stone floor"
    override fun moveSpeed(actor: Actor) = 0.7f
    override fun stepSound(actor: Actor) = Speaker.SFX.STEPHARD
    override fun trailsOverwrite() = false
    override fun sleepComfort() = 0f
}

object WoodFloor: Floor(Type.TERRAIN_WOODFLOOR, Glyph.WOOD_FLOOR, false, -2f) {
    override fun name() = "wood planks"
    override fun moveSpeed(actor: Actor) = 0.7f
    override fun stepSound(actor: Actor) = Speaker.SFX.STEPHARD
    override fun trailsOverwrite() = false
    override fun sleepComfort() = 0f
}

object Hearth: Floor(Type.TERRAIN_HEARTH, Glyph.HEARTH, false) {
    override fun name() = "hearth"
    override fun overlapsOn() = setOf(Type.TERRAIN_STONEFLOOR, Type.TERRAIN_WOODFLOOR, Type.TERRAIN_CAVEFLOOR)
    override fun moveSpeed(actor: Actor) = 1.0f
    override fun stepSound(actor: Actor) = Speaker.SFX.STEPDIRT
    override fun trailsOverwrite() = false
}

object Dirt : Floor(Type.TERRAIN_DIRT, Glyph.DIRT, true, -1f) {
    override fun name() = "bare ground"
    override fun overlapsOn() = setOf(Type.TERRAIN_STONEFLOOR, Type.TERRAIN_PAVEMENT, Type.TERRAIN_ROCKS)
    override fun moveSpeed(actor: Actor) = 1.0f
    override fun stepSound(actor: Actor) = Speaker.SFX.STEPDIRT
    override fun fertilityBonus() = -0.2f
    override fun sleepComfort() = -0.2f
}

object Trail : Floor(Type.TERRAIN_TRAIL, Glyph.TRAIL, false, -1f) {
    override fun name() = "trail"
    override fun overlapsOn() = setOf(Type.TERRAIN_ROCKS, Type.TERRAIN_DIRT, Type.TERRAIN_GRASS, Type.TERRAIN_SAND, Type.TERRAIN_HARDPAN, Type.TERRAIN_UNDERGROWTH)
    override fun moveSpeed(actor: Actor) = 0.8f
    override fun stepSound(actor: Actor) = Speaker.SFX.STEPDIRT
    override fun sleepComfort() = -0.2f
}

object Grass : Floor(Type.TERRAIN_GRASS, Glyph.GRASS, true) {
    override fun name() = "grass"
    override fun overlapsOn() = setOf(Type.TERRAIN_STONEFLOOR, Type.TERRAIN_DIRT, Type.TERRAIN_PAVEMENT, Type.TERRAIN_ROCKS, Type.TERRAIN_HARDPAN)
    override fun stepSound(actor: Actor) = Speaker.SFX.STEPGRASS
    override fun sleepComfort() = 0f
}

object Undergrowth : Floor(Type.TERRAIN_UNDERGROWTH, Glyph.UNDERGROWTH, true, 2f) {
    override fun name() = "undergrowth"
    override fun overlapsOn() = setOf(Type.TERRAIN_GRASS, Type.TERRAIN_DIRT)
    override fun moveSpeed(actor: Actor) = 1.7f
    override fun stepSound(actor: Actor) = Speaker.SFX.STEPGRASS
    override fun fertilityBonus() = 0.4f

    override fun scavengeCommand() = "find a stick"
    override fun scavengeDifficulty() = -2f
    override fun scavengeProduct() = Stick()
    override fun scavengeMsg() = "You root around in the undergrowth and find a sturdy stick."
    override fun scavengeFailMsg() = "You root around a while, but find only thorns and leaves."
}

object Swamp : Floor(Type.TERRAIN_SWAMP, Glyph.SWAMP, true, 2f) {
    override fun name() = "bog"
    override fun overlapsOn() = setOf(Type.TERRAIN_GRASS, Type.TERRAIN_DIRT)
    override fun moveSpeed(actor: Actor) = 1.8f
    override fun stepSound(actor: Actor) = Speaker.SFX.STEPDIRT
    override fun fertilityBonus() = 0.2f
}

object Beach : Floor(Type.TERRAIN_BEACH, Glyph.BEACH, true) {
    override fun name() = "beach sand"
    override fun overlapsOn() = setOf(Type.TERRAIN_DIRT, Type.TERRAIN_GRASS, Type.TERRAIN_PAVEMENT, Type.TERRAIN_ROCKS, Type.TERRAIN_HARDPAN)
    override fun trailsOverwrite() = false
    override fun moveSpeed(actor: Actor) = 1.3f
    override fun stepSound(actor: Actor) = Speaker.SFX.STEPGRASS
    override fun sleepComfort() = 0f
}

object Sand : Floor(Type.TERRAIN_SAND, Glyph.BEACH, true) {
    override fun name() = "sand"
    override fun overlapsOn() = setOf(Type.TERRAIN_DIRT, Type.TERRAIN_GRASS, Type.TERRAIN_PAVEMENT, Type.TERRAIN_ROCKS, Type.TERRAIN_HARDPAN)
    override fun moveSpeed(actor: Actor) = 1.3f
    override fun stepSound(actor: Actor) = Speaker.SFX.STEPGRASS
    override fun fertilityBonus() = -0.2f
    override fun sleepComfort() = 0.2f
}

object Hardpan : Floor(Type.TERRAIN_HARDPAN, Glyph.HARDPAN, true, -1f) {
    override fun name() = "hardpan"
    override fun overlapsOn() = setOf(Type.TERRAIN_DIRT, Type.TERRAIN_ROCKS)
    override fun moveSpeed(actor: Actor) = 0.8f
    override fun stepSound(actor: Actor) = Speaker.SFX.STEPDIRT
    override fun sleepComfort() = -0.2f
}

object Pavement : Floor(Type.TERRAIN_PAVEMENT, Glyph.PAVEMENT, false, -2f) {
    override fun name() = "pavement"
    override fun moveSpeed(actor: Actor) = 0.7f
    override fun stepSound(actor: Actor) = Speaker.SFX.STEPHARD
    override fun sleepComfort() = -0.1f
}

object Rubble : Floor(Type.TERRAIN_RUBBLE, Glyph.RUBBLE, false, 2f) {
    override fun name() = "rubble"
    override fun moveSpeed(actor: Actor) = 1.8f
    override fun stepSound(actor: Actor) = Speaker.SFX.STEPDIRT

    override fun scavengeCommand() = "search rubble"
    override fun scavengeDifficulty() = -1f
    override fun scavengeProduct() = if (Dice.flip()) Rebar() else Brick()
    override fun scavengeMsg() = "You poke in the rubble and uncover %it."
    override fun scavengeFailMsg() = "You fail to find anything useful in the rubble."
}

sealed class Highway : Floor(Type.GENERIC_HIGHWAY, Glyph.HIGHWAY_H, false, -2f) {
    override fun name() = "pavement"
    override fun moveSpeed(actor: Actor) = 0.7f
    override fun stepSound(actor: Actor) = Speaker.SFX.STEPHARD
    override fun trailsOverwrite() = false
    override fun sleepComfort() = -0.1f
}

object HighwayH : Highway() {
    override fun glyph() = Glyph.HIGHWAY_H
}

object HighwayV : Highway() {
    override fun glyph() = Glyph.HIGHWAY_V
}

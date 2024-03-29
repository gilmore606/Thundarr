package world.terrains

import actors.actors.Actor
import actors.actors.Player
import actors.stats.skills.Survive
import audio.Speaker
import kotlinx.serialization.Serializable
import render.Screen
import render.sparks.Scoot
import render.sparks.Spark
import render.tilesets.Glyph
import things.Thing
import ui.panels.Console
import util.LightColor
import util.XY
import world.Entity
import world.level.Level

sealed class Terrain(
    val type: Type,
    private val glyph: Glyph,
    private val flyable: Boolean,
    private val opaque: Boolean,
    val canGrowPlants: Boolean,
    val dataType: Type = type,  // should match TerrainData.forType
    val sneakDifficulty: Float = 0f,
) : Entity {

    companion object {
        val xy = XY(0,0)
        fun get(type: Type) = when (type) {
            Type.BLANK -> Blank
            Type.TEMP1 -> Blank
            Type.TEMP2 -> Blank
            Type.TEMP3 -> Blank
            Type.TEMP4 -> Blank
            Type.TEMP5 -> Blank
            Type.TERRAIN_BRICKWALL -> BrickWall
            Type.TERRAIN_CAVEWALL -> CaveWall
            Type.TERRAIN_WOODWALL -> WoodWall
            Type.TERRAIN_METALWALL -> MetalWall
            Type.TERRAIN_TEMPERATE_FORESTWALL -> TemperateForestWall
            Type.TERRAIN_PINE_FORESTWALL -> PineForestWall
            Type.TERRAIN_TROPICAL_FORESTWALL -> TropicalForestWall
            Type.TERRAIN_WINDOWWALL -> WindowWall
            Type.TERRAIN_CAVEFLOOR -> CaveFloor
            Type.TERRAIN_STONEFLOOR -> StoneFloor
            Type.TERRAIN_WOODFLOOR -> WoodFloor
            Type.TERRAIN_DIRT -> Dirt
            Type.TERRAIN_ROCKS -> Rocks
            Type.TERRAIN_CAVE_ROCKS -> CaveRocks
            Type.TERRAIN_GRASS -> Grass
            Type.TERRAIN_UNDERGROWTH -> Undergrowth
            Type.TERRAIN_HARDPAN -> Hardpan
            Type.TERRAIN_SWAMP -> Swamp
            Type.TERRAIN_BEACH -> Beach
            Type.TERRAIN_SAND -> Sand
            Type.TERRAIN_SHALLOW_WATER -> ShallowWater
            Type.TERRAIN_DEEP_WATER -> DeepWater
            Type.GENERIC_WATER -> ScratchWater
            Type.TERRAIN_PORTAL_DOOR -> PortalDoor
            Type.TERRAIN_PORTAL_CAVE -> PortalCave
            Type.TERRAIN_PAVEMENT -> Pavement
            Type.TERRAIN_HIGHWAY_H -> HighwayH
            Type.TERRAIN_HIGHWAY_V -> HighwayV
            Type.TERRAIN_LAVA -> Lava
            Type.TERRAIN_CHASM -> Chasm
            Type.TERRAIN_RUBBLE -> Rubble
            Type.TERRAIN_TRAIL -> Trail
            Type.TERRAIN_HEARTH -> Hearth
            else -> throw RuntimeException("tried to get(terrainType) for un-instantiatable type $type !")
        }
        fun getTiled(type: Int): Terrain.Type? = when (type) {
            27 -> null
            24,25 -> Type.TERRAIN_DIRT
            1 -> Type.TERRAIN_STONEFLOOR
            3 -> Type.TERRAIN_BRICKWALL
            28 -> Type.TERRAIN_PORTAL_DOOR
            8 -> Type.TERRAIN_GRASS
            else -> null
        }
    }

    enum class Type {
        BLANK,
        TEMP1,
        TEMP2,
        TEMP3,
        TEMP4,
        TEMP5,
        GENERIC_WALL,
        GENERIC_FLOOR,
        GENERIC_WATER,
        GENERIC_HIGHWAY,
        TERRAIN_BRICKWALL,
        TERRAIN_CAVEWALL,
        TERRAIN_WOODWALL,
        TERRAIN_METALWALL,
        TERRAIN_WINDOWWALL,
        TERRAIN_TEMPERATE_FORESTWALL,
        TERRAIN_PINE_FORESTWALL,
        TERRAIN_TROPICAL_FORESTWALL,
        TERRAIN_STONEFLOOR,
        TERRAIN_CAVEFLOOR,
        TERRAIN_WOODFLOOR,
        TERRAIN_DIRT,
        TERRAIN_ROCKS,
        TERRAIN_CAVE_ROCKS,
        TERRAIN_GRASS,
        TERRAIN_UNDERGROWTH,
        TERRAIN_HARDPAN,
        TERRAIN_SWAMP,
        TERRAIN_BEACH,
        TERRAIN_SAND,
        TERRAIN_SHALLOW_WATER,
        TERRAIN_DEEP_WATER,
        TERRAIN_PORTAL_DOOR,
        TERRAIN_PORTAL_CAVE,
        TERRAIN_PAVEMENT,
        TERRAIN_HIGHWAY_H,
        TERRAIN_HIGHWAY_V,
        TERRAIN_LAVA,
        TERRAIN_CHASM,
        TERRAIN_RUBBLE,
        TERRAIN_TRAIL,
        TERRAIN_HEARTH,
    }

    override fun glyph() = this.glyph
    override fun description() = ""
    override fun glyphBatch() = Screen.terrainBatch
    override fun uiBatch() = Screen.uiTerrainBatch
    override fun level() = null
    override fun xy() = Terrain.xy

    open fun renderExtraQuads(level: Level, x: Int, y: Int, vis: Float, glyph: Glyph, light: LightColor,
                              doQuad: (x0: Double, y0: Double, x1: Double, y1: Double, tx0: Float, tx1: Float, ty0: Float, ty1: Float,
                                       vis: Float, glyph: Glyph, light: LightColor, rotate: Boolean)->Unit) { }

    open fun isWalkableBy(actor: Actor) = actor.canWalkOn(this)
    open fun isOpaque() = this.opaque
    open fun moveSpeed(actor: Actor) = 1f
    open fun stepSound(actor: Actor): Speaker.SFX? = null
    open fun stepSpark(actor: Actor, dir: XY): Spark? = Scoot(dir)
    open fun fertilityBonus() = 0f
    open fun glowColor(): LightColor? = null
    open fun trailsOverwrite() = true
    open fun sleepComfort() = -0.5f

    open fun scavengeCommand(): String? = null
    open fun scavengeDifficulty(): Float = 0f
    open fun scavengeMsg() = "You poke around and find %it."
    open fun scavengeFailMsg() = "You root around a while, but fail to find anything useful."
    open fun scavengeProduct(): Thing? = null
    open fun uses(): Map<Thing.UseTag, Thing.Use> = scavengeCommand()?.let { command ->
        mapOf(
            Thing.UseTag.TRANSFORM to Thing.Use(command, 4f,
                canDo = { actor,x,y,targ -> true },
                toDo = { actor,level,x,y -> doScavenge(actor) }
            )
        )
    } ?: mapOf()

    open fun doScavenge(actor: Actor) {
        if (Survive.resolve(actor, scavengeDifficulty()) >= 0) {
            scavengeProduct()?.also { loot ->
                if (actor is Player) Console.sayAct(scavengeMsg(), "", actor, loot)
                loot.moveTo(actor)
            }
        } else {
            if (actor is Player) Console.sayAct(scavengeFailMsg(), "", actor)
        }
    }

    open fun onBump(actor: Actor, x: Int, y: Int, data: TerrainData?) { }
    open fun onStep(actor: Actor, x: Int, y: Int, data: TerrainData?) { }

    open fun pruneVerticalOrphans() = false

    open fun debugData(data: TerrainData?): String { return "none" }
}

object Blank : Terrain(Type.BLANK, Glyph.BLANK, true, false, false) {
    override fun name() = "BLANK"
}

@Serializable
sealed class TerrainData(
    val forType: Terrain.Type
)

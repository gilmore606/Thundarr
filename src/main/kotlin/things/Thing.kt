package things

import actors.Actor
import actors.NPC
import actors.actions.Action
import audio.Speaker
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.Screen
import render.tilesets.Glyph
import ui.modals.DirectionModal
import ui.panels.Console
import util.*
import world.Entity
import world.level.Level
import world.stains.Fire
import java.lang.Float.min
import java.lang.RuntimeException

@Serializable
sealed class Thing() : Entity {

    @Serializable
    data class Key(
        val xy: XY,
        val id: String
    ) {
        @Transient var thing: Thing? = null
        fun getThing(level: Level): Thing? = thing ?: run {
            thing = level.thingByKey(xy.x, xy.y, id)
            return thing
        }
    }

    val id = UUID()
    fun getKey() = Key(xy(), id)
    override fun toString() = "${name()}($id)"

    open fun containsByID(byID: String): Thing? {
        if (byID == this.id) return this
        return null
    }

    enum class Tag(
        val singularName: String,
        val pluralName: String,
        val spawn: ()->Thing
    ) {
        THING_NPCDEN("NPC DEN", "NPC DENS", { NPCDen(NPC.Tag.NPC_TUSKLET) }),
        THING_BEDROLL("bedroll", "bedrolls", { Bedroll() }),
        THING_PAPERBACK("paperback", "paperbacks", { Paperback() }),
        THING_BOYSLIFE("Boys Life magazine", "Boys Life magazines", { BoysLife() }),
        THING_HARDHAT("hardhat", "hardhats", { HardHat() }),
        THING_HORNEDHELMET("horned helmet", "horned helmets", { HornedHelmet() }),
        THING_RIOTHELMET("riot helmet", "riot helmets", { RiotHelmet() }),
        THING_MOKBOOTS("mok books", "pairs of mok boots", { MokBoots() }),
        THING_TRAVELBOOTS("travel boots", "pairs of travel boots", { TravelBoots() }),
        THING_FURTUNIC("fur tunic", "fur tunics", { FurTunic() }),
        THING_FURJACKET("fur jacket", "fur jackets", { FurJacket() }),
        THING_SABRETOOTHCHARM("sabretooth charm", "sabretooth charms", { SabretoothCharm() }),
        THING_BRICK("brick", "bricks", { Brick() }),
        THING_ROCK("rock", "rocks", { Rock() }),
        THING_LIGHTER("lighter", "lighters", { Lighter() }),
        THING_APPLE("apple", "apples", { Apple() }),
        THING_PEAR("pear", "pears", { Pear() }),
        THING_RAWMEAT("chunk of raw meat", "chunks of raw meat", { RawMeat() }),
        THING_STEAK("steak", "steaks", { Steak() }),
        THING_CHICKENLEG("chicken leg", "chicken legs", { ChickenLeg() }),
        THING_CHEESE("cheese", "cheeses", { Cheese() }),
        THING_STEW("bowl of stew", "bowls of stew", { Stew() }),
        THING_THRALLCHOW("thrall chow", "thrall chows", { ThrallChow() }),
        THING_ENERGYDRINK("energy drink", "energy drinks", { EnergyDrink() }),

        THING_FILINGCABINET("filing cabinet", "filing cabinets", { FilingCabinet() }),
        THING_BOOKSHELF("bookshelf", "bookshelves", { Bookshelf() }),
        THING_WARDROBE("wardrobe", "wardrobes", { Wardrobe() }),
        THING_FRIDGE("fridge", "fridges", { Fridge() }),
        THING_TRUNK("storage trunk", "storage trunks", { Trunk() }),
        THING_BONEPILE("bone pile", "bone piles", { Bonepile() }),
        THING_WRECKEDCAR("wrecked car", "wrecked cars", { WreckedCar() }),
        THING_CORPSE("corpse", "corpses", { Corpse() }),
        THING_TABLE("table", "tables", { Table() }),

        THING_MODERNDOOR("door", "doors", { ModernDoor() }),
        THING_WOODDOOR("door", "doors", { WoodDoor() }),
        THING_FORGE("forge", "forges", { Forge() }),
        THING_LOG("log", "logs", { Log() }),
        THING_BOARD("board", "boards", { Board() }),
        THING_LIGHTBULB("lightbulb", "lightbulbs", { Lightbulb() }),
        THING_CEILINGLIGHT("ceiling light", "ceiling lights", { CeilingLight() }),
        THING_GLOWSTONE("glowstone", "glowstones", { Glowstone() }),
        THING_SUNSWORD("sunsword", "sunswords", { Sunsword() }),
        THING_TORCH("torch", "torches", { Torch() }),
        THING_BANDAGES("bandages", "bandages", { Bandages() }),
        THING_FIRSTAIDKIT("first aid kit", "first aid kits", { FirstAidKit() }),
        THING_FIST("fists", "fists", { Fist() }),
        THING_TEETH("teeth", "teeth", { Teeth() }),
        THING_AXE("axe", "axes", { Axe() }),
        THING_PICKAXE("pickaxe", "pickaxes", { Pickaxe() }),
        THING_THORNBUSH("thornbush", "thornbushes", { ThornBush() }),
        THING_SAGEBUSH("sagebush", "sagebushes", { SageBush() }),
        THING_BERRYBUSH("berry bush", "berry bushes", { BerryBush() }),
        THING_HONEYPODBUSH("honeypod bush", "honeypod bushes", { HoneypodBush() }),
        THING_WILDFLOWERS("wildflowers", "wildflowers", { Wildflowers() }),
        THING_POPPIES("poppies", "poppies", { Poppies() }),
        THING_DEATHFLOWER("deathflower", "deathflowers", { Deathflower() }),
        THING_DREAMFLOWER("dreamflower", "dreamflowers", { Dreamflower() }),
        THING_SUNFLOWER("sunflower", "sunflowers", { Sunflower() }),
        THING_LIGHTFLOWER("lightflower", "lightflowers", { Lightflower() }),
        THING_SAGUARO("saguaro", "saguaros", { Saguaro() }),
        THING_CHOLLA("cholla", "chollas", { Cholla() }),
        THING_PRICKPEAR("prickpear", "prickpears", { Prickpear() }),
        THING_BALMMOSS("balm moss", "balm mosses", { BalmMoss() }),
        THING_LACEMOSS("lace moss", "lace mosses", { LaceMoss() }),
        THING_WIZARDCAP_MYCELIUM("", "", { WizardcapMycelium() }),
        THING_SPECKLED_MYCELIUM("", "",{ SpeckledMycelium() }),
        THING_BLOODCAP_MYCELIUM("", "", { BloodcapMycelium() }),
        THING_WIZARDCAP_MUSHROOM("wizardcap mushroom", "wizardcap mushrooms", { WizardcapMushroom() }),
        THING_SPECKLED_MUSHROOM("speckled mushroom", "speckled mushrooms", { SpeckledMushroom() }),
        THING_BLOODCAP_MUSHROOM("bloodcap mushroom", "bloodcap mushrooms", { BloodcapMushroom() }),
        THING_FOOLSLEAF("foolsleaf", "foolsleaves", { Foolsleaf() }),
        THING_HIGHWAYSIGN("highway sign", "highway signs", { HighwaySign("") }),
        THING_TRAILSIGN("trail sign", "trail signs", { TrailSign("") }),
        THING_BOULDER("boulder", "boulders", { Boulder() }),
        THING_SHRINE("shrine", "shrines", { Shrine() }),
        THING_OAKTREE("oak tree", "oak trees", { OakTree() }),
        THING_TEAKTREE("teak tree", "teak trees", { TeakTree() }),
        THING_MAPLETREE("maple tree", "maple trees", { MapleTree() }),
        THING_BIRCHTREE("birch tree", "birch trees", { BirchTree() }),
        THING_APPLETREE("apple tree", "apple trees", { AppleTree() }),
        THING_PEARTREE("pear tree", "pear trees", { PearTree() }),
        THING_PINETREE("pine tree", "pine trees", { PineTree() }),
        THING_SPRUCETREE("spruce tree", "spruce trees", { SpruceTree() }),
        THING_PALMTREE("palm tree", "palm trees", { PalmTree() }),
        THING_COCONUTTREE("coconut tree", "coconut trees", { CoconutTree() }),
        THING_DEADTREE("dead tree", "dead trees", { DeadTree() }),
        THING_WELL("well", "wells", { Well() }),
        THING_CAMPFIRE("campfire", "campfires", { Campfire() }),
        THING_GRAVESTONE("gravestone", "gravestones", { Gravestone("") }),
        THING_HIDE("hide", "hides", { Hide() }),
        THING_SCALYHIDE("scaly hide", "scaly hides", { ScalyHide() }),
        THING_CANDLESTICK("candlestick", "candlesticks", { Candlestick() }),
        THING_LAMPPOST("lamppost", "lampposts", { Lamppost() }),
    }

    abstract val tag: Tag
    abstract fun isOpaque(): Boolean
    open fun isBlocking(actor: Actor): Boolean = false
    abstract fun isPortable(): Boolean
    open fun isIntangible() = false
    open fun isAlwaysVisible() = false
    open fun announceOnWalk() = !isIntangible()

    @Transient var holder: ThingHolder? = null

    enum class Category {
        GEAR, TOOL, CONSUMABLE, MISC
    }

    open fun category(): Category = Category.MISC

    enum class UseTag {
        E0, E1, E2, E3, E4, E5, E6, E7, E8, E9,
        USE, USE_ON, SWITCH, SWITCH_ON, SWITCH_OFF, CONSUME, OPEN, CLOSE, EQUIP, UNEQUIP, DESTROY, TRANSFORM,
    }

    class Use(
        val command: String,
        val duration: Float,
        val canDo: ((Actor, Int, Int, Boolean)->Boolean) = { _,_,_,_ -> false },
        val toDo: ((Actor, Level, Int, Int)->Unit) = { _,_,_,_ -> }
    ) {
        companion object {
            fun enumeratedTag(n: Int) = when (n) {
                0 -> UseTag.E0
                1 -> UseTag.E1
                2 -> UseTag.E2
                3 -> UseTag.E3
                4 -> UseTag.E4
                5 -> UseTag.E5
                6 -> UseTag.E6
                7 -> UseTag.E7
                8 -> UseTag.E8
                9 -> UseTag.E9
                else -> UseTag.E1
            }
        }
    }

    private var spawned = false

    init {
        if (!spawned) {
            spawned = true
            onCreate()
        }
    }

    open fun onCreate() { }
    open fun onSpawn() { }

    open fun uses(): Map<UseTag, Use> = mapOf()
    open fun canSpawnIn(containerType: Thing.Tag) = spawnContainers().hasOneWhere { it == containerType }
    open fun spawnContainers() = mutableListOf<Thing.Tag>()

    protected fun isHeldBy(actor: Actor) = actor.contents.contains(this)
    protected fun isAtFeet(actor: Actor) = holder?.let { it.xy() == actor.xy() } ?: false
    protected fun isNextTo(actor: Actor) = holder?.let { it.xy().let { xy ->
        Math.abs(xy.x - actor.xy.x) < 2 && Math.abs(xy.y - actor.xy.y) < 2
    }} ?: false
    protected fun isNextTo(x: Int, y: Int) = holder?.let { it.xy().let { xy ->
        Math.abs(xy.x - x) < 2 && Math.abs(xy.y - y) < 2
    }} ?: false

    override fun description() =  ""
    open fun listTag() = if (tag == App.player.thrownTag) "(throwing)" else ""
    fun listName() = name() + " " + listTag()

    override fun examineInfo(): String {
        if (thrownDamage(App.player, 6f) > defaultThrownDamage()) {
            return "It looks like it would do extra damage when thrown."
        }
        return super.examineInfo()
    }

    override fun level() = holder?.level
    override fun xy() = holder?.xy() ?: XY(0,0)
    override fun glyphBatch() = Screen.thingBatch
    override fun uiBatch() = Screen.uiThingBatch

    open fun drawExtraGlyphs(toDraw: (Glyph,Float,Float,Float)->Unit) { }

    open fun weight() = 0.1f
    open fun flammability() = 0f
    open fun onBurn(delta: Float): Float { // return the amount of fuel we provided on this turn
        if (Dice.chance(flammability())) {
            moveTo(null)
            return 0f
        } else {
            return 1f * delta
        }
    }

    open fun onWalkedOnBy(actor: Actor) { }
    open fun convertMoveAction(actor: Actor): Action? = null
    open fun playerBumpAction(): Action? = null

    open fun onRestore(holder: ThingHolder) {
        this.holder = holder
    }

    fun moveTo(to: ThingHolder?) {
        val from = holder
        holder?.remove(this)
        if (this is Temporal) holder?.level?.unlinkTemporal(this)
        this.holder = to
        to?.add(this)
        if (this is Temporal) holder?.level?.linkTemporal(this)
        onMoveTo(from, to)
    }
    fun moveTo(x: Int, y: Int) = moveTo(level()?.cellContainerAt(x, y) ?: throw RuntimeException("moved $this to local coords but it wasn't in a level!"))
    fun moveTo(level: Level, x: Int, y: Int) = moveTo(level.cellContainerAt(x, y))

    open fun onDropping(actor: Actor, dest: ThingHolder) { }
    open fun onMoveTo(from: ThingHolder?, to: ThingHolder?) { }

    // Move speed penalty to walk past/through this thing on the ground
    open fun moveSpeedPast(actor: Actor): Float? = null

    open fun thrownDamage(thrower: Actor, roll: Float) = defaultThrownDamage()
    private fun defaultThrownDamage() = min(weight() / 0.1f, 4f)
    open fun thrownAccuracy() = -1f
    open fun onThrownOn(target: Actor) { moveTo(target.xy.x, target.xy.y) }
    open fun onThrownAt(level: Level, x: Int, y: Int) { moveTo(level, x, y) }
    open fun thrownHitSound() = Speaker.SFX.ROCKHIT

    open fun toolbarName(): String? = null
    open fun toolbarUseTag(): UseTag? = null
    open fun toolbarAction(instance: Thing) {
        toolbarUseTag()?.also { tag ->
            val use = uses()[tag]!!
            if (use.canDo(App.player, App.player.xy.x, App.player.xy.y, false)) {
                App.player.queue(actors.actions.Use(tag, instance.getKey(), use.duration))
            }
        }
    }

    open fun ambientSound(): Speaker.PointAmbience? = null
}

@Serializable
sealed class Portable : Thing() {
    override fun isOpaque() = false
    override fun isBlocking(actor: Actor) = false
    override fun isPortable() = true
}

@Serializable
class Brick : Portable() {
    override val tag = Tag.THING_BRICK
    override fun name() = "brick"
    override fun description() = "A squared hunk of stone.  Could be used to kill, or build."
    override fun glyph() = Glyph.BRICK
    override fun weight() = 0.4f
    override fun thrownDamage(thrower: Actor, roll: Float) = super.thrownDamage(thrower, roll) + 1.5f
}

@Serializable
class Rock : Portable() {
    override val tag = Tag.THING_ROCK
    override fun name() = "rock"
    override fun description() = "A chunk of rock.  You could throw it at someone."
    override fun glyph() = Glyph.ROCK
    override fun weight() = 0.3f
    override fun thrownDamage(thrower: Actor, roll: Float) = super.thrownDamage(thrower, roll) + 1.5f
}

@Serializable
class Hide : Portable() {
    override val tag = Tag.THING_HIDE
    override fun name() = "hide"
    override fun description() = "A leather animal hide.  You could make something out of it."
    override fun glyph() = Glyph.LEATHER
    override fun weight() = 0.5f
}

@Serializable
class ScalyHide: Portable() {
    override val tag = Tag.THING_SCALYHIDE
    override fun name() = "scaly hide"
    override fun description() = "A thick animal hide covered in rigid scales.  You could make something out of it."
    override fun glyph() = Glyph.LEATHER
    override fun weight() = 0.7f
}

@Serializable
class Lighter : Portable() {
    override val tag = Tag.THING_LIGHTER
    override fun name() = "lighter"
    override fun description() = "A brass cigarette lighter.  Handy for starting fires."
    override fun glyph() = Glyph.LIGHTER
    override fun category() = Category.TOOL
    override fun weight() = 0.02f
    override fun uses() = mapOf(
        UseTag.USE to Use("light fire nearby", 2.0f,
            canDo = { actor,x,y,targ ->
                var canDo = false
                if (actor.xy.x == x && actor.xy.y == y) {
                    DIRECTIONS.forEach { if (hasTargetAt(it.x + x, it.y + y)) canDo = true }
                } else canDo = hasTargetAt(x,y)
                canDo && isHeldBy(actor)
            },
            toDo = { actor, level, x, y ->
                if (actor.xy.x == x && actor.xy.y == y) askDirection(actor, level)
                else lightFireAt(actor, level, XY(x,y))
            })
    )
    override fun toolbarName() = "light fire nearby"
    override fun toolbarUseTag() = UseTag.USE
    override fun spawnContainers() = mutableListOf(Tag.THING_TRUNK, Tag.THING_WRECKEDCAR, Tag.THING_BONEPILE, Tag.THING_TABLE)

    private fun hasTargetAt(x: Int, y: Int): Boolean = holder?.level?.thingsAt(x, y)?.hasOneWhere { it.flammability() > 0f } ?: false

    private fun askDirection(actor: Actor, level: Level) {
        Screen.addModal(DirectionModal("Light a fire in which direction?")
        { xy ->
            if (xy == NO_DIRECTION) {
                Console.say("Are you crazy?  You'd be standing in a fire!")
            } else {
                lightFireAt(actor, level, XY(actor.xy.x + xy.x, actor.xy.y + xy.y))
            }
        })
    }
    private fun lightFireAt(actor: Actor, level: Level, xy: XY) {
        level.addStain(Fire(), xy.x, xy.y)
        Console.sayAct("You start a fire.", "%Dn lights a fire.", actor)
    }
}

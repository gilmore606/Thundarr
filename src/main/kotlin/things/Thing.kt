package things

import actors.Actor
import actors.NPC
import actors.actions.Action
import audio.Speaker
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.Screen
import render.tilesets.Glyph
import util.*
import variants.Variant
import world.Entity
import world.gen.spawnsets.Spawnable
import world.level.Level
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
        val spawn: ()->Thing,
        val levels: Pair<Int,Int> = Pair(0,100),
        val variants: Set<Variant<Thing>>? = null
    ) : Spawnable<Thing> {

        NPCDEN("NPC DEN", "NPC DENS", { NPCDen(NPC.Tag.NPC_TUSKLET) }),
        BEDROLL("bedroll", "bedrolls", { Bedroll() }),
        PAPERBACK("paperback", "paperbacks", { Paperback() }),
        BOYSLIFE("Boys Life magazine", "Boys Life magazines", { BoysLife() }),
        HARDHAT("hardhat", "hardhats", { HardHat() }),
        HORNEDHELMET("horned helmet", "horned helmets", { HornedHelmet() }),
        RIOTHELMET("riot helmet", "riot helmets", { RiotHelmet() }),
        MOKBOOTS("mok books", "pairs of mok boots", { MokBoots() }),
        TRAVELBOOTS("travel boots", "pairs of travel boots", { TravelBoots() }),
        FURTUNIC("fur tunic", "fur tunics", { FurTunic() }),
        FURJACKET("fur jacket", "fur jackets", { FurJacket() }),
        SABRETOOTH_CHARM("sabretooth charm", "sabretooth charms", { SabretoothCharm() }),
        BRICK("brick", "bricks", { Brick() }),
        ROCK("rock", "rocks", { Rock() }),
        LIGHTER("lighter", "lighters", { Lighter() }),
        APPLE("apple", "apples", { Apple() }),
        PEAR("pear", "pears", { Pear() }),
        RAWMEAT("chunk of raw meat", "chunks of raw meat", { RawMeat() }),
        STEAK("steak", "steaks", { Steak() }),
        CHICKENLEG("chicken leg", "chicken legs", { ChickenLeg() }),
        CHEESE("cheese", "cheeses", { Cheese() }),
        STEW("bowl of stew", "bowls of stew", { Stew() }),
        THRALLCHOW("thrall chow", "thrall chows", { ThrallChow() }),
        ENERGYDRINK("energy drink", "energy drinks", { EnergyDrink() }),

        FILING_CABINET("filing cabinet", "filing cabinets", { FilingCabinet() }),
        STORAGE_CABINET("storage cabinet", "storage cabinets", { StorageCabinet() }),
        BOOKSHELF("bookshelf", "bookshelves", { Bookshelf() }),
        WARDROBE("wardrobe", "wardrobes", { Wardrobe() }),
        FRIDGE("fridge", "fridges", { Fridge() }),
        TRUNK("storage trunk", "storage trunks", { Trunk() }),
        BONEPILE("bone pile", "bone piles", { Bonepile() }),
        WRECKEDCAR("wrecked car", "wrecked cars", { WreckedCar() }),
        CORPSE("corpse", "corpses", { Corpse() }),
        TABLE("table", "tables", { Table() }),

        METAL_DOOR("door", "doors", { ModernDoor() }),
        WOOD_DOOR("door", "doors", { WoodDoor() }),
        FORGE("forge", "forges", { Forge() }),
        LOG("log", "logs", { Log() }),
        BOARD("board", "boards", { Board() }),
        LIGHTBULB("lightbulb", "lightbulbs", { Lightbulb() }),
        CEILING_LIGHT("ceiling light", "ceiling lights", { CeilingLight() }),
        GLOWSTONE("glowstone", "glowstones", { Glowstone() }),
        SUNSWORD("sunsword", "sunswords", { Sunsword() }),
        TORCH("torch", "torches", { Torch() }),
        BANDAGES("bandages", "bandages", { Bandages() }),
        FIRSTAIDKIT("first aid kit", "first aid kits", { FirstAidKit() }),
        FIST("fists", "fists", { Fist() }),
        TEETH("teeth", "teeth", { Teeth() }),
        STONE_AXE("stone axe", "stone axes", { StoneAxe() }),
        AXE("axe", "axes", { Axe() }),
        PICKAXE("pickaxe", "pickaxes", { Pickaxe() }),
        THORNBUSH("thornbush", "thornbushes", { ThornBush() }),
        SAGEBUSH("sagebush", "sagebushes", { SageBush() }),
        BERRYBUSH("berry bush", "berry bushes", { BerryBush() }),
        HONEYPODBUSH("honeypod bush", "honeypod bushes", { HoneypodBush() }),
        WILDFLOWERS("wildflowers", "wildflowers", { Wildflowers() }),
        POPPIES("poppies", "poppies", { Poppies() }),
        DEATHFLOWER("deathflower", "deathflowers", { Deathflower() }),
        DREAMFLOWER("dreamflower", "dreamflowers", { Dreamflower() }),
        SUNFLOWER("sunflower", "sunflowers", { Sunflower() }),
        LIGHTFLOWER("lightflower", "lightflowers", { Lightflower() }),
        SAGUARO("saguaro", "saguaros", { Saguaro() }),
        CHOLLA("cholla", "chollas", { Cholla() }),
        PRICKPEAR("prickpear", "prickpears", { Prickpear() }),
        BALMMOSS("balm moss", "balm mosses", { BalmMoss() }),
        LACEMOSS("lace moss", "lace mosses", { LaceMoss() }),
        WIZARDCAP_MYCELIUM("", "", { WizardcapMycelium() }),
        SPECKLED_MYCELIUM("", "",{ SpeckledMycelium() }),
        BLOODCAP_MYCELIUM("", "", { BloodcapMycelium() }),
        WIZARDCAP_MUSHROOM("wizardcap mushroom", "wizardcap mushrooms", { WizardcapMushroom() }),
        SPECKLED_MUSHROOM("speckled mushroom", "speckled mushrooms", { SpeckledMushroom() }),
        BLOODCAP_MUSHROOM("bloodcap mushroom", "bloodcap mushrooms", { BloodcapMushroom() }),
        FOOLSLEAF("foolsleaf", "foolsleaves", { Foolsleaf() }),
        HIGHWAY_SIGN("highway sign", "highway signs", { HighwaySign("") }),
        TRAIL_SIGN("trail sign", "trail signs", { TrailSign("") }),
        BOULDER("boulder", "boulders", { Boulder() }),
        SHRINE("shrine", "shrines", { Shrine() }),
        OAKTREE("oak tree", "oak trees", { OakTree() }),
        TEAKTREE("teak tree", "teak trees", { TeakTree() }),
        MAPLETREE("maple tree", "maple trees", { MapleTree() }),
        BIRCHTREE("birch tree", "birch trees", { BirchTree() }),
        APPLETREE("apple tree", "apple trees", { AppleTree() }),
        PEARTREE("pear tree", "pear trees", { PearTree() }),
        PINETREE("pine tree", "pine trees", { PineTree() }),
        SPRUCETREE("spruce tree", "spruce trees", { SpruceTree() }),
        PALMTREE("palm tree", "palm trees", { PalmTree() }),
        COCONUTTREE("coconut tree", "coconut trees", { CoconutTree() }),
        DEADTREE("dead tree", "dead trees", { DeadTree() }),
        WELL("well", "wells", { Well() }),
        CAMPFIRE("campfire", "campfires", { Campfire() }),
        GRAVESTONE("gravestone", "gravestones", { Gravestone("") }),
        ANIMAL_HIDE("hide", "hides", { Hide() }),
        SCALY_HIDE("scaly hide", "scaly hides", { ScalyHide() }),
        CANDLESTICK("candlestick", "candlesticks", { Candlestick() }),
        LAMPPOST("lamppost", "lampposts", { Lamppost() }),
        STICK("stick", "sticks", { Stick() }),
        REBAR("rebar", "rebars", { Rebar() }),
        BED("bed", "beds", { Bed() })
        ;

        override fun levels() = levels
        override fun variants(): Set<Variant<Thing>>? = variants
    }

    abstract val tag: Tag
    open fun isOpaque(): Boolean = false
    open fun isBlocking(actor: Actor): Boolean = false
    open fun isPortable(): Boolean = true
    open fun isIntangible() = false
    open fun isAlwaysVisible() = false
    open fun announceOnWalk() = !isIntangible()
    open fun sleepComfort() = -0.1f
    open fun value() = 0

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
    fun isNextTo(actor: Actor) = holder?.let { it.xy().let { xy ->
        Math.abs(xy.x - actor.xy.x) < 2 && Math.abs(xy.y - actor.xy.y) < 2
    }} ?: false
    protected fun isNextTo(x: Int, y: Int) = holder?.let { it.xy().let { xy ->
        Math.abs(xy.x - x) < 2 && Math.abs(xy.y - y) < 2
    }} ?: false

    override fun description() =  ""
    open fun listTag() = if (tag == App.player.thrownTag) "(throwing)" else ""
    fun listName() = name() + " " + listTag()
    open fun canListGrouped() = true

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
    fun moveTo(xy:  XY) = moveTo(xy.x, xy.y)
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
class Hide : Portable() {
    override val tag = Tag.ANIMAL_HIDE
    override fun name() = "hide"
    override fun description() = "A leather animal hide.  You could make something out of it."
    override fun glyph() = Glyph.LEATHER
    override fun weight() = 0.5f
}

@Serializable
class ScalyHide: Portable() {
    override val tag = Tag.SCALY_HIDE
    override fun name() = "scaly hide"
    override fun description() = "A thick animal hide covered in rigid scales.  You could make something out of it."
    override fun glyph() = Glyph.LEATHER
    override fun weight() = 0.7f
}


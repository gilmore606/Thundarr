package things

import actors.actors.Actor
import actors.actors.Player
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import ktx.async.KtxAsync
import render.tilesets.Glyph
import util.Dice
import util.log
import world.level.Level

@Serializable
sealed class Container : Portable(), ThingHolder {

    protected val contents = ArrayList<Thing>()

    override fun contents() = contents
    @Transient
    override var level: Level? = null

    override fun containsByID(byID: String): Thing? {
        super.containsByID(byID)?.also { return it }
        contents().forEach {
            it.containsByID(byID)?.also { return it }
        }
        return null
    }

    override fun getHolderKey() = ThingHolder.Key(ThingHolder.Type.CONTAINER, containerKey = getKey())

    open fun openVerb() = "look inside"
    open fun isEmptyMsg() = "It's empty."
    open fun preposition() = "in"
    open fun itemLimit() = 100
    open fun isOpenable() = true
    open fun canAccept(thing: Thing) = contents().size < itemLimit()

    override fun onRestore(holder: ThingHolder) {
        super.onRestore(holder)
        this.level = holder.level
        contents.forEach { it.onRestore(this) }
    }

    override fun onMoveTo(from: ThingHolder?, to: ThingHolder?) {
        super.onMoveTo(from, to)
        this.level = holder?.level
    }

    override fun add(thing: Thing) {
        contents.add(thing)
        onAdd(thing)
    }
    open fun onAdd(thing: Thing) { }

    override fun remove(thing: Thing) {
        contents.remove(thing)
        onRemove(thing)
    }
    open fun onRemove(thing: Thing) { }

    open fun randomTreasure(): Thing? = null
    open fun randomTreasureCount(): Int = 0

    override fun onSpawn() {
        // TODO: Find a more generic way to deal with this, that doesn't involve launching one for every thing.onSpawn().
        // Coroutine, because otherwise constructor hasn't finished and we don't have a contents.
        val treasures = randomTreasureCount()
        if (treasures > 0) {
            KtxAsync.launch {
                repeat (treasures) {
                    randomTreasure()?.moveTo(this@Container)
                }
            }
        }
    }

    override fun uses(): MutableMap<UseTag, Use> {
        if (isOpenable()) {
            return super.uses().apply {
                this[UseTag.OPEN] = Use(openVerb() + " " + name(), 1.5f,
                    canDo = { actor, x, y, targ -> !targ && isHeldBy(actor) || isNextTo(actor) },
                    toDo = { actor, level, x, y ->
                        if (actor is Player) {
                            App.openInventory(withContainer = this@Container)
                        }
                    })
            }
        } else return super.uses()
    }

}

@Serializable
class FilingCabinet : Container() {
    override val tag = Tag.FILING_CABINET
    override fun name() = "filing cabinet"
    override fun description() = "You don't know what files are, but you know they went in here."
    override fun glyph() = Glyph.FILING_CABINET
    override fun isPortable() = false
    override fun openVerb() = "open"

    override fun randomTreasureCount() = Dice.zeroTo(2)
    override fun randomTreasure() = when (Dice.zeroTo(3)) {
        0 -> BoysLife()
        1 -> Lighter()
        2 -> HardHat()
        else -> Paperback()
    }
}

@Serializable
class StorageCabinet : Container() {
    override val tag = Tag.STORAGE_CABINET
    override fun name() = "storage cabinet"
    override fun description() = "A metal cabinet with large, lockable drawers."
    override fun glyph() = Glyph.FILING_CABINET
    override fun hue() = -0.9f
    override fun isPortable() = false
    override fun openVerb() = "open"
    override fun randomTreasureCount() = Dice.zeroTo(2)
    override fun randomTreasure() = when (Dice.zeroTo(3)) {
        0 -> BoysLife()
        1 -> Lighter()
        2 -> HardHat()
        else -> Paperback()
    }
}

@Serializable
class Bookshelf : Container() {
    override val tag = Tag.BOOKSHELF
    override fun name() = "bookshelf"
    override fun description() = "Shelves full of mostly uninteresting books."
    override fun glyph() = Glyph.BOOKSHELF
    override fun isPortable() = false
    override fun openVerb() = "peruse"

    override fun randomTreasureCount() = Dice.zeroTo(2)
    override fun randomTreasure() = when (Dice.zeroTo(1)) {
        0 -> BoysLife()
        1 -> Paperback()
        else -> Paperback()
    }
}

@Serializable
class Wardrobe : Container() {
    override val tag = Tag.WARDROBE
    override fun name() = "wardrobe"
    override fun description() = "A wooden wardrobe."
    override fun glyph() = Glyph.WARDROBE
    override fun isPortable() = false

    override fun randomTreasureCount() = Dice.zeroTo(2)
}

@Serializable
class Fridge : Container() {
    override val tag = Tag.FRIDGE
    override fun name() = "fridge"
    override fun description() = "A box chilled by technology to preserve foods.  Astounding."
    override fun glyph() = Glyph.FRIDGE
    override fun isPortable() = false
    override fun openVerb() = "open"

    fun isRefrigerating() = true  // TODO: invent electric power

    override fun randomTreasureCount() = Dice.oneTo(3)
    override fun randomTreasure() = when (Dice.zeroTo(13)) {
        0,8,9,10 -> RawMeat()
        1 -> ChickenLeg()
        2 -> Cheese()
        3 -> EnergyDrink()
        4 -> Steak()
        5 -> Apple()
        6 -> Pear()
        7,11 -> ThrallChow()
        else -> Stew()
    }
}

@Serializable
class Trunk : Container() {
    override val tag = Tag.TRUNK
    override fun name() = "trunk"
    override fun description() = "A heavy metal-bound trunk."
    override fun glyph() = Glyph.STRONGBOX
    override fun isPortable() = false
    override fun openVerb() = "open"

    override fun randomTreasureCount() = Dice.oneTo(4)
    override fun randomTreasure() = when (Dice.zeroTo(10)) {
        0 -> BoysLife()
        1 -> Lighter()
        2 -> HardHat()
        3 -> Axe()
        4 -> Pickaxe()
        5 -> Bandages()
        6 -> FirstAidKit()
        7 -> RiotHelmet()
        8 -> TravelBoots()
        9 -> Paperback()
        else -> Torch()
    }
}

@Serializable
class Bonepile : Container() {
    override val tag = Tag.BONEPILE
    override fun name() = "bone pile"
    override fun description() = "A pile of dried old bones; from what, you can't tell."
    override fun glyph() = Glyph.BONEPILE
    override fun isPortable() = false
    override fun openVerb() = "search"
    override fun isEmptyMsg() = "You find nothing useful in the bones."

    override fun randomTreasureCount() = Dice.zeroTo(1)
    override fun randomTreasure() = when (Dice.zeroTo(10)) {
        0 -> BoysLife()
        1 -> Lighter()
        2 -> HardHat()
        3 -> Axe()
        4 -> Pickaxe()
        5 -> Bandages()
        6 -> FirstAidKit()
        7 -> RiotHelmet()
        8 -> TravelBoots()
        9 -> Paperback()
        else -> Torch()
    }
}

@Serializable
class WreckedCar : Container() {
    override val tag = Tag.WRECKEDCAR
    override fun name() = "wrecked vehicle"
    override fun description() = "The hulk of a rusty metal wagon."
    override fun glyph() = Glyph.WRECKED_CAR
    override fun isPortable() = false
    override fun isBlocking(actor: Actor) = true
    override fun openVerb() = "search"
    override fun isEmptyMsg() = "You find nothing useful in the vehicle."

    override fun randomTreasureCount() = Dice.zeroTo(1)
    override fun randomTreasure() = when (Dice.zeroTo(10)) {
        0 -> BoysLife()
        1 -> Lighter()
        2 -> HardHat()
        3 -> Axe()
        4 -> Pickaxe()
        5 -> Bandages()
        6 -> FirstAidKit()
        7 -> RiotHelmet()
        8 -> TravelBoots()
        9 -> Paperback()
        else -> Torch()
    }
}

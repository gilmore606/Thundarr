package things

import actors.actors.Actor
import actors.actors.Player
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import ktx.async.KtxAsync
import render.tilesets.Glyph
import util.Dice
import world.gen.spawnsets.*
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

    open fun withLoot(lootSet: LootSet, lootCount: Int, xpLevel: Int): Container {
        KtxAsync.launch {
            repeat(lootCount) {
                lootSet.getLoot(xpLevel)?.also {
                    it.moveTo(this@Container)
                }
            }
        }
        return this
    }

    open fun withDefaultLoot(xpLevel: Int): Container = defaultLoot()?.let { withLoot(it, defaultLootCount(), xpLevel) } ?: this
    open fun defaultLoot(): LootSet? = null
    open fun defaultLootCount() = 0

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
}

@Serializable
class StoreCabinet : Container() {
    override val tag = Tag.WARDROBE
    override fun name() = "storage cabinet"
    override fun description() = "A metal cabinet with large, lockable drawers."
    override fun glyph() = Glyph.FILING_CABINET
    override fun hue() = 3.2f
    override fun isPortable() = false
    override fun openVerb() = "open"
    override fun isOpenable() = false
}

@Serializable
class Bookshelf : Container() {
    override val tag = Tag.BOOKSHELF
    override fun name() = "bookshelf"
    override fun description() = "Shelves full of mostly uninteresting books."
    override fun glyph() = Glyph.BOOKSHELF
    override fun isPortable() = false
    override fun openVerb() = "peruse"
}

@Serializable
class Wardrobe : Container() {
    override val tag = Tag.WARDROBE
    override fun name() = "wardrobe"
    override fun description() = "A wooden wardrobe."
    override fun glyph() = Glyph.WARDROBE
    override fun isPortable() = false
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
}

@Serializable
class Trunk : Container() {
    override val tag = Tag.TRUNK
    override fun name() = "trunk"
    override fun description() = "A heavy metal-bound trunk."
    override fun glyph() = Glyph.STRONGBOX
    override fun isPortable() = false
    override fun openVerb() = "open"
}

@Serializable
class Chest : Container() {
    override val tag = Tag.CHEST
    override fun name() = "chest"
    override fun description() = "A sturdy oak chest."
    override fun glyph() = Glyph.CHEST
    override fun isPortable() = false
    override fun openVerb() = "open"
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
    override fun defaultLoot() = TravelerLoot.set
    override fun defaultLootCount() = Dice.range(0, 3)
}

@Serializable
class WreckedCar : Container() {
    var hue: Float = 0f
    override val tag = Tag.WRECKEDCAR
    override fun name() = "wrecked vehicle"
    override fun description() = "The hulk of a rusty metal wagon."
    override fun glyph() = Glyph.WRECKED_CAR
    override fun hue() = hue
    override fun isPortable() = false
    override fun isBlocking(actor: Actor) = true
    override fun openVerb() = "search"
    override fun isEmptyMsg() = "You find nothing useful in the vehicle."

    override fun onSpawn() {
        super.onSpawn()
        hue = Dice.float(-2f, 2f)
    }
}

package things

import actors.Player
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import ktx.async.KtxAsync
import render.tilesets.Glyph
import util.Dice
import world.level.Level

@Serializable
sealed class Container : Portable(), ThingHolder {

    private val contents = ArrayList<Thing>()

    override fun contents() = contents
    @Transient
    override var level: Level? = null

    open fun openVerb() = "look inside"
    open fun isEmptyMsg() = "It's empty."
    open fun preposition() = "in"
    open fun itemLimit() = 100
    open fun isOpenable() = true

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

    override fun uses(): Map<UseTag, Use> {
        if (isOpenable()) {
            return mapOf(
                UseTag.OPEN to Use(openVerb() + " " + name(), 1.5f,
                    canDo = { actor, x, y, targ -> !targ && isHeldBy(actor) || isNextTo(actor) },
                    toDo = { actor, level, x, y ->
                        if (actor is Player) {
                            App.openInventory(withContainer = this@Container)
                        }
                    })
            )
        } else return mapOf()
    }
}

@Serializable
class FilingCabinet : Container() {
    override fun name() = "filing cabinet"
    override fun description() = "You don't know what files are, but you know they went in here."
    override fun glyph() = Glyph.FILING_CABINET
    override fun isPortable() = false
    override fun openVerb() = "open"

    override fun onSpawn() {
        KtxAsync.launch {
            repeat (Dice.zeroTo(2)) {
                when (Dice.zeroTo(3)) {
                    0 -> BoysLife()
                    1 -> Lighter()
                    2 -> HardHat()
                    else -> Paperback()
                }.moveTo(this@FilingCabinet)
            }
        }
    }
}

@Serializable
class Fridge : Container() {
    override fun name() = "fridge"
    override fun description() = "A box chilled by technology to preserve foods.  Astounding."
    override fun glyph() = Glyph.FRIDGE
    override fun isPortable() = false
    override fun openVerb() = "open"

    fun isRefrigerating() = true  // TODO: invent electric power

    override fun onSpawn() {
        // Coroutine, because otherwise constructor hasn't finished and we don't have a contents.
        // TODO: Find a more generic way to deal with this, that doesn't involve launching one for every thing.onSpawn().
        KtxAsync.launch {
            repeat (Dice.oneTo(3)) {
                when (Dice.zeroTo(7)) {
                    0 -> RawMeat()
                    1 -> ChickenLeg()
                    2 -> Cheese()
                    3 -> EnergyDrink()
                    4 -> Steak()
                    5 -> Apple()
                    6 -> Pear()
                    else -> Stew()
                }.moveTo(this@Fridge)
            }
        }
    }
}

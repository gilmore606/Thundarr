package things

import actors.Player
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.tilesets.Glyph
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

}

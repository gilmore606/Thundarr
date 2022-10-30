package things

import actors.Player
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.tilesets.Glyph
import world.Level

@Serializable
sealed class Container : Portable(), ThingHolder {

    private val contents = ArrayList<Thing>()

    override fun contents() = contents
    override fun xy() = holder?.xy()
    @Transient
    override var level: Level? = null

    open fun openVerb() = "look inside"

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
    }

    override fun remove(thing: Thing) {
        contents.remove(thing)
    }

    override fun uses() = setOf(
        Use(openVerb() + " " + name(), 1.5f, { it.xy.x == xy()?.x && it.xy.y == xy()?.y },
            { actor, level ->
                if (actor is Player) {
                    App.openInventory(withContainer = this@Container)
                }
            })
    )
}

@Serializable
class FilingCabinet : Container() {

    override fun name() = "filing cabinet"
    override fun description() = "You don't know what files are, but you know they went in here."
    override fun glyph() = Glyph.FILING_CABINET
    override fun isPortable() = false
    override fun openVerb() = "open"

}

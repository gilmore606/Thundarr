package things

interface ThingHolder {

    val contents: MutableList<Thing>
    fun add(thing: Thing)
    fun remove(thing: Thing)

}

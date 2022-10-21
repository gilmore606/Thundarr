package things

interface ThingHolder {

    val contents: MutableList<Thing>
    fun add(thing: Thing)
    fun remove(thing: Thing)

    fun byKind() = mutableMapOf<Thing.Kind, MutableSet<Thing>>().apply {
        contents.forEach { thing ->
            if (containsKey(thing.kind)) {
                this[thing.kind]?.add(thing)
            } else {
                this[thing.kind] = mutableSetOf(thing)
            }
        }
    }
}

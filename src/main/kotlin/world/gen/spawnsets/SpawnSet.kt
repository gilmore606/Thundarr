package world.gen.spawnsets

import things.Thing
import util.Dice
import variants.Variant
import world.Entity

abstract class SpawnSet<S: Any, O: Entity, T: Any>() {

    sealed class Entry<S,O,T> (
        val freq: Float,
        val limit: Pair<Int,Int> = Pair(0,100)
    ) {
        open fun matches(filter: T?) = true
    }

    class ItemEntry<S,O,T>(
        freq: Float,
        val item: S,
        limit: Pair<Int,Int> = Pair(0,100),
        val only: T? = null,
        val include: Set<T>? = null,
        val exclude: Set<T>? = null,
    ) : Entry<S,O,T>(freq, limit) {

        override fun matches(filter: T?): Boolean {
            if (filter == null) return true
            only?.also { return it == filter }
            include?.also { return it.contains(filter) }
            exclude?.also { return !it.contains(filter) }
            return true
        }
    }

    class SubsetEntry<S: Any, O: Entity, T: Any>(
        freq: Float,
        val subset: SpawnSet<S,O,T>,
        limit: Pair<Int,Int> = Pair(0,100)
    ) : Entry<S,O,T>(freq, limit) {
        fun roll(limitValue: Int, filterBy: T? = null) = subset.roll(limitValue, filterBy)
    }

    class Result<S,O> (
        val item: S,
        val variant: Variant<O>? = null
    ) {
        fun spawnThing() = (item as Thing.Tag).spawn()
    }

    val set: MutableSet<Entry<S,O,T>> = mutableSetOf()

    fun addEntry(freq: Float, item: S, limit: Pair<Int,Int> = Pair(0,100), only: T? = null, include: Set<T>? = null, exclude: Set<T>? = null) {
        set.add(ItemEntry(
            freq = freq,
            item = item,
            limit = limit,
            only = only,
            include = include,
            exclude = exclude
        ))
    }

    protected fun roll(limitValue: Int, filterBy: T? = null): Result<S,O>? {
        val filtered = mutableListOf<Pair<Float,Entry<S,O,T>>>()
        var freqTotal = 0f
        set.forEach { cand ->
            var freq = when (limitValue) {
                cand.limit.first - 1 -> cand.freq * 0.5f
                cand.limit.first - 2 -> cand.freq * 0.25f
                cand.limit.second + 1 -> cand.freq * 0.25f
                in cand.limit.first..cand.limit.second -> cand.freq
                else -> null
            }
            if (!cand.matches(filterBy)) freq = null

            freq?.also { freq ->
                filtered.add(Pair(freq, cand))
                freqTotal += freq
            }
        }
        if (filtered.isEmpty()) return null

        val roll = Dice.float(0f, freqTotal)
        var acc = 0f
        var winner: Entry<S,O,T>? = null
        filtered.forEach {
            if (winner == null) {
                acc += it.first
                if (roll <= acc) {
                    winner = it.second
                }
            }
        }

        if (winner is SubsetEntry<S,O,T>) {
            return (winner as SubsetEntry<S,O,T>).roll(limitValue, filterBy)
        } else if (winner is ItemEntry<S,O,T>) {
            return Result((winner as ItemEntry<S,O,T>).item)
        }

        return null
    }
}
package world.gen.spawnsets

import things.*
import things.Thing.Tag.*
import things.gearmods.GearMod
import things.gearmods.GearMod.Tag.*
import util.Dice
import util.total
import world.gen.spawnsets.LootSet.*

open class LootSet {

    sealed class Entry(
        val freq: Float,
        val limit: Pair<Int,Int> = Pair(0,100)
    )

    open class ItemEntry(
        freq: Float,
        val item: Thing.Tag,
        limit: Pair<Int,Int> = Pair(0,100),
        val mod: GearMod.Tag? = null,
        val customizer: ((Thing)->Unit)? = null
    ) : Entry(freq, limit)

    class SubsetEntry(
        freq: Float,
        val subset: LootSet,
        limit: Pair<Int,Int> = Pair(0,100)
    ) : Entry(freq, limit)

    class Variant(
        val freq: Float,
        val mod: GearMod.Tag? = null,
        val levelMin: Int = 0,
        val levelMax: Int = 100,
        val customizer: ((Thing)->Unit)? = null
    )

    val set: MutableSet<Entry> = mutableSetOf()

    fun add(freq: Float, item: Thing.Tag, levelMin: Int = 0, levelMax: Int = 100, mod: GearMod.Tag? = null, custom: ((Thing)->Unit)? = null) {
        set.add(ItemEntry(
            freq = freq, item = item, limit = Pair(levelMin, levelMax), mod = mod, customizer = custom)
        )
    }

    fun add(freq: Float, subset: LootSet, levelMin: Int = 0, levelMax: Int = 100) {
        set.add(SubsetEntry(
            freq = freq, subset = subset, limit = Pair(levelMin, levelMax)
        ))
    }

    fun add(freq: Float, item: Thing.Tag, variants: List<Variant>) {
        val freqTotal = variants.total { it.freq }
        variants.forEach {
            add(freq * (it.freq / freqTotal), item, it.levelMin, it.levelMax, it.mod, it.customizer)
        }
    }

    open fun getLoot(level: Int): Thing? {
        val filtered = mutableListOf<Pair<Float, Entry>>()
        var freqTotal = 0f
        set.forEach { cand ->
            when (level) {
                cand.limit.first - 1 -> cand.freq * 0.5f
                cand.limit.first - 2 -> cand.freq * 0.25f
                cand.limit.second + 1 -> cand.freq * 0.25f
                in cand.limit.first..cand.limit.second -> cand.freq
                else -> null
            }?.also { freq ->
                filtered.add(Pair(freq, cand))
                freqTotal += freq
            }
        }
        if (filtered.isEmpty()) return null

        val roll = Dice.float(0f, freqTotal)
        var acc = 0f
        var winner: Entry? = null
        filtered.forEach {
            if (winner == null) {
                acc += it.first
                if (roll <= acc) {
                    winner = it.second
                }
            }
        }
        if (winner is SubsetEntry) {
            return (winner as SubsetEntry).subset.getLoot(level)
        } else if (winner is ItemEntry) {
            (winner as ItemEntry).also {
                val thing = it.item.create()
                it.mod?.also { (thing as Gear).addMod(it.get) }
                it.customizer?.invoke(thing)
                return thing
            }
        }
        return null
    }
}

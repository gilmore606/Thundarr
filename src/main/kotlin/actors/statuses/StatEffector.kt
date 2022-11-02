package actors.statuses

import actors.stats.Stat

interface StatEffector {
    fun name(): String
    fun statEffects(): Map<Stat.Tag, Float>
}

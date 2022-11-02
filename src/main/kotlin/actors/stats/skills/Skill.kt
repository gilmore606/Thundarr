package actors.stats.skills

import actors.Actor
import actors.stats.Stat

val allSkills = listOf(Dig, Fight, Throw)

abstract class Skill(tag: Stat.Tag, name: String,
                    val dependsOn: Set<Stat>
                     ) : Stat(tag, name) {

    init {
        dependsOn.forEach { it.addDependent(this) }
    }

    // A skill total is the average of dependent stats, plus base.
    override fun total(actor: Actor, base: Float): Float {
        var total = 0f
        dependsOn.forEach { total += it.get(actor) }
        total /= dependsOn.size
        total += base
        return total
    }

}

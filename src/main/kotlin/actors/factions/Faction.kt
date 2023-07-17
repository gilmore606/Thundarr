package actors.factions

import actors.actors.Actor
import actors.actors.NPC
import kotlinx.serialization.Serializable
import util.UUID
import world.quests.Quest

@Serializable
open class Faction(
    val name: String,
) {

    val id = UUID()
    override fun toString() = "faction $name"

    private val opinions = mutableMapOf<String, NPC.Opinion>()

    val quests = mutableListOf<String>()

    // Should the faction insta-hate someone who attacks a member?
    open fun hateMemberAttacker() = false
    // Should the faction love fellow members (i.e. defend them?)
    open fun memberLove() = false

    open fun isHuman() = true
    open fun isGood() = false
    open fun isEvil() = false

    fun onMemberAttacked(attacker: Actor) {
        if (hateMemberAttacker()) {
            downgradeOpinionOf(attacker)
        }
    }

    open fun hatesFaction(otherFaction: Faction): Boolean = false
    open fun lovesFaction(otherFaction: Faction): Boolean = otherFaction == this && memberLove()

    fun opinionOf(actor: Actor): NPC.Opinion {
        if (opinions.containsKey(actor.id)) return opinions[actor.id]!! else {
            var loved = false
            actor.factions.forEach { id ->
                App.factions.byID(id)?.also { actorFaction ->
                    if (this.hatesFaction(actorFaction)) return NPC.Opinion.HATE
                    else if (this.lovesFaction(actorFaction)) loved = true
                }
            }
            return if (loved) NPC.Opinion.LOVE else NPC.Opinion.NEUTRAL
        }
    }

    fun upgradeOpinionOf(actor: Actor) {
        if (opinions[actor.id] == NPC.Opinion.HATE) {
            opinions.remove(actor.id)
        } else {
            opinions[actor.id] = NPC.Opinion.LOVE
        }
    }

    fun downgradeOpinionOf(actor: Actor) {
        if (opinions[actor.id] == NPC.Opinion.LOVE) {
            opinions.remove(actor.id)
        } else {
            opinions[actor.id] = NPC.Opinion.HATE
        }
    }

    fun addQuest(quest: Quest) {
        App.factions.addQuest(quest)
        quests.add(quest.id)
    }
}

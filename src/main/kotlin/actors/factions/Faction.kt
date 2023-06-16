package actors.factions

import actors.Actor
import kotlinx.serialization.Serializable
import util.UUID
import util.log

@Serializable
open class Faction(
    val name: String,
) {

    val id = UUID()

    val hatedActors = mutableSetOf<String>()

    // Should the faction hate someone who attacks a member?
    open fun hateMemberAttacker() = false

    fun onMemberAttacked(attacker: Actor) {
        if (hateMemberAttacker()) {
            hateActor(attacker)
        }
    }

    fun hateActor(actor: Actor) {
        log.info("faction $this ($id) now hates $actor (${actor.id})")
        hatedActors.add(actor.id)
    }

}

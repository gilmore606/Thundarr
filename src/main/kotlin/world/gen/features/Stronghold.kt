package world.gen.features

import kotlinx.serialization.Serializable

@Serializable
sealed class Stronghold(
    private val strongholdAbandoned: Boolean = false
) : Habitation(strongholdAbandoned) {

    var empire: Int? = null

    override fun name(): String = "???"
}

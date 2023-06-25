package world.gen.features

import kotlinx.serialization.Serializable

@Serializable
sealed class Stronghold : Habitation() {

    var empire: Int? = null

    override fun name(): String = "???"
}

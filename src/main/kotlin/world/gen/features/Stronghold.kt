package world.gen.features

import kotlinx.serialization.Serializable

@Serializable
sealed class Stronghold : Feature() {

    var empire: Int? = null

    abstract fun name(): String

}

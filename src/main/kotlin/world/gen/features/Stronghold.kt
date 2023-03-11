package world.gen.features

import kotlinx.serialization.Serializable
import world.history.Empire

@Serializable
abstract class Stronghold : Feature() {

    var empire: Empire? = null

}

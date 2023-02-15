package world.gen.features

import kotlinx.serialization.Serializable

@Serializable
class RuinedCitySite(
    val name: String,
) : ChunkFeature(
    4, Stage.BUILD
) {

    override fun doDig() {
        // NOOP.  We only store this for a map marker.
    }

}

package world.gen.features

import kotlinx.serialization.Serializable
import render.tilesets.Glyph

@Serializable
class RuinedCitySite(
    val name: String,
) : Stronghold() {
    override fun order() = 4
    override fun stage() = Stage.BUILD
    override fun name() = name

    override fun doDig() {
        // NOOP.  We only store this for a map marker.
    }

    override fun mapIcon(): Glyph? = Glyph.MAP_CITY
    override fun mapPOITitle() = name
    override fun mapPOIDescription() = "The ruins of the fabled lost city of $name."
}

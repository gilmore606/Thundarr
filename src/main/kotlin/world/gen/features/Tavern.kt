package world.gen.features

import kotlinx.serialization.Serializable
import util.XY
import world.ChunkScratch
import world.gen.biomes.Glacier
import world.gen.biomes.Ocean

@Serializable
class Tavern(
    private val name: String,
    private val villageDirection: XY
) : Feature(
    4, Stage.BUILD
) {

    companion object {
        fun canBuildOn(meta: ChunkScratch) = meta.biome !in listOf(Ocean, Glacier)
    }

    override fun trailDestinationChance() = 1f

    override fun doDig() {

    }

}

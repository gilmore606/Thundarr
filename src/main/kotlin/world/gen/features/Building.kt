package world.gen.features

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import world.ChunkScratch
import world.gen.biomes.Biome
import world.gen.biomes.Glacier
import world.gen.biomes.Ocean
import world.quests.FetchQuest

@Serializable
class Building : Feature() {
    override fun order() = 1
    override fun stage() = Stage.BUILD
    override fun canBeQuestDestination() = true
    override fun createQuest() = FetchQuest()
    override fun name() = "strange building"
    override fun trailDestinationChance() = 1f
    override fun mapIcon(onBiome: Biome?) = Glyph.MAP_BUILDING
    override fun mapPOITitle() = name()
    override fun mapPOIDescription() = "A strange building."

    override fun loreKnowabilityRadius() = 400
    override fun loreName() = "strange building"
    override fun xpValue() = 50

    companion object {
        fun canBuildOn(meta: ChunkScratch) = !meta.hasFeature(Village::class) && meta.biome !in listOf(Ocean, Glacier)
    }

    override fun doDig() {
        carto.buildStructureDungeon()
    }

}

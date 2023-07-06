package world.gen.features

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.Dice
import util.Rect
import world.ChunkScratch
import world.gen.biomes.Biome
import world.gen.biomes.Mountain
import world.gen.decors.Decor
import world.gen.decors.MountainPeak
import world.quests.FetchQuest

@Serializable
class Peak(
    val name: String
) : Feature() {
    override fun order() = 3
    override fun stage() = Stage.TERRAIN
    override fun name() = name
    override fun cellTitle() = name
    override fun mapIcon(onBiome: Biome?): Glyph? = Glyph.MAP_PEAK
    override fun mapPOITitle() = name
    override fun mapPOIDescription() = ""
    override fun trailDestinationChance() = 0.6f
    override fun canBeQuestDestination() = true
    override fun createQuest() = FetchQuest()
    override fun loreKnowabilityRadius() = 800
    override fun xpValue() = 20
    override fun temperatureMod() = -6

    companion object {
        fun canBuildOn(meta: ChunkScratch) = !meta.hasFeature(Village::class) &&
                meta.biome == Mountain
    }

    override fun doDig() {
        printGrid(growBlob(Dice.range(14,20), Dice.range(14,20)), x0 + 20, y0 + 20, meta.biome.baseTerrain)
        MountainPeak().furnish(Decor.Room(
            Rect(x0 + 22, y0 + 22, x1 - 22, y1 - 22)
        ), carto)
    }
}

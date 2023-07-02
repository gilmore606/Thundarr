package world.gen.features

import audio.Speaker
import kotlinx.serialization.Serializable
import util.CARDINALS
import util.Dice
import util.hasOneWhere
import world.quests.FetchQuest
import world.terrains.Terrain

@Serializable
class Volcano : Feature() {
    override fun order() = 2
    override fun stage() = Stage.BUILD
    override fun canBeQuestDestination() = true
    override fun createQuest() = FetchQuest()
    override fun name() = "volcano"

    override fun trailDestinationChance() = 1f

    override fun loreKnowabilityRadius() = 1000
    override fun loreName() = "an active volcano"

    override fun doDig() {
        val width = Dice.range(40,56)
        val height = Dice.range(40,56)
        printGrid(growBlob(width, height), x0 + Dice.range(3,6), y0 + Dice.range(3,6), Terrain.Type.TERRAIN_LAVA)
        fringeTerrain(Terrain.Type.TERRAIN_LAVA, Terrain.Type.TERRAIN_ROCKS, 0.7f)
        addLavaSound()
    }

    private fun addLavaSound() {
        forEachTerrain(Terrain.Type.TERRAIN_LAVA) { x, y ->
            if (CARDINALS.hasOneWhere { boundsCheck(x+it.x, y+it.y) && getTerrain(x+it.x, y+it.y) != Terrain.Type.TERRAIN_LAVA }) {
                chunk.setSound(x, y, Speaker.PointAmbience(Speaker.Ambience.LAVA, 35f, 1f))
            }
        }
    }

}

package world.lore

import actors.NPC
import kotlinx.serialization.Serializable
import ui.modals.ConverseModal
import util.XY
import util.distanceBetween
import util.forXY
import util.toEnglishList
import world.gen.Metamap
import world.gen.features.Feature
import java.lang.Math.abs

@Serializable
class DirectionsLore(
    val sourceXY: XY
): Lore() {
    var directionsListMsg: String = "x, y, or z"

    val directions = mutableMapOf<String, Pair<String,Feature>>()

    init {
        findDirections()
    }

    data class Neighbor(val feature: Feature, val distance: Float) {
        var description: String = ""
    }

    private fun findDirections() {
        // Collect list of all knowable features nearby
        var neighbors = mutableListOf<Neighbor>()
        forXY((sourceXY.x-1280)/64,(sourceXY.y-1280)/64, (sourceXY.x+1280)/64,(sourceXY.y+1280)/64) { ix,iy ->
            val meta = Metamap.metaAtWorld(ix*64, iy*64)
            meta.features().forEach { feature ->
                val distance = distanceBetween(sourceXY.x, sourceXY.y, feature.worldX, feature.worldY)
                if (distance < feature.loreKnowabilityRadius() && distance > 32) {
                    neighbors.add(Neighbor(feature, distance))
                }
            }
        }
        neighbors.sortBy { it.distance }
        if (neighbors.size > 5) {
            neighbors = neighbors.subList(0, 4)
        }
        neighbors.forEach {
            it.description = it.feature.loreName().capitalize() + " is " + describeTravelDistance(it.distance) + " " +
            sourceXY.describeDirectionTo(XY(it.feature.worldX, it.feature.worldY)) + "."
        }
        // Describe distance + dir
        neighbors.forEach { neighbor ->
            directions[neighbor.feature.loreName()] = Pair(neighbor.description, neighbor.feature)
        }
        // Draw line to habitation and check for terrain/river crossings

        directionsListMsg = "Points of interest in these parts include " + directions.keys.toList().toEnglishList(articles = false) + "."
    }

    override fun helloText() = "I've travelled all over this region, and know the trails well."
    override fun subjects() = mutableSetOf(
        Subject("habitations", "travelled all over", "What other settlements are nearby?",
            directionsListMsg)
    ).apply {
        directions.forEach { direction ->
            add(Subject("directions_${direction.key}", direction.key, "How do I get to ${direction.key}?",
                direction.value.first + " I'll mark it on your map."))
        }
    }

    override fun getConversationTopic(talker: NPC, topic: String): ConverseModal.Scene? {
        if (topic.startsWith("directions_")) {
            val keyString = topic.drop(11)
            directions[keyString]?.also { pair ->
                val feature = pair.second
                Metamap.markChunkMappedAt(feature.worldX, feature.worldY)
            }
        }
        return super.getConversationTopic(talker, topic)
    }

    private fun describeTravelDistance(distance: Float): String {
        val perDay = 600
        if (distance < perDay / 15) {
            return "a short hike"
        } else if (distance < perDay / 12) {
            return "an hour's walk"
        } else if (distance < perDay / 2) {
            return "a few hours' hike"
        } else if (distance < perDay * 0.7) {
            return "a half day's travel"
        } else if (distance < perDay * 1.5) {
            return "a day's travel"
        } else if (distance < perDay * 2.5) {
            return "two days' travel"
        } else if (distance < perDay * 3.5) {
            return "three days' travel"
        } else return "a long journey"
    }

}

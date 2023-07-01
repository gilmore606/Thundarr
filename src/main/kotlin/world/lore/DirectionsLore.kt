package world.lore

import kotlinx.serialization.Serializable
import util.XY
import util.distanceBetween
import util.toEnglishList
import world.gen.Metamap
import world.gen.features.Feature

@Serializable
class DirectionsLore(
    val sourceXY: XY
): Lore() {
    var directionsListMsg: String = "x, y, or z"

    val directions = mutableMapOf<String, String>()

    init {
        findDirections()
    }

    private fun findDirections() {
        // Collect list of all knowable features nearby
        val neighbors = mutableMapOf<Feature,String>()
        for (ix in (sourceXY.x - 960)/64 .. (sourceXY.x + 960)/64) {
            for (iy in (sourceXY.y - 960)/64 .. (sourceXY.y + 960)/64) {
                val meta = Metamap.metaAtWorld(ix*64, iy*64)
                meta.features().forEach { feature ->
                    val distance = distanceBetween(sourceXY.x, sourceXY.y, feature.worldX, feature.worldY)
                    if (distance < feature.loreKnowabilityRadius() && distance > 32) {
                        neighbors[feature] = describeTravelDistance(distance)
                    }
                }
            }
        }
        // Describe distance + dir
        neighbors.keys.forEach { neighbor ->
            directions[neighbor.loreName()] = neighbor.loreName().capitalize() + " is " + neighbors[neighbor] + " " +
                    describeDirection(sourceXY, XY(neighbor.worldX, neighbor.worldY)) + "."
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
            add(Subject("directions_${direction.key}", direction.key, "How do I get to ${direction.key}?", direction.value))
        }
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

    private fun describeDirection(from: XY, to: XY): String {
        val xd = to.x - from.x
        val yd = to.y - from.y
        if (xd > yd * 2) {
            return if (xd > 0) "east" else "west"
        } else if (yd > xd * 2) {
            return if (yd > 0) "south" else "north"
        } else if (xd > 0 && yd > 0) return "southeast"
        else if (xd < 0 && yd < 0) return "northwest"
        else if (xd < 0 && yd > 0) return "southwest"
        else return "southeast"
    }
}

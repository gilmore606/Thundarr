package world

import kotlinx.serialization.Serializable

@Serializable
class Building(
    val id: String,
    val x: Int,
    val y: Int,
    val floorCount: Int,
    val floorWidth: Int,
    val floorHeight: Int,
    val firstLevelId: String,
    val doorMsg: String
)

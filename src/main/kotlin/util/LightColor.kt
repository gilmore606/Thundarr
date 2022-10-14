package util

import kotlinx.serialization.Serializable

@Serializable
data class LightColor(
    var r: Float,
    var g: Float,
    var b: Float
)

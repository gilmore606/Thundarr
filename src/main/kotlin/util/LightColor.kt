package util

import kotlinx.serialization.Serializable
import kotlin.math.pow
import kotlin.math.sqrt

@Serializable
data class LightColor(
    var r: Float,
    var g: Float,
    var b: Float
) {
    override fun toString() = "RGB($r,$g,$b)"
    fun brightness() = sqrt(0.299f*r.pow(2) + 0.587f*g.pow(2) + 0.114f*b.pow(2))
}

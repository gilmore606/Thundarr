package util

import kotlinx.serialization.Serializable
import kotlin.math.pow
import kotlin.math.sqrt

@Serializable
data class LightColor(
    var r: Float,
    var g: Float,
    var b: Float,
    var a: Float = 1f
) {
    override fun toString() = "RGB($r,$g,$b)"
    fun brightness() = sqrt(0.299f*r.pow(2) + 0.587f*g.pow(2) + 0.114f*b.pow(2))
    fun setTo(r: Float, g: Float, b: Float, a: Float = 1f) {
        this.r = r
        this.g = g
        this.b = b
        this.a = a
    }
}

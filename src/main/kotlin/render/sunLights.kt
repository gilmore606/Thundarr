package render

import util.LightColor

fun sunLights() = mutableMapOf<Int, LightColor>().apply {
    this[0] = LightColor(0.0f, 0.0f, 0.4f)
    this[4] = LightColor(0.2f, 0.3f, 0.7f)
    this[5] = LightColor(0.6f, 0.7f, 0.6f)
    this[7] = LightColor(0.8f, 0.9f, 0.6f)
    this[8] = LightColor(1f, 1f, 0.8f)
    this[12] = LightColor(1f, 1f, 1f)
    this[17] = LightColor(1f, 1f, 0.9f)
    this[20] = LightColor(1f, 0.7f, 0.6f)
    this[21] = LightColor(0.6f, 0.3f, 0.4f)
    this[23] = LightColor(0.1f, 0.1f, 0.4f)
}

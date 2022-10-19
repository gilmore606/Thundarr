package render

import util.LightColor

fun sunLights() = mutableMapOf<Int, LightColor>().apply {
    this[0] = LightColor(0.2f, 0.2f, 0.5f)
    this[1] = LightColor(0.15f, 0.15f, 0.5f)
    this[2] = LightColor(0.13f, 0.13f, 0.5f)
    this[3] = LightColor(0.1f, 0.1f, 0.4f)
    this[4] = LightColor(0.2f, 0.25f, 0.4f)
    this[5] = LightColor(0.4f, 0.4f, 0.5f)
    this[6] = LightColor(0.7f, 0.5f, 0.5f)
    this[7] = LightColor(1f, 0.6f, 0.6f)
    this[8] = LightColor(1f, 0.8f, 0.8f)
    this[9] = LightColor(1f, 0.9f, 0.8f)
    this[10] = LightColor(1f, 0.9f, 0.8f)
    this[11] = LightColor(1f, 1f, 0.9f)
    this[12] = LightColor(1f, 1f, 1f)
    this[13] = LightColor(1f, 1f, 1f)
    this[14] = LightColor(1f, 1f, 1f)
    this[15] = LightColor(1f, 1f, 1f)
    this[16] = LightColor(1f, 1f, 1f)
    this[17] = LightColor(1f, 1f, 0.9f)
    this[18] = LightColor(1f, 1f, 0.8f)
    this[19] = LightColor(0.8f, 0.7f, 0.6f)
    this[20] = LightColor(0.8f, 0.6f, 0.5f)
    this[21] = LightColor(0.6f, 0.5f, 0.5f)
    this[22] = LightColor(0.4f, 0.35f, 0.5f)
    this[23] = LightColor(0.2f, 0.2f, 0.5f)
}
